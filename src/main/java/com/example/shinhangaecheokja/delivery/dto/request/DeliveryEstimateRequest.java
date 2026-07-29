package com.example.shinhangaecheokja.delivery.dto.request;

import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요금 견적 요청 DTO. 실제 배송 요청을 생성하지 않고 예상 요금만 계산한다. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryEstimateRequest {

  @NotNull(message = "출발지 위도는 필수입니다.")
  @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
  private Double pickupLatitude;

  @NotNull(message = "출발지 경도는 필수입니다.")
  @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
  private Double pickupLongitude;

  @NotNull(message = "도착지 위도는 필수입니다.")
  @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
  private Double destinationLatitude;

  @NotNull(message = "도착지 경도는 필수입니다.")
  @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
  private Double destinationLongitude;

  @NotNull(message = "물품 무게는 필수입니다.")
  @DecimalMin(value = "0.0", inclusive = false, message = "물품 무게는 0보다 커야 합니다.")
  private Double weight;

  @NotNull(message = "물품 크기는 필수입니다.")
  private ItemSize itemSize;
}
