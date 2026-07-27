package com.example.shinhangaecheokja.common.exception;

import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.MatchingNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.NoAvailableCourierException;
import com.example.shinhangaecheokja.member.exception.DuplicateMemberException;
import com.example.shinhangaecheokja.member.exception.MemberNotFoundException;
import com.example.shinhangaecheokja.payment.exception.InsufficientPointException;
import com.example.shinhangaecheokja.payment.exception.PointWalletNotFoundException;
import com.example.shinhangaecheokja.vehicle.exception.InvalidWeightException;
import com.example.shinhangaecheokja.vehicle.exception.OverMaxDistanceException;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 모든 예외를 HTTP 응답으로 변환하는 전역 예외 처리기. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 요청 본문 파싱 실패를 400으로 변환한다. */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(HttpMessageNotReadableException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse("요청 본문을 읽을 수 없습니다."));
  }

  /** 입력값 자체가 유효하지 않은 경우 400으로 변환한다. */
  @ExceptionHandler({InvalidWeightException.class, OverMaxDistanceException.class})
  public ResponseEntity<ErrorResponse> handleInvalidInput(RuntimeException e) {
    return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
  }

  /** 리소스를 찾을 수 없는 경우 404로 변환한다. */
  @ExceptionHandler({
    MemberNotFoundException.class,
    VehicleNotFoundException.class,
    DeliveryRequestNotFoundException.class,
    MatchingNotFoundException.class,
    PointWalletNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
  }

  /** 리소스 중복(예: 이메일 중복 가입)을 409로 변환한다. */
  @ExceptionHandler(DuplicateMemberException.class)
  public ResponseEntity<ErrorResponse> handleConflict(DuplicateMemberException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
  }

  /** 비즈니스 규칙상 처리가 불가능한 경우 422로 변환한다. */
  @ExceptionHandler({
    NoAvailableCourierException.class,
    AlreadyMatchedException.class,
    InsufficientPointException.class
  })
  public ResponseEntity<ErrorResponse> handleUnprocessable(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(new ErrorResponse(e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
    // 예상 못한 예외는 500으로, 원인은 로그로만 남기고 사용자에겐 상세 노출 안 함
    return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류가 발생했습니다."));
  }
}
