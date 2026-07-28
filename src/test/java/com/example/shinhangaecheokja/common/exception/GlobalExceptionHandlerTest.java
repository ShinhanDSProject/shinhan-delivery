package com.example.shinhangaecheokja.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("비즈니스 예외(BusinessException) 발생 시 설정된 HTTP 상태와 ErrorCode 정보가 반환된다.")
  void handleBusinessException_returnsCorrectStatusAndResponseBody() {
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
  @DisplayName("알 수 없는 서버 내부 예외(Exception) 발생 시 500 상태와 C003 에러 응답이 반환된다.")
  void handleException_returns500InternalServerError() {
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
