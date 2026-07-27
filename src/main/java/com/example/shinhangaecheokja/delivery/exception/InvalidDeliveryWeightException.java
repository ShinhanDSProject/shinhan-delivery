package com.example.shinhangaecheokja.delivery.exception;

/** 배송 요청의 무게가 0 이하로 유효하지 않을 때 던진다. */
public class InvalidDeliveryWeightException extends RuntimeException {

  public InvalidDeliveryWeightException(double weight) {
    super("유효하지 않은 배송 무게입니다: " + weight);
  }
}
