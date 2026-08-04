package com.example.shinhandelivery.delivery.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;

/** 이미 매칭되었거나 완료·취소되어 더 이상 매칭 대상이 될 수 없는 배송 요청에 접근할 때 던진다. */
public class AlreadyMatchedException extends BusinessException {

  public AlreadyMatchedException(Long deliveryRequestId, DeliveryStatus status) {
    super(ErrorCode.ALREADY_MATCHED, describe(status) + " 배송 요청입니다: " + deliveryRequestId);
  }

  /** REQUESTED는 이 예외가 던져지는 정상 경로에서는 나오지 않지만, switch 표현식 완전성을 위해 방어적으로 처리한다. */
  private static String describe(DeliveryStatus status) {
    return switch (status) {
      case MATCHED -> "이미 매칭된";
      case PICKED_UP -> "이미 픽업된";
      case COMPLETED -> "이미 완료된";
      case CANCELLED -> "이미 취소된";
      case REQUESTED -> "매칭 대기 중인";
    };
  }
}
