package com.example.shinhangaecheokja.delivery.dto.response;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;

/** 배송 결제 완료 응답 DTO. */
public record DeliveryPaymentResponse(
    Long deliveryRequestId,
    long paidAmount,
    long remainingBalance,
    DeliveryStatus deliveryStatus,
    String matchedStatus) {

  public static DeliveryPaymentResponse from(
      DeliveryRequest deliveryRequest, long paidAmount, long remainingBalance) {
    return new DeliveryPaymentResponse(
        deliveryRequest.getId(),
        paidAmount,
        remainingBalance,
        deliveryRequest.getStatus(),
        "MATCHING");
  }
}
