package com.example.shinhangaecheokja.payment.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 포인트 사용 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class PointUseRequest {

  @Positive(message = "사용 금액은 0보다 커야 합니다.")
  private long amount;
}
