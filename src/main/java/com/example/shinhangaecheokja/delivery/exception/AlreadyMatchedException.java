package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;

/** 이미 매칭되었거나 완료·취소되어 더 이상 매칭 대상이 될 수 없는 배송 요청에 접근할 때 던진다. */
public class AlreadyMatchedException extends RuntimeException {

  public AlreadyMatchedException(Long deliveryRequestId, DeliveryStatus status) {
    super("이미 " + describe(status) + " 배송 요청입니다: " + deliveryRequestId);
  }

  private static String describe(DeliveryStatus status) {
    return switch (status) {
      case MATCHED, REQUESTED -> "매칭된";
      case COMPLETED -> "완료된";
      case CANCELLED -> "취소된";
    };
  }
}
