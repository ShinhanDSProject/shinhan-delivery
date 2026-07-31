package com.example.shinhangaecheokja.delivery.dto.response;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;

/** 배송 완료 증거 사진 조회 응답 DTO. */
public record ProofPhotoResponse(Long deliveryRequestId, String proofPhotoUrl) {

  public static ProofPhotoResponse from(DeliveryRequest entity) {
    return new ProofPhotoResponse(entity.getId(), entity.getProofPhotoUrl());
  }
}
