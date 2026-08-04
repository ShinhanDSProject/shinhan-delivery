package com.example.shinhandelivery.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 수행 중 발생하는 예외의 최상위 부모 예외 클래스입니다.
 *
 * <p>모든 도메인 예외는 이 클래스를 상속받거나, 이 클래스 인스턴스에 {@link ErrorCode}를 주입하여 발생시킵니다.
 */
@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCode errorCode, String detailMessage) {
    super(detailMessage);
    this.errorCode = errorCode;
  }
}
