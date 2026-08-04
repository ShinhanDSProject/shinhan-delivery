package com.example.shinhangaecheokja.delivery.dto.response;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import java.time.LocalDateTime;

/** 배송 요청 상세 조회 응답 DTO. 목록용 {@link DeliveryListResponseDto}와 달리 배송원 이름·증거사진·타임라인 시각까지 포함한다. */
public record DeliveryDetailResponseDto(
    Long id,
    Long customerId,
    String pickupAddress,
    String dropoffAddress,
    double weight,
    double distance,
    DeliveryStatus status,
    long feePoint,
    double pickupLatitude,
    double pickupLongitude,
    double dropoffLatitude,
    double dropoffLongitude,
    ItemSize itemSize,
    String courierName,
    String proofPhotoUrl,
    LocalDateTime createdAt,
    LocalDateTime matchedAt,
    LocalDateTime pickedUpAt,
    LocalDateTime completedAt) {

  /**
   * DeliveryRequest 엔티티를 상세 응답 DTO로 변환한다. courierName·matchedAt은 아직 매칭된 배송원이 없으면 둘 다 null이다(호출자가
   * Matching을 미리 조회해 전달).
   */
  public static DeliveryDetailResponseDto from(
      DeliveryRequest entity, String courierName, LocalDateTime matchedAt) {
    return new DeliveryDetailResponseDto(
        entity.getId(),
        entity.getCustomerId(),
        entity.getPickupAddress(),
        entity.getDropoffAddress(),
        entity.getWeight(),
        entity.getDistance(),
        entity.getStatus(),
        entity.getFeePoint(),
        entity.getPickupLatitude(),
        entity.getPickupLongitude(),
        entity.getDropoffLatitude(),
        entity.getDropoffLongitude(),
        entity.getItemSize(),
        courierName,
        entity.getProofPhotoUrl(),
        entity.getCreatedAt(),
        matchedAt,
        entity.getPickedUpAt(),
        entity.getCompletedAt());
  }
}
