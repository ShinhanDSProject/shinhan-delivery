package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class NoAvailableCourierException extends BusinessException {

  public NoAvailableCourierException() {
    super(ErrorCode.ENTITY_NOT_FOUND, "배송 가능한 라이더가 없습니다.");
  }

  public NoAvailableCourierException(double weight, double distance) {
    super(
        ErrorCode.ENTITY_NOT_FOUND,
        "조건에 맞는 배송 라이더가 없습니다. (중량: " + weight + "kg, 거리: " + distance + "km)");
  }

  public NoAvailableCourierException(String message) {
    super(ErrorCode.ENTITY_NOT_FOUND, message);
  }
}
