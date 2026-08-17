package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import java.time.LocalDateTime;
import lombok.Builder;

/** 매칭 응답 DTO. */
@Builder
public record MatchingResponse(
    Long id,
    Long deliveryRequestId,
    Long vehicleId,
    MatchingStatus status,
    LocalDateTime matchedAt) {

  /** Matching 엔티티를 응답 DTO로 변환한다. */
  public static MatchingResponse from(Matching entity) {
    return MatchingResponse.builder()
        .id(entity.getId())
        .deliveryRequestId(entity.getDeliveryRequestId())
        .vehicleId(entity.getVehicleId())
        .status(entity.getStatus())
        .matchedAt(entity.getMatchedAt())
        .build();
  }
}
