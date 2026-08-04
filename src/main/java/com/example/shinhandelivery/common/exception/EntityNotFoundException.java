package com.example.shinhandelivery.common.exception;

/**
 * 존재하지 않는 리소스/엔티티를 조회했을 때 발생하는 공통 예외 클래스입니다.
 *
 * <p>도메인별로 개별 XxxNotFoundException 클래스를 무분별하게 생성하지 않고, {@link ErrorCode}를 주입받아 정밀하게 에러 정보를 표현합니다.
 */
public class EntityNotFoundException extends BusinessException {

  public EntityNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }

  public EntityNotFoundException(ErrorCode errorCode, String detailMessage) {
    super(errorCode, detailMessage);
  }
}
