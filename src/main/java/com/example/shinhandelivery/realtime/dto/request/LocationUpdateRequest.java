package com.example.shinhandelivery.realtime.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송원 클라이언트가 STOMP로 발행하는 위치 업데이트 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class LocationUpdateRequest {

  @NotNull(message = "배송 요청 id는 필수입니다.")
  private Long deliveryId;

  @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
  @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
  private double latitude;

  @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
  @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
  private double longitude;

  @NotNull(message = "타임스탬프는 필수입니다.")
  private LocalDateTime timestamp;
}
