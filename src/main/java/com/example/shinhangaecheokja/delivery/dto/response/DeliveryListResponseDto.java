package com.example.shinhangaecheokja.delivery.dto.response;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import java.time.LocalDateTime;

/** 배송 내역 목록 조회 응답 DTO. 목록 화면에는 상세 정보 전부가 필요 없어 요약 필드만 담는다. */
public record DeliveryListResponseDto(
    Long id,
    DeliveryStatus status,
    String pickupAddress,
    String dropoffAddress,
    LocalDateTime createdAt) {

  /** DeliveryRequest 엔티티를 목록 응답 DTO로 변환한다. */
  public static DeliveryListResponseDto from(DeliveryRequest entity) {
    return new DeliveryListResponseDto(
        entity.getId(),
        entity.getStatus(),
        entity.getPickupAddress(),
        entity.getDropoffAddress(),
        entity.getCreatedAt());
  }
}
