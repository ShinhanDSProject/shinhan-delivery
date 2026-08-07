package com.example.shinhandelivery.realtime.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/**
 * 본인이 아닌 회원의 알림 채널을 구독하려 할 때 던진다. 이 메시지는 STOMP ERROR 프레임으로 그대로 클라이언트에 전달될 수 있으므로, 회원 식별자(memberId)는
 * 담지 않는다.
 */
public class UnauthorizedNotificationAccessException extends BusinessException {

  public UnauthorizedNotificationAccessException() {
    super(ErrorCode.UNAUTHORIZED, "해당 알림 채널에 접근할 권한이 없습니다.");
  }
}
