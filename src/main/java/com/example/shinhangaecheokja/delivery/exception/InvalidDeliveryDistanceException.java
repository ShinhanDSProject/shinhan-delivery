package com.example.shinhangaecheokja.delivery.exception;

/** 배송 요청의 거리가 0 이하로 유효하지 않을 때 던진다. */
public class InvalidDeliveryDistanceException extends RuntimeException {

  public InvalidDeliveryDistanceException(double distance) {
    super("유효하지 않은 배송 거리입니다: " + distance);
  }
}
