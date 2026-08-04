package com.example.shinhandelivery.realtime.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/**
 * 차량 소유주가 아닌 회원이 그 차량의 오퍼 채널을 구독하려 할 때 던진다. 이 메시지는 STOMP ERROR 프레임으로 그대로 클라이언트에 전달될 수 있으므로, 회원
 * 식별자(memberId)는 담지 않는다.
 */
public class UnauthorizedOfferAccessException extends BusinessException {

  public UnauthorizedOfferAccessException(Long vehicleId) {
    super(ErrorCode.UNAUTHORIZED, "해당 차량의 오퍼 채널에 접근할 권한이 없습니다: vehicleId=" + vehicleId);
  }
}
