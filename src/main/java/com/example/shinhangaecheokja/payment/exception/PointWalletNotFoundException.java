package com.example.shinhangaecheokja.payment.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class PointWalletNotFoundException extends BusinessException {

  public PointWalletNotFoundException() {
    super(ErrorCode.POINT_WALLET_NOT_FOUND);
  }

  public PointWalletNotFoundException(Long id) {
    super(ErrorCode.POINT_WALLET_NOT_FOUND, "포인트 지갑 정보를 찾을 수 없습니다. (ID: " + id + ")");
  }

  public PointWalletNotFoundException(String message) {
    super(ErrorCode.POINT_WALLET_NOT_FOUND, message);
  }
}
