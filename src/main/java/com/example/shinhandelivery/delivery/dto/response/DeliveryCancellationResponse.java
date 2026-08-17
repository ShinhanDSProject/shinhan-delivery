package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import java.time.LocalDateTime;
import lombok.Builder;

/** 고객 취소 완료 후 상태와 실제 정산 금액을 반환한다. */
@Builder
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
    return DeliveryCancellationResponse.builder()
        .deliveryRequestId(deliveryRequest.getId())
        .previousStatus(deliveryRequest.getCancellationPreviousStatus())
        .cancellationReason(deliveryRequest.getCancellationReason())
        .paidAmount(deliveryRequest.getFeePoint())
        .cancellationFee(valueOrZero(deliveryRequest.getCancellationFee()))
        .refundAmount(valueOrZero(deliveryRequest.getRefundAmount()))
        .courierCompensation(valueOrZero(deliveryRequest.getCourierCompensation()))
        .cancelledAt(deliveryRequest.getCancelledAt())
        .build();
  }

  private static long valueOrZero(Long value) {
    return value == null ? 0L : value;
  }
}
