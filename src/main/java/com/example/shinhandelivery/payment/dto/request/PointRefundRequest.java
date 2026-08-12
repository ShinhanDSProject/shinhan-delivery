package com.example.shinhandelivery.payment.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 시스템이 결제 포인트를 원거래 참조와 함께 반환할 때 사용하는 내부 요청 DTO. */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PointRefundRequest {

  private long amount;
  private Long referenceId;
  private String description;

  public static PointRefundRequest of(long amount, Long referenceId, String description) {
    return new PointRefundRequest(amount, referenceId, description);
  }
}
