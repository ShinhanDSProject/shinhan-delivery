package com.example.shinhandelivery.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전역에서 사용하는 표준 에러 코드 정의 Enum입니다.
 *
 * <p>HTTP 상태 코드, 시스템 고유 에러 코드, 사용자 안내 메시지를 일관성 있게 관리합니다.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
  // Common Errors
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력값입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "존재하지 않는 리소스입니다."),
  INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "C005", "허용되지 않는 파일 형식입니다."),
  FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "C006", "파일 크기가 허용 범위를 초과했습니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "C007", "접근 권한이 없습니다."),

  // Member Domain Errors
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M002", "이미 가입된 이메일 주소입니다."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "M003", "이메일 또는 비밀번호가 일치하지 않습니다."),
  CURRENT_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "M004", "현재 비밀번호가 일치하지 않습니다."),
  PASSWORD_CONFIRMATION_MISMATCH(HttpStatus.BAD_REQUEST, "M005", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."),
  PASSWORD_REUSE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "M006", "현재 비밀번호와 다른 비밀번호를 입력해야 합니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증 권한이 필요합니다."),

  // Vehicle Domain Errors
  VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "존재하지 않는 차량입니다."),

  // Delivery Domain Errors
  DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "존재하지 않는 배송 요청입니다."),
  MATCHING_NOT_FOUND(HttpStatus.NOT_FOUND, "D002", "존재하지 않는 매칭입니다."),
  ALREADY_MATCHED(HttpStatus.CONFLICT, "D003", "이미 처리된 배송 요청입니다."),
  INVALID_DELIVERY_WEIGHT(HttpStatus.BAD_REQUEST, "D004", "유효하지 않은 배송 무게입니다."),
  INVALID_DELIVERY_DISTANCE(HttpStatus.BAD_REQUEST, "D005", "유효하지 않은 배송 거리입니다."),
  INVALID_MATCHING_TRANSITION(HttpStatus.CONFLICT, "D006", "허용되지 않는 매칭 상태 전이입니다."),
  INVALID_DELIVERY_TRANSITION(HttpStatus.CONFLICT, "D007", "허용되지 않는 배송 상태 전이입니다."),
  PROOF_PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "D008", "완료 증거 사진을 찾을 수 없습니다."),

  // Vehicle Domain Errors (continued)
  VEHICLE_CAPACITY_MISMATCH(HttpStatus.BAD_REQUEST, "V002", "차량이 배송 조건을 감당할 수 없습니다."),
  VEHICLE_NOT_AVAILABLE(HttpStatus.CONFLICT, "V003", "이미 배정되어 사용할 수 없는 차량입니다."),

  // Payment Domain Errors
  POINT_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "포인트 지갑 정보를 찾을 수 없습니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "P002", "포인트 잔액이 부족합니다."),
  INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "P003", "포인트 금액은 0보다 커야 합니다."),
  POINT_BALANCE_OVERFLOW(HttpStatus.BAD_REQUEST, "P004", "포인트 잔액 한도를 초과했습니다."),

  // Notification Domain Errors
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "존재하지 않는 알림입니다."),

  // Notice Domain Errors
  NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "N002", "존재하지 않는 공지사항입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
