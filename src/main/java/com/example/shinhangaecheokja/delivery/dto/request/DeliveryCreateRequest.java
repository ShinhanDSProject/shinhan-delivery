package com.example.shinhangaecheokja.delivery.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryCreateRequest {

  @NotNull(message = "고객 id는 필수입니다.")
  private Long customerId;

  @NotBlank(message = "픽업 주소는 필수입니다.")
  private String pickupAddress;

  @NotBlank(message = "도착 주소는 필수입니다.")
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
