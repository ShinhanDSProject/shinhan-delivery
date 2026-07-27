package com.example.shinhangaecheokja.delivery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 매칭 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MatchingCreateRequest {

  private Long deliveryRequestId;
  private Long vehicleId;
}
