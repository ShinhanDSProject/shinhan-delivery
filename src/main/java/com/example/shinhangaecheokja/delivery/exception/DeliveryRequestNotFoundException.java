package com.example.shinhangaecheokja.delivery.exception;

/** 주어진 id에 해당하는 DeliveryRequest가 존재하지 않을 때 던진다. */
public class DeliveryRequestNotFoundException extends RuntimeException {

  public DeliveryRequestNotFoundException(Long deliveryRequestId) {
    super("존재하지 않는 배송 요청입니다: " + deliveryRequestId);
  }
}
