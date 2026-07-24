package com.example.shinhangaecheokja.payment.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 포인트 지갑 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class PointWalletCreateRequest {

  private Long memberId;
}
