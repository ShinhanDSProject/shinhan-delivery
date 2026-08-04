package com.example.shinhandelivery.notification.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 알림의 소유자가 아닌 회원이 그 알림을 읽음 처리하려 할 때 던진다. */
public class NotificationAccessDeniedException extends BusinessException {

  public NotificationAccessDeniedException(Long notificationId, Long memberId) {
    super(
        ErrorCode.ACCESS_DENIED,
        "해당 알림에 접근할 권한이 없습니다: notificationId=" + notificationId + ", memberId=" + memberId);
  }
}
