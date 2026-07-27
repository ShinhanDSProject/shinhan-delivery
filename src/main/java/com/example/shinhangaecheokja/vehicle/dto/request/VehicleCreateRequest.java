package com.example.shinhangaecheokja.vehicle.dto.request;

import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 운송수단 등록 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class VehicleCreateRequest {

  @NotNull(message = "소유자 id는 필수입니다.")
  private Long ownerId;

  @NotNull(message = "운송수단 종류는 필수입니다.")
  private VehicleType type;

  private double maxWeight;
  private double maxDistance;

  @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
  private double latitude;

  @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
  private double longitude;
}
