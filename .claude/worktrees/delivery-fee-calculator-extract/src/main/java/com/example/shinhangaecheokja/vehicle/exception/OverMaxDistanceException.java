package com.example.shinhandelivery.vehicle.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

public class OverMaxDistanceException extends BusinessException {

  public OverMaxDistanceException() {
    super(ErrorCode.INVALID_INPUT_VALUE, "차량 운행 가능 거리를 초과했습니다.");
  }

  public OverMaxDistanceException(double distance) {
    super(ErrorCode.INVALID_INPUT_VALUE, "차량 운행 가능 거리를 초과했습니다. (요청 거리: " + distance + "km)");
  }

  public OverMaxDistanceException(String message) {
    super(ErrorCode.INVALID_INPUT_VALUE, message);
  }
}
