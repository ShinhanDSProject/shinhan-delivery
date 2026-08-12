package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import java.time.LocalDateTime;

/** 고객 취소 완료 후 상태와 실제 정산 금액을 반환한다. */
public record DeliveryCancellationResponse(
    Long deliveryRequestId,
    DeliveryStatus previousStatus,
    DeliveryCancellationReason cancellationReason,
    long paidAmount,
    long cancellationFee,
    long refundAmount,
    long courierCompensation,
    LocalDateTime cancelledAt) {

  public static DeliveryCancellationResponse from(DeliveryRequest deliveryRequest) {
    return new DeliveryCancellationResponse(
        deliveryRequest.getId(),
        deliveryRequest.getCancellationPreviousStatus(),
        deliveryRequest.getCancellationReason(),
        deliveryRequest.getFeePoint(),
        valueOrZero(deliveryRequest.getCancellationFee()),
        valueOrZero(deliveryRequest.getRefundAmount()),
        valueOrZero(deliveryRequest.getCourierCompensation()),
        deliveryRequest.getCancelledAt());
  }

  private static long valueOrZero(Long value) {
    return value == null ? 0L : value;
  }
}
