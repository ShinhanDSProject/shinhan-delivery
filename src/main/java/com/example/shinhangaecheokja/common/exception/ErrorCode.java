package com.example.shinhangaecheokja.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전역에서 사용하는 표준 에러 코드 정의 Enum입니다.
 *
 * <p>HTTP 상태 코드, 시스템 고유 에러 코드, 사용자 안내 메시지를 일관성 있게 관리합니다.
 */
public enum ErrorCode {
  // Common Errors
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력값입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "존재하지 않는 리소스입니다."),

  // Member Domain Errors
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M002", "이미 가입된 이메일 주소입니다."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "M003", "이메일 또는 비밀번호가 일치하지 않습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증 권한이 필요합니다."),

  // Vehicle Domain Errors
  VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "존재하지 않는 차량입니다."),

  // Delivery Domain Errors
  DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "존재하지 않는 배송 요청입니다."),

  // Payment Domain Errors
  POINT_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "포인트 지갑 정보를 찾을 수 없습니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "P002", "포인트 잔액이 부족합니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

  ErrorCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
