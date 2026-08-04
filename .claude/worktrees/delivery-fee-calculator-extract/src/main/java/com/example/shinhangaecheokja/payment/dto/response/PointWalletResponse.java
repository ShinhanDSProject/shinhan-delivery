package com.example.shinhandelivery.payment.dto.response;

import com.example.shinhandelivery.payment.entity.PointWallet;

/** 포인트 지갑 응답 DTO. */
public record PointWalletResponse(Long id, Long memberId, long balance) {

  /** PointWallet 엔티티를 응답 DTO로 변환한다. */
  public static PointWalletResponse from(PointWallet entity) {
    return new PointWalletResponse(entity.getId(), entity.getMemberId(), entity.getBalance());
  }
}
