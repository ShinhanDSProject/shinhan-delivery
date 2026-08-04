package com.example.shinhandelivery.delivery.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;

/** 허용되지 않는 배송 상태 전이를 시도할 때 던진다. */
public class InvalidDeliveryTransitionException extends BusinessException {

  public InvalidDeliveryTransitionException(DeliveryStatus from, DeliveryStatus to) {
    super(ErrorCode.INVALID_DELIVERY_TRANSITION, "허용되지 않는 배송 상태 전이입니다: " + from + " -> " + to);
  }
}
