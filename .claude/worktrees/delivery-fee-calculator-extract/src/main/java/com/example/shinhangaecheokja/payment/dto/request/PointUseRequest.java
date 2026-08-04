package com.example.shinhandelivery.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 포인트 사용 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class PointUseRequest {

  private long amount;
}
