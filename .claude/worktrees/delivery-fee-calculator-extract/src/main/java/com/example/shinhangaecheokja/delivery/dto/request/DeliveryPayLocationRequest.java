package com.example.shinhangaecheokja.delivery.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 결제 요청의 출발지/도착지 좌표 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryPayLocationRequest {

  @NotBlank(message = "주소는 필수입니다.")
  private String address;

  @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
  private double lat;

  @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
  private double lng;
}
