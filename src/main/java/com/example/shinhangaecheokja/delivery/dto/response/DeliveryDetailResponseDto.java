package com.example.shinhangaecheokja.delivery.dto.response;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;

/** 배송 요청 상세 조회 응답 DTO. 목록용 {@link DeliveryListResponseDto}와 달리 배송원 이름·증거사진까지 포함한다. */
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
    String proofPhotoUrl) {

  /** DeliveryRequest 엔티티를 상세 응답 DTO로 변환한다. courierName은 아직 매칭된 배송원이 없으면 null이다(호출자가 미리 조회해 전달). */
  public static DeliveryDetailResponseDto from(DeliveryRequest entity, String courierName) {
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
        entity.getProofPhotoUrl());
  }
}
