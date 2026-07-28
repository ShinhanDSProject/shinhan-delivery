package com.example.shinhangaecheokja.vehicle.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

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
