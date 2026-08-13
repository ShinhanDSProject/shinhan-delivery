package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import java.time.LocalDateTime;

/** 픽업 완료 물품 확인 사진 조회 응답 DTO. */
public record PickupPhotoResponse(
    Long deliveryRequestId, String pickupPhotoUrl, LocalDateTime pickedUpAt) {

  public static PickupPhotoResponse from(DeliveryRequest entity) {
    return new PickupPhotoResponse(
        entity.getId(), entity.getPickupPhotoUrl(), entity.getPickedUpAt());
  }
}
