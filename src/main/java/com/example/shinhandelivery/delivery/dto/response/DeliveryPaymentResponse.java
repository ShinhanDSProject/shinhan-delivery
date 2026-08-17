package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import lombok.Builder;

/** 배송 결제 완료 응답 DTO. */
@Builder
public record DeliveryPaymentResponse(
    Long deliveryRequestId,
    long paidAmount,
    long remainingBalance,
    DeliveryStatus deliveryStatus,
    String matchedStatus) {

  public static DeliveryPaymentResponse from(
      DeliveryRequest deliveryRequest, long paidAmount, long remainingBalance) {
    return DeliveryPaymentResponse.builder()
        .deliveryRequestId(deliveryRequest.getId())
        .paidAmount(paidAmount)
        .remainingBalance(remainingBalance)
        .deliveryStatus(deliveryRequest.getStatus())
        .matchedStatus("MATCHING")
        .build();
  }
}
