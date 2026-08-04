package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import java.time.LocalDateTime;

/** 매칭 응답 DTO. */
public record MatchingResponse(
    Long id,
    Long deliveryRequestId,
    Long vehicleId,
    MatchingStatus status,
    LocalDateTime matchedAt) {

  /** Matching 엔티티를 응답 DTO로 변환한다. */
  public static MatchingResponse from(Matching entity) {
    return new MatchingResponse(
        entity.getId(),
        entity.getDeliveryRequestId(),
        entity.getVehicleId(),
        entity.getStatus(),
        entity.getMatchedAt());
  }
}
