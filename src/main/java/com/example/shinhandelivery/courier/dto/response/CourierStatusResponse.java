package com.example.shinhandelivery.courier.dto.response;

import com.example.shinhandelivery.courier.entity.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송원 영업 상태 응답 DTO. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierStatusResponse {

  private Long memberId;
  private WorkStatus workStatus;
  private Double latitude;
  private Double longitude;
}
