package com.example.shinhangaecheokja.vehicle.dto.request;

import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 운송수단 수정 요청 DTO. ownerId는 변경 대상이 아니다. */
@Getter
@Setter
@NoArgsConstructor
public class VehicleUpdateRequest {

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
