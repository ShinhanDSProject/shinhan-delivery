package com.example.shinhandelivery.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 수정 요청 DTO. 고객·무게·거리·요금은 변경 대상이 아니다. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryUpdateRequest {

  @NotBlank(message = "픽업 주소는 필수입니다.")
  private String pickupAddress;

  @NotBlank(message = "도착 주소는 필수입니다.")
  private String dropoffAddress;
}
