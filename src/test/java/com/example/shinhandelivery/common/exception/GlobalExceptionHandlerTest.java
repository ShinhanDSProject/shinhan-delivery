package com.example.shinhandelivery.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.NoHandlerFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  @DisplayName("MdcLoggingFilter가 주입한 traceId가 ErrorResponse에 그대로 담긴다.")
  void handleBusinessExceptionIncludesTraceIdFromMdc() {
    // given
    MDC.put("traceId", "test-trace-id");
    BusinessException exception = new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

    // when
    ResponseEntity<ErrorResponse> responseEntity = handler.handleBusinessException(exception);

    // then
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getTraceId()).isEqualTo("test-trace-id");
  }

  @Test
  @DisplayName("비즈니스 예외(BusinessException) 발생 시 설정된 HTTP 상태와 ErrorCode 정보가 반환된다.")
  void handleBusinessExceptionReturnsCorrectStatusAndResponseBody() {
    // given
    BusinessException exception = new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

    // when
    ResponseEntity<ErrorResponse> responseEntity = handler.handleBusinessException(exception);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getStatus()).isEqualTo(404);
    assertThat(body.getCode()).isEqualTo("M001");
    assertThat(body.getMessage()).isEqualTo("존재하지 않는 회원입니다.");
  }

  @Test
  @DisplayName("409 Conflict BusinessException 발생 시 상태 409와 M002 에러 코드가 반환된다.")
  void handleBusinessExceptionConflictReturns409() {
    // given
    BusinessException exception = new BusinessException(ErrorCode.DUPLICATE_EMAIL);

    // when
    ResponseEntity<ErrorResponse> responseEntity = handler.handleBusinessException(exception);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getStatus()).isEqualTo(409);
    assertThat(body.getCode()).isEqualTo("M002");
  }

  @Test
  @DisplayName("AccessDeniedException 발생 시 403 상태와 C007 에러 코드가 반환된다.")
  void handleAccessDeniedExceptionReturns403() {
    // given
    AccessDeniedException exception = new AccessDeniedException("접근 거부");

    // when
    ResponseEntity<ErrorResponse> responseEntity = handler.handleAccessDeniedException(exception);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getStatus()).isEqualTo(403);
    assertThat(body.getCode()).isEqualTo("C007");
  }

  @Test
  @DisplayName("존재하지 않는 경로 요청 시(NoHandlerFoundException) 404 상태와 C004 에러 코드가 반환된다.")
  void handleNoHandlerFoundExceptionReturns404() {
    // given
    NoHandlerFoundException exception = new NoHandlerFoundException("GET", "/api/unknown", null);

    // when
    ResponseEntity<ErrorResponse> responseEntity = handler.handleNoHandlerFoundException(exception);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getStatus()).isEqualTo(404);
    assertThat(body.getCode()).isEqualTo("C004");
  }

  @Test
  @DisplayName("존재하지 않는 정적 리소스 요청 시(NoResourceFoundException) 404 상태와 C004 에러 코드가 반환된다.")
  void handleNoResourceFoundExceptionReturns404() {
    // given
    org.springframework.web.servlet.resource.NoResourceFoundException exception =
        new org.springframework.web.servlet.resource.NoResourceFoundException(
            org.springframework.http.HttpMethod.GET, "unknown.js", "Static resource not found");

    // when
    ResponseEntity<ErrorResponse> responseEntity =
        handler.handleNoResourceFoundException(exception);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getStatus()).isEqualTo(404);
    assertThat(body.getCode()).isEqualTo("C004");
  }

  @Test
  @DisplayName("ConstraintViolationException 발생 시 400 상태와 C001 에러 코드가 반환된다.")
  void handleConstraintViolationExceptionReturns400() {
    // given
    ConstraintViolationException exception =
        new ConstraintViolationException("유효하지 않은 입력값", Set.of());

    // when
    ResponseEntity<ErrorResponse> responseEntity =
        handler.handleConstraintViolationException(exception);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getStatus()).isEqualTo(400);
    assertThat(body.getCode()).isEqualTo("C001");
  }

  @Test
  @DisplayName("알 수 없는 서버 내부 예외(Exception) 발생 시 500 상태와 C003 에러 응답이 반환된다.")
  void handleExceptionReturns500InternalServerError() {
    // given
    RuntimeException unhandled = new RuntimeException("DB Connection Timeout Failure");

    // when
    ResponseEntity<ErrorResponse> responseEntity = handler.handleException(unhandled);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    ErrorResponse body = responseEntity.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getStatus()).isEqualTo(500);
    assertThat(body.getCode()).isEqualTo("C003");
    assertThat(body.getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
  }
}
