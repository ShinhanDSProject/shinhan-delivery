package com.example.shinhangaecheokja.realtime.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

/** 차량 소유주가 아닌 회원이 그 차량의 오퍼 채널을 구독하려 할 때 던진다. */
public class UnauthorizedOfferAccessException extends BusinessException {

  public UnauthorizedOfferAccessException(Long vehicleId, Long memberId) {
    super(
        ErrorCode.UNAUTHORIZED,
        "해당 차량의 오퍼 채널에 접근할 권한이 없습니다: vehicleId=" + vehicleId + ", memberId=" + memberId);
  }
}
