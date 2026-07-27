package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class DeliveryRequestNotFoundException extends BusinessException {

  public DeliveryRequestNotFoundException() {
    super(ErrorCode.DELIVERY_NOT_FOUND);
  }

  public DeliveryRequestNotFoundException(Long id) {
    super(ErrorCode.DELIVERY_NOT_FOUND, "존재하지 않는 배송 요청입니다. (ID: " + id + ")");
  }

  public DeliveryRequestNotFoundException(String message) {
    super(ErrorCode.DELIVERY_NOT_FOUND, message);
  }
}
