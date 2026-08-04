package com.example.shinhandelivery.delivery.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 배송 요청의 무게가 0 이하로 유효하지 않을 때 던진다. */
public class InvalidDeliveryWeightException extends BusinessException {

  public InvalidDeliveryWeightException(double weight) {
    super(ErrorCode.INVALID_DELIVERY_WEIGHT, "유효하지 않은 배송 무게입니다: " + weight);
  }
}
