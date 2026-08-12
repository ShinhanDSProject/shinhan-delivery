package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.delivery.dto.response.DeliveryCancellationPreviewResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryCancellationResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.payment.dto.request.PointRefundRequest;
import com.example.shinhandelivery.payment.service.PaymentService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DeliveryCancellationServiceTest {

  private static final Long DELIVERY_ID = 10L;
  private static final Long CUSTOMER_ID = 1L;
  private static final Long COURIER_ID = 2L;

  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private MatchingRepository matchingRepository;
  @Mock private VehicleService vehicleService;
  @Mock private PaymentService paymentService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private DeliveryCancellationService service;

  @Test
  @DisplayName("배정 전 결제 배송 취소는 수수료 없이 전액 환불한다")
  void requestedCancellationRefundsAllPoints() {
    DeliveryRequest delivery = paidDelivery(DeliveryStatus.REQUESTED, 3000L);
    when(deliveryRequestRepository.findByIdForUpdate(DELIVERY_ID))
        .thenReturn(Optional.of(delivery));

    DeliveryCancellationResponse response = service.cancel(CUSTOMER_ID, DELIVERY_ID);

    assertThat(response.cancellationFee()).isZero();
    assertThat(response.refundAmount()).isEqualTo(3000L);
    assertThat(response.courierCompensation()).isZero();
    assertThat(delivery.getCancellationReason())
        .isEqualTo(DeliveryCancellationReason.CUSTOMER_REQUEST);
    verify(paymentService).refundPoint(eq(CUSTOMER_ID), eq("delivery-cancel-refund:10"), any());
    verify(paymentService, never()).compensateCourier(any(), any(), any(), any(Long.class));
  }

  @Test
  @DisplayName("배정 후 취소는 1000P를 배송원에게 보상하고 나머지만 고객에게 환불한다")
  void matchedCancellationDeductsFeeAndCompensatesCourier() {
    DeliveryRequest delivery = paidDelivery(DeliveryStatus.MATCHED, 3000L);
    Matching matching =
        Matching.builder()
            .deliveryRequestId(DELIVERY_ID)
            .vehicleId(20L)
            .status(MatchingStatus.MATCHED)
            .build();
    Vehicle vehicle =
        Vehicle.builder().id(20L).memberId(COURIER_ID).status(VehicleStatus.BUSY).build();
    when(deliveryRequestRepository.findByIdForUpdate(DELIVERY_ID))
        .thenReturn(Optional.of(delivery));
    when(matchingRepository.findByDeliveryRequestIdForUpdate(DELIVERY_ID))
        .thenReturn(Optional.of(matching));
    when(vehicleService.getVehicleForUpdate(20L)).thenReturn(vehicle);

    DeliveryCancellationResponse response = service.cancel(CUSTOMER_ID, DELIVERY_ID);

    assertThat(response.cancellationFee()).isEqualTo(1000L);
    assertThat(response.refundAmount()).isEqualTo(2000L);
    assertThat(response.courierCompensation()).isEqualTo(1000L);
    assertThat(matching.getStatus()).isEqualTo(MatchingStatus.CANCELLED);
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    verify(paymentService)
        .compensateCourier(COURIER_ID, "delivery-cancel-compensation:10", DELIVERY_ID, 1000L);
  }

  @Test
  @DisplayName("결제액이 1000P보다 작으면 음수 환불 없이 전액을 배송원 보상으로 사용한다")
  void lowPaidAmountNeverCreatesNegativeRefund() {
    DeliveryRequest delivery = paidDelivery(DeliveryStatus.MATCHED, 800L);
    when(deliveryRequestRepository.findById(DELIVERY_ID)).thenReturn(Optional.of(delivery));

    DeliveryCancellationPreviewResponse preview = service.preview(CUSTOMER_ID, DELIVERY_ID);

    assertThat(preview.cancellationFee()).isEqualTo(800L);
    assertThat(preview.refundAmount()).isZero();
    assertThat(preview.courierCompensation()).isEqualTo(800L);
  }

  @Test
  @DisplayName("픽업 이후 배송은 고객이 취소할 수 없다")
  void pickedUpDeliveryCannotBeCancelled() {
    DeliveryRequest delivery = paidDelivery(DeliveryStatus.PICKED_UP, 3000L);
    when(deliveryRequestRepository.findByIdForUpdate(DELIVERY_ID))
        .thenReturn(Optional.of(delivery));

    assertThatThrownBy(() -> service.cancel(CUSTOMER_ID, DELIVERY_ID))
        .isInstanceOf(InvalidDeliveryTransitionException.class);
    verifyNoInteractions(paymentService);
  }

  @Test
  @DisplayName("고객 본인이 아닌 사용자는 취소할 수 없다")
  void nonOwnerCannotCancelDelivery() {
    DeliveryRequest delivery = paidDelivery(DeliveryStatus.REQUESTED, 3000L);
    when(deliveryRequestRepository.findByIdForUpdate(DELIVERY_ID))
        .thenReturn(Optional.of(delivery));

    assertThatThrownBy(() -> service.cancel(999L, DELIVERY_ID))
        .isInstanceOf(DeliveryAccessDeniedException.class);
    verifyNoInteractions(paymentService);
  }

  @Test
  @DisplayName("환불 요청 금액은 클라이언트가 아니라 서버 결제액과 정책으로 계산한다")
  void refundAmountComesFromServerPolicy() {
    DeliveryRequest delivery = paidDelivery(DeliveryStatus.REQUESTED, 4321L);
    when(deliveryRequestRepository.findByIdForUpdate(DELIVERY_ID))
        .thenReturn(Optional.of(delivery));

    service.cancel(CUSTOMER_ID, DELIVERY_ID);

    ArgumentCaptor<PointRefundRequest> captor = ArgumentCaptor.forClass(PointRefundRequest.class);
    verify(paymentService).refundPoint(eq(CUSTOMER_ID), any(), captor.capture());
    assertThat(captor.getValue().getAmount()).isEqualTo(4321L);
  }

  private DeliveryRequest paidDelivery(DeliveryStatus status, long feePoint) {
    return DeliveryRequest.builder()
        .id(DELIVERY_ID)
        .memberId(CUSTOMER_ID)
        .status(status)
        .feePoint(feePoint)
        .paymentIdempotencyKey("payment-10")
        .build();
  }
}
