package com.example.shinhandelivery.delivery.dto.response;

import java.math.BigDecimal;
import lombok.Builder;

/** 배송 요금 견적 응답 DTO. */
@Builder
public record DeliveryEstimateResponse(
    BigDecimal baseFee,
    BigDecimal distanceSurcharge,
    BigDecimal weightSurcharge,
    BigDecimal sizeSurcharge,
    BigDecimal totalFee) {

  public static DeliveryEstimateResponse of(
      BigDecimal baseFee,
      BigDecimal distanceSurcharge,
      BigDecimal weightSurcharge,
      BigDecimal sizeSurcharge,
      BigDecimal totalFee) {
    return DeliveryEstimateResponse.builder()
        .baseFee(baseFee)
        .distanceSurcharge(distanceSurcharge)
        .weightSurcharge(weightSurcharge)
        .sizeSurcharge(sizeSurcharge)
        .totalFee(totalFee)
        .build();
  }
}
