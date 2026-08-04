package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import java.time.LocalDateTime;

/** 배송 완료 증거 사진 조회 응답 DTO. */
public record ProofPhotoResponse(
    Long deliveryRequestId, String proofPhotoUrl, LocalDateTime completedAt) {

  public static ProofPhotoResponse from(DeliveryRequest entity) {
    return new ProofPhotoResponse(
        entity.getId(), entity.getProofPhotoUrl(), entity.getCompletedAt());
  }
}
