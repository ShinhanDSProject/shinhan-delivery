package com.example.shinhandelivery.vehicle.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

public class InvalidWeightException extends BusinessException {

  public InvalidWeightException() {
    super(ErrorCode.INVALID_INPUT_VALUE, "차량 최대 적재 중량을 초과했습니다.");
  }

  public InvalidWeightException(double weight) {
    super(ErrorCode.INVALID_INPUT_VALUE, "차량 최대 적재 중량을 초과했습니다. (요청 중량: " + weight + "kg)");
  }

  public InvalidWeightException(String message) {
    super(ErrorCode.INVALID_INPUT_VALUE, message);
  }
}
