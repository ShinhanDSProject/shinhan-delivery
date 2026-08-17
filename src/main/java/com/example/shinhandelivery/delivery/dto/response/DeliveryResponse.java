package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import lombok.Builder;

/** 배송 요청 응답 DTO. */
@Builder
public record DeliveryResponse(
    Long id,
    Long memberId,
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
    return DeliveryResponse.builder()
        .id(entity.getId())
        .memberId(entity.getMemberId())
        .pickupAddress(entity.getPickupAddress())
        .dropoffAddress(entity.getDropoffAddress())
        .weight(entity.getWeight())
        .distance(entity.getDistance())
        .status(entity.getStatus())
        .feePoint(entity.getFeePoint())
        .pickupLatitude(entity.getPickupLatitude())
        .pickupLongitude(entity.getPickupLongitude())
        .dropoffLatitude(entity.getDropoffLatitude())
        .dropoffLongitude(entity.getDropoffLongitude())
        .itemSize(entity.getItemSize())
        .build();
  }
}
