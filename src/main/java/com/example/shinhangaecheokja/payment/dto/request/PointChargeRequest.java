package com.example.shinhangaecheokja.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 포인트 충전 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class PointChargeRequest {

  private long amount;
}
