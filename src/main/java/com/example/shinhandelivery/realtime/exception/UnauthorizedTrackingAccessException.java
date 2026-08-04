package com.example.shinhandelivery.realtime.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 배송의 고객 본인이나 매칭된 차량 소유주가 아닌 회원이 위치 추적 채널을 구독·발행하려 할 때 던진다. */
public class UnauthorizedTrackingAccessException extends BusinessException {

  public UnauthorizedTrackingAccessException(Long deliveryId, Long memberId) {
    super(
        ErrorCode.UNAUTHORIZED,
        "해당 배송의 위치 추적 채널에 접근할 권한이 없습니다: deliveryId=" + deliveryId + ", memberId=" + memberId);
  }
}
