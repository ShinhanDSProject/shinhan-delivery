package com.example.shinhandelivery.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/** 애플리케이션 전역에서 발생하는 예외를 포착하여 표준 에러 응답 객체(ErrorResponse)로 변환하는 컨트롤러 어드바이스입니다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** 비즈니스 로직 수행 중 발생하는 비즈니스 예외(BusinessException)를 처리합니다. */
  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("BusinessException occurred: [{}] {}", errorCode.getCode(), e.getMessage());
    ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** DTO @Valid 입출력 유효성 검증 실패 예외(MethodArgumentNotValidException)를 처리합니다. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.info(
        "MethodArgumentNotValidException occurred: fieldErrorCount={}",
        e.getBindingResult().getFieldErrorCount());
    ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
    ErrorResponse response = ErrorResponse.of(errorCode, e.getBindingResult());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** 지원하지 않는 HTTP 메서드 호출 예외(HttpRequestMethodNotSupportedException)를 처리합니다. */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    log.warn("HttpRequestMethodNotSupportedException occurred: {}", e.getMessage());
    ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
    ErrorResponse response = ErrorResponse.of(errorCode);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** 클라이언트의 요청 본문 JSON 형식이 올바르지 않은 예외(HttpMessageNotReadableException)를 처리합니다. */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    log.info("HttpMessageNotReadableException occurred");
    ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
    ErrorResponse response = ErrorResponse.of(errorCode, "요청 본문의 JSON 형식이 올바르지 않습니다.");
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /**
   * {@code @PreAuthorize} 인가 실패 예외(AccessDeniedException 및 그 하위 타입인 AuthorizationDeniedException)를
   * 처리합니다. 메서드 시큐리티 예외는 Spring Security의 필터가 아니라 이 {@code @RestControllerAdvice}(DispatcherServlet
   * 내부)에서 먼저 잡히므로, 여기서 직접 403으로 변환해야 한다.
   */
  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
    log.warn("AccessDeniedException occurred: {}", e.getMessage());
    ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
    ErrorResponse response = ErrorResponse.of(errorCode);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /**
   * 존재하지 않는 API 경로 요청 예외(NoHandlerFoundException)를 처리합니다.
   *
   * <p>DispatcherServlet이 요청에 매핑된 핸들러를 찾지 못할 때 발생하며, 404 응답으로 변환합니다.
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
    log.warn("NoHandlerFoundException occurred: {} {}", e.getHttpMethod(), e.getRequestURL());
    ErrorCode errorCode = ErrorCode.ENTITY_NOT_FOUND;
    ErrorResponse response = ErrorResponse.of(errorCode);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /**
   * {@code @PathVariable}, {@code @RequestParam} 제약 조건 위반 예외(ConstraintViolationException)를 처리합니다.
   *
   * <p>{@code @Validated} + Bean Validation 제약 조건 위반 시 발생하며, 400 응답으로 변환합니다.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException e) {
    log.warn("ConstraintViolationException occurred: {}", e.getMessage());
    ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
    ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** 기타 미처 처리하지 못한 모든 런타임/서버 예외(Exception)를 포착하여 500 응답으로 변환합니다. */
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Unhandled Exception occurred: ", e);
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    ErrorResponse response = ErrorResponse.of(errorCode);
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
