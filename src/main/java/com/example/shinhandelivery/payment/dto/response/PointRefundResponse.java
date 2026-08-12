package com.example.shinhandelivery.payment.dto.response;

import java.time.LocalDateTime;

/** 멱등 포인트 환불 결과 DTO. */
public record PointRefundResponse(long balance, long refundedAmount, LocalDateTime refundedAt) {}
