package com.example.shinhangaecheokja.delivery.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 매칭 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MatchingCreateRequest {

  @NotNull(message = "배송 요청 id는 필수입니다.")
  private Long deliveryRequestId;

  @NotNull(message = "차량 id는 필수입니다.")
  private Long vehicleId;
}
