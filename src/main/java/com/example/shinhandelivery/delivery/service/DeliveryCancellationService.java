package com.example.shinhandelivery.delivery.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.response.DeliveryCancellationPreviewResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryCancellationResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.payment.dto.request.PointRefundRequest;
import com.example.shinhandelivery.payment.service.PaymentService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 배송 상태별 고객 취소 수수료, 환불, 배송원 이동 보상을 하나의 트랜잭션으로 처리한다. */
@Service
@RequiredArgsConstructor
public class DeliveryCancellationService {

  private static final long MATCHED_CANCELLATION_FEE = 1000L;
  private static final String REFUND_KEY_PREFIX = "delivery-cancel-refund:";
  private static final String COMPENSATION_KEY_PREFIX = "delivery-cancel-compensation:";

  private final DeliveryRequestRepository deliveryRequestRepository;
  private final MatchingRepository matchingRepository;
  private final VehicleService vehicleService;
  private final PaymentService paymentService;
  private final ApplicationEventPublisher eventPublisher;

  /** 현재 상태를 기준으로 취소 수수료와 예상 환불·보상액을 계산한다. */
  @Transactional(readOnly = true)
  public DeliveryCancellationPreviewResponse preview(Long customerId, Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    assertOwner(customerId, deliveryRequest);
    Settlement settlement = calculateSettlement(deliveryRequest);
    return new DeliveryCancellationPreviewResponse(
        deliveryRequestId,
        deliveryRequest.getStatus(),
        settlement.paidAmount(),
        settlement.cancellationFee(),
        settlement.refundAmount(),
        settlement.courierCompensation());
  }

  /** 배송 행을 잠근 뒤 고객 환불과 배송원 보상, 매칭·차량·배송 상태를 원자적으로 갱신한다. */
  @Transactional
  public DeliveryCancellationResponse cancel(Long customerId, Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestForUpdateOrThrow(deliveryRequestId);
    assertOwner(customerId, deliveryRequest);
    if (deliveryRequest.getStatus() == DeliveryStatus.CANCELLED
        && deliveryRequest.getCancellationReason() == DeliveryCancellationReason.CUSTOMER_REQUEST) {
      return DeliveryCancellationResponse.from(deliveryRequest);
    }

    DeliveryStatus previousStatus = deliveryRequest.getStatus();
    Settlement settlement = calculateSettlement(deliveryRequest);
    Matching matching = null;
    Long courierId = null;
    if (previousStatus == DeliveryStatus.MATCHED) {
      matching =
          matchingRepository
              .findByDeliveryRequestIdForUpdate(deliveryRequestId)
              .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATCHING_NOT_FOUND));
      Vehicle vehicle = vehicleService.getVehicleForUpdate(matching.getVehicleId());
      courierId = vehicle.getMemberId();
      vehicle.markAs(VehicleStatus.AVAILABLE);
    }

    settlePoints(deliveryRequest, courierId, settlement);
    LocalDateTime processedAt = LocalDateTime.now();
    if (matching != null) {
      matching.changeStatus(MatchingStatus.CANCELLED);
    }
    deliveryRequest.cancelByCustomer(
        customerId,
        previousStatus,
        settlement.cancellationFee(),
        settlement.refundAmount(),
        settlement.courierCompensation(),
        processedAt);
    eventPublisher.publishEvent(
        new DeliveryStatusChangedEvent(deliveryRequestId, DeliveryStatus.CANCELLED, processedAt));
    return DeliveryCancellationResponse.from(deliveryRequest);
  }

  private void settlePoints(
      DeliveryRequest deliveryRequest, Long courierId, Settlement settlement) {
    if (settlement.refundAmount() > 0) {
      paymentService.refundPoint(
          deliveryRequest.getMemberId(),
          REFUND_KEY_PREFIX + deliveryRequest.getId(),
          PointRefundRequest.of(
              settlement.refundAmount(), deliveryRequest.getId(), "고객 요청 배송 취소 환불"));
    }
    if (settlement.courierCompensation() > 0) {
      paymentService.compensateCourier(
          courierId,
          COMPENSATION_KEY_PREFIX + deliveryRequest.getId(),
          deliveryRequest.getId(),
          settlement.courierCompensation());
    }
  }

  private Settlement calculateSettlement(DeliveryRequest deliveryRequest) {
    DeliveryStatus status = deliveryRequest.getStatus();
    if (status != DeliveryStatus.REQUESTED && status != DeliveryStatus.MATCHED) {
      throw new InvalidDeliveryTransitionException(status, DeliveryStatus.CANCELLED);
    }
    long paidAmount =
        deliveryRequest.getPaymentIdempotencyKey() == null ? 0L : deliveryRequest.getFeePoint();
    long cancellationFee =
        status == DeliveryStatus.MATCHED ? Math.min(MATCHED_CANCELLATION_FEE, paidAmount) : 0L;
    return new Settlement(
        paidAmount, cancellationFee, paidAmount - cancellationFee, cancellationFee);
  }

  private void assertOwner(Long customerId, DeliveryRequest deliveryRequest) {
    if (!customerId.equals(deliveryRequest.getMemberId())) {
      throw new DeliveryAccessDeniedException(deliveryRequest.getId(), customerId);
    }
  }

  private DeliveryRequest findDeliveryRequestOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findById(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }

  private DeliveryRequest findDeliveryRequestForUpdateOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findByIdForUpdate(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }

  private record Settlement(
      long paidAmount, long cancellationFee, long refundAmount, long courierCompensation) {}
}
