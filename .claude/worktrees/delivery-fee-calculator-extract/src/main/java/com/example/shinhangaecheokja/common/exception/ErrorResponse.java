package com.example.shinhandelivery.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/** 프론트엔드 및 클라이언트에 반환되는 전역 공통 에러 응답 DTO 클래스입니다. */
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

  private final int status;
  private final String code;
  private final String message;
  private final LocalDateTime timestamp;
  private final String traceId;
  private final List<FieldErrorDetail> errors;

  private ErrorResponse(ErrorCode errorCode, List<FieldErrorDetail> errors) {
    this.status = errorCode.getHttpStatus().value();
    this.code = errorCode.getCode();
    this.message = errorCode.getMessage();
    this.timestamp = LocalDateTime.now();
    this.traceId = MDC.get("traceId");
    this.errors = errors;
  }

  private ErrorResponse(ErrorCode errorCode, String customMessage) {
    this.status = errorCode.getHttpStatus().value();
    this.code = errorCode.getCode();
    this.message = customMessage != null ? customMessage : errorCode.getMessage();
    this.timestamp = LocalDateTime.now();
    this.traceId = MDC.get("traceId");
    this.errors = new ArrayList<>();
  }

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(errorCode, (List<FieldErrorDetail>) null);
  }

  public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
    return new ErrorResponse(errorCode, customMessage);
  }

  public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
    return new ErrorResponse(errorCode, FieldErrorDetail.of(bindingResult));
  }

  /** DTO 입출력 필드 검증 실패 시 세부 필드 오류 정보를 담는 정적 내부 클래스입니다. */
  @Getter
  public static class FieldErrorDetail {

    private final String field;
    private final String value;
    private final String reason;

    private FieldErrorDetail(String field, String value, String reason) {
      this.field = field;
      this.value = value;
      this.reason = reason;
    }

    public static List<FieldErrorDetail> of(BindingResult bindingResult) {
      List<FieldError> fieldErrors = bindingResult.getFieldErrors();
      return fieldErrors.stream()
          .map(
              error ->
                  new FieldErrorDetail(
                      error.getField(),
                      error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                      error.getDefaultMessage()))
          .collect(Collectors.toList());
    }
  }
}
