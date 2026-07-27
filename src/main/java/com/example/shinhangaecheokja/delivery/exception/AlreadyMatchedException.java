package com.example.shinhangaecheokja.delivery.exception;

/** 이미 매칭된 배송 요청에 다시 매칭을 시도할 때 던진다. */
public class AlreadyMatchedException extends RuntimeException {

  public AlreadyMatchedException(Long deliveryRequestId) {
    super("이미 매칭된 배송 요청입니다: " + deliveryRequestId);
  }
}
