package com.example.shinhangaecheokja.delivery.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryCreateRequest {

  private Long customerId;
  private String pickupAddress;
  private String dropoffAddress;
  private double weight;
  private double distance;

  @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
  private double pickupLatitude;

  @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
  private double pickupLongitude;
}
