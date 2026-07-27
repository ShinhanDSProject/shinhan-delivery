package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class MatchingNotFoundException extends BusinessException {

  public MatchingNotFoundException() {
    super(ErrorCode.DELIVERY_NOT_FOUND);
  }

  public MatchingNotFoundException(Long id) {
    super(ErrorCode.DELIVERY_NOT_FOUND, "존재하지 않는 배송 매칭 정보입니다. (ID: " + id + ")");
  }

  public MatchingNotFoundException(String message) {
    super(ErrorCode.DELIVERY_NOT_FOUND, message);
  }
}
