package com.example.shinhandelivery.courier.dto.request;

import com.example.shinhandelivery.courier.entity.WorkStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송원 영업 상태 변경 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourierStatusUpdateRequest {

  @NotNull(message = "영업 상태는 필수 입력 항목입니다.")
  private WorkStatus status;

  private Double latitude; // 위도
  private Double longitude; // 경도
}
