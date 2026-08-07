package com.example.shinhandelivery.delivery.dto.request;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 생성 요청 DTO. 거리는 클라이언트가 주지 않고 출발지·도착지 좌표로 서버가 직접 계산한다. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryCreateRequest {

  @NotBlank(message = "픽업 주소는 필수입니다.")
  private String pickupAddress;

  @NotBlank(message = "도착 주소는 필수입니다.")
  private String dropoffAddress;

  @DecimalMin(value = "0.0", inclusive = false, message = "물품 무게는 0보다 커야 합니다.")
  private double weight;

  @DecimalMin(value = "-90.0", message = "출발지 위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "출발지 위도는 90 이하이어야 합니다.")
  private double pickupLatitude;

  @DecimalMin(value = "-180.0", message = "출발지 경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "출발지 경도는 180 이하이어야 합니다.")
  private double pickupLongitude;

  @DecimalMin(value = "-90.0", message = "도착지 위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "도착지 위도는 90 이하이어야 합니다.")
  private double dropoffLatitude;

  @DecimalMin(value = "-180.0", message = "도착지 경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "도착지 경도는 180 이하이어야 합니다.")
  private double dropoffLongitude;

  @NotNull(message = "물품 크기는 필수입니다.")
  private ItemSize itemSize;

  @JsonIgnore
  public Location getPickupLocation() {
    return Location.of(pickupLatitude, pickupLongitude);
  }

  @JsonIgnore
  public Location getDropoffLocation() {
    return Location.of(dropoffLatitude, dropoffLongitude);
  }
}
