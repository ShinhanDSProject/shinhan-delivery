package com.example.shinhangaecheokja.delivery.dto.response;

import java.math.BigDecimal;

/** 배송 요금 견적 응답 DTO. */
public record DeliveryEstimateResponse(
    BigDecimal baseFee,
    BigDecimal distanceSurcharge,
    BigDecimal weightSurcharge,
    BigDecimal sizeSurcharge,
    BigDecimal totalFee) {}
