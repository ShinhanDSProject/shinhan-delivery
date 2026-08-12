package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.payment.dto.request.PointRefundRequest;
import com.example.shinhandelivery.payment.dto.response.PointRefundResponse;
import com.example.shinhandelivery.payment.service.PaymentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DeliveryTimeoutServiceTest {

  private static final LocalDateTime PROCESSED_AT = LocalDateTime.of(2026, 8, 11, 16, 0);
  private static final LocalDateTime CUTOFF = PROCESSED_AT.minusMinutes(30);

  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private PaymentService paymentService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private DeliveryTimeoutService deliveryTimeoutService;

  @Test
  @DisplayName("만료 후보는 설정된 배치 크기와 경계 시각으로 조회한다")
  void listTimedOutCandidateIdsUsesCutoffAndBatchSize() {
    when(deliveryRequestRepository.findTimedOutCandidateIds(
            eq(DeliveryStatus.REQUESTED),
            eq(CUTOFF),
            any(LocalDateTime.class),
            any(Pageable.class)))
        .thenReturn(List.of(1L, 2L));

    List<Long> result = deliveryTimeoutService.listTimedOutCandidateIds(CUTOFF, 100);

    assertThat(result).containsExactly(1L, 2L);
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(deliveryRequestRepository)
        .findTimedOutCandidateIds(
            eq(DeliveryStatus.REQUESTED),
            eq(CUTOFF),
            any(LocalDateTime.class),
            pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
  }

  @Test
  @DisplayName("30분 경계의 결제된 미배정 배송은 전액 환불 후 자동 취소한다")
  void expirePaidDeliveryAtBoundaryRefundsAndCancels() {
    DeliveryRequest deliveryRequest = paidRequestedDelivery(CUTOFF);
    when(deliveryRequestRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(deliveryRequest));
    when(paymentService.refundPoint(eq(1L), eq("delivery-timeout-refund:55"), any()))
        .thenReturn(new PointRefundResponse(3000L, 3000L, PROCESSED_AT));

    boolean expired = deliveryTimeoutService.expireTimedOutDelivery(55L, CUTOFF, PROCESSED_AT);

    assertThat(expired).isTrue();
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
    assertThat(deliveryRequest.getCancellationReason())
        .isEqualTo(DeliveryCancellationReason.AUTO_TIMEOUT);
    assertThat(deliveryRequest.getCancelledAt()).isEqualTo(PROCESSED_AT);
    assertThat(deliveryRequest.getRefundedAt()).isEqualTo(PROCESSED_AT);

    ArgumentCaptor<PointRefundRequest> refundCaptor =
        ArgumentCaptor.forClass(PointRefundRequest.class);
    verify(paymentService)
        .refundPoint(eq(1L), eq("delivery-timeout-refund:55"), refundCaptor.capture());
    assertThat(refundCaptor.getValue().getAmount()).isEqualTo(3000L);
    assertThat(refundCaptor.getValue().getReferenceId()).isEqualTo(55L);
    verify(eventPublisher).publishEvent(any(DeliveryStatusChangedEvent.class));
  }

  @Test
  @DisplayName("결제되지 않은 만료 배송은 포인트 환불 없이 자동 취소한다")
  void expireUnpaidDeliveryCancelsWithoutRefund() {
    DeliveryRequest deliveryRequest = paidRequestedDelivery(CUTOFF.minusSeconds(1));
    deliveryRequest.setPaymentIdempotencyKey(null);
    when(deliveryRequestRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(deliveryRequest));

    boolean expired = deliveryTimeoutService.expireTimedOutDelivery(55L, CUTOFF, PROCESSED_AT);

    assertThat(expired).isTrue();
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
    assertThat(deliveryRequest.getRefundedAt()).isNull();
    verifyNoInteractions(paymentService);
  }

  @Test
  @DisplayName("30분이 지나지 않은 배송은 취소하거나 환불하지 않는다")
  void recentDeliveryIsSkipped() {
    DeliveryRequest deliveryRequest = paidRequestedDelivery(CUTOFF.plusNanos(1));
    when(deliveryRequestRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(deliveryRequest));

    boolean expired = deliveryTimeoutService.expireTimedOutDelivery(55L, CUTOFF, PROCESSED_AT);

    assertThat(expired).isFalse();
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    verifyNoInteractions(paymentService, eventPublisher);
  }

  @Test
  @DisplayName("REQUESTED가 아닌 모든 배송 상태는 자동 취소 대상에서 제외한다")
  void nonRequestedStatusesAreSkipped() {
    DeliveryRequest deliveryRequest = paidRequestedDelivery(CUTOFF.minusMinutes(1));
    when(deliveryRequestRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(deliveryRequest));

    for (DeliveryStatus status :
        List.of(
            DeliveryStatus.MATCHED,
            DeliveryStatus.PICKED_UP,
            DeliveryStatus.COMPLETED,
            DeliveryStatus.CANCELLED)) {
      deliveryRequest.setStatus(status);
      assertThat(deliveryTimeoutService.expireTimedOutDelivery(55L, CUTOFF, PROCESSED_AT))
          .isFalse();
    }

    verifyNoInteractions(paymentService, eventPublisher);
  }

  @Test
  @DisplayName("환불이 실패하면 배송 상태를 취소로 변경하거나 이벤트를 발행하지 않는다")
  void refundFailureDoesNotChangeDelivery() {
    DeliveryRequest deliveryRequest = paidRequestedDelivery(CUTOFF.minusMinutes(1));
    when(deliveryRequestRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(deliveryRequest));
    when(paymentService.refundPoint(eq(1L), eq("delivery-timeout-refund:55"), any()))
        .thenThrow(new IllegalStateException("refund failed"));

    assertThatThrownBy(
            () -> deliveryTimeoutService.expireTimedOutDelivery(55L, CUTOFF, PROCESSED_AT))
        .isInstanceOf(IllegalStateException.class);

    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("타임아웃 실패는 5분 뒤 재시도하도록 별도 상태를 기록한다")
  void scheduleRetryAfterFailureAppliesBackoff() {
    DeliveryRequest deliveryRequest = paidRequestedDelivery(CUTOFF.minusMinutes(1));
    when(deliveryRequestRepository.findByIdForUpdate(55L)).thenReturn(Optional.of(deliveryRequest));

    deliveryTimeoutService.scheduleRetryAfterFailure(55L, PROCESSED_AT);

    assertThat(deliveryRequest.getTimeoutRetryCount()).isEqualTo(1);
    assertThat(deliveryRequest.getTimeoutNextRetryAt()).isEqualTo(PROCESSED_AT.plusMinutes(5));
  }

  private DeliveryRequest paidRequestedDelivery(LocalDateTime createdAt) {
    return DeliveryRequest.builder()
        .id(55L)
        .memberId(1L)
        .status(DeliveryStatus.REQUESTED)
        .feePoint(3000L)
        .paymentIdempotencyKey("payment-55")
        .createdAt(createdAt)
        .build();
  }
}
