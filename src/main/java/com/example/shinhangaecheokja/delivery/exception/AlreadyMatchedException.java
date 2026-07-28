package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class AlreadyMatchedException extends BusinessException {

  public AlreadyMatchedException() {
    super(ErrorCode.INVALID_INPUT_VALUE, "이미 배달 매치가 완료된 요청입니다.");
  }

  public AlreadyMatchedException(Long id) {
    super(ErrorCode.INVALID_INPUT_VALUE, "이미 배달 매치가 완료된 요청입니다. (ID: " + id + ")");
  }

  public AlreadyMatchedException(String message) {
    super(ErrorCode.INVALID_INPUT_VALUE, message);
  }
}
