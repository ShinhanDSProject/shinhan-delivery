package com.example.shinhangaecheokja.delivery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 수정 요청 DTO. 고객·무게·거리·요금은 변경 대상이 아니다. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryUpdateRequest {

  private String pickupAddress;
  private String dropoffAddress;
}
