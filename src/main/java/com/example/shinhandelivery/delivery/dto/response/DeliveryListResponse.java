package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import java.time.LocalDateTime;

/** 배송 내역 목록 조회 응답 DTO. 목록 화면에는 상세 정보 전부가 필요 없어 요약 필드만 담는다. */
public record DeliveryListResponse(
    Long id,
    DeliveryStatus status,
    String pickupAddress,
    String dropoffAddress,
    long feePoint,
    LocalDateTime createdAt) {

  /** DeliveryRequest 엔티티를 목록 응답 DTO로 변환한다. */
  public static DeliveryListResponse from(DeliveryRequest entity) {
    return new DeliveryListResponse(
        entity.getId(),
        entity.getStatus(),
        entity.getPickupAddress(),
        entity.getDropoffAddress(),
        entity.getFeePoint(),
        entity.getCreatedAt());
  }
}
