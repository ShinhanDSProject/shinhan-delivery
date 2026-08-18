package com.example.shinhandelivery.payment.dto.response;

import com.example.shinhandelivery.payment.entity.PaymentMethod;
import com.example.shinhandelivery.payment.entity.PointHistory;
import com.example.shinhandelivery.payment.entity.PointHistoryType;
import java.time.LocalDateTime;
import lombok.Builder;

/** 포인트 지갑 최근 이력 1건 응답 DTO. */
@Builder
public record PointHistoryItemResponse(
    Long historyId,
    PointHistoryType type,
    long amount,
    long balanceAfter,
    PaymentMethod paymentMethod,
    Long referenceId,
    String description,
    LocalDateTime createdAt) {

  public static PointHistoryItemResponse from(PointHistory history) {
    return PointHistoryItemResponse.builder()
        .historyId(history.getId())
        .type(history.getType())
        .amount(history.getAmount())
        .balanceAfter(history.getBalanceAfter())
        .paymentMethod(history.getPaymentMethod())
        .referenceId(history.getReferenceId())
        .description(history.getDescription())
        .createdAt(history.getCreatedAt())
        .build();
  }
}
