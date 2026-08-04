package com.example.shinhandelivery.delivery.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 배송 요청의 고객 본인 또는 배정된 배송원이 아닌 회원이 상세 조회를 시도할 때 던진다. */
public class DeliveryAccessDeniedException extends BusinessException {

  public DeliveryAccessDeniedException(Long deliveryRequestId, Long memberId) {
    super(
        ErrorCode.ACCESS_DENIED,
        "해당 배송 요청에 접근할 권한이 없습니다: deliveryRequestId="
            + deliveryRequestId
            + ", memberId="
            + memberId);
  }
}
