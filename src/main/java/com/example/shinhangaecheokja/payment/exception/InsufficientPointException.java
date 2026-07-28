package com.example.shinhangaecheokja.payment.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class InsufficientPointException extends BusinessException {

  public InsufficientPointException() {
    super(ErrorCode.INSUFFICIENT_BALANCE);
  }

  public InsufficientPointException(Long walletId, long amount) {
    super(
        ErrorCode.INSUFFICIENT_BALANCE,
        "포인트 잔액이 부족합니다. (Wallet ID: " + walletId + ", 요청 금액: " + amount + ")");
  }

  public InsufficientPointException(String message) {
    super(ErrorCode.INSUFFICIENT_BALANCE, message);
  }
}
