package com.example.shinhangaecheokja.delivery.dto.response;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;

/** 배송 요청 응답 DTO. */
public record DeliveryResponse(
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
    ItemSize itemSize) {

  /** DeliveryRequest 엔티티를 응답 DTO로 변환한다. */
  public static DeliveryResponse from(DeliveryRequest entity) {
    return new DeliveryResponse(
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
        entity.getItemSize());
  }
}
