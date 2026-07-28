package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

/** 배송 요청의 거리가 0 이하로 유효하지 않을 때 던진다. */
public class InvalidDeliveryDistanceException extends BusinessException {

  public InvalidDeliveryDistanceException(double distance) {
    super(ErrorCode.INVALID_DELIVERY_DISTANCE, "유효하지 않은 배송 거리입니다: " + distance);
  }
}
