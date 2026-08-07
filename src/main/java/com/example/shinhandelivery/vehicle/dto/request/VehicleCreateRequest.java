package com.example.shinhandelivery.vehicle.dto.request;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

  @NotNull(message = "회원 id는 필수입니다.")
  private Long memberId;

  @NotNull(message = "운송수단 종류는 필수입니다.")
  private VehicleType type;

  @DecimalMin(value = "0.0", inclusive = false, message = "최대 적재 무게는 0보다 커야 합니다.")
  private double maxWeight;

  @DecimalMin(value = "0.0", inclusive = false, message = "최대 운행 거리는 0보다 커야 합니다.")
  private double maxDistance;

  @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
  private double latitude;

  @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
  private double longitude;

  @JsonIgnore
  public Location getLocation() {
    return Location.of(latitude, longitude);
  }
}
