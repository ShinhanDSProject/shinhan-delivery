package com.example.shinhangaecheokja.payment.exception;

/** 포인트 사용 시 지갑 잔액이 부족할 때 던진다. */
public class InsufficientPointException extends RuntimeException {

  public InsufficientPointException(Long walletId, long requiredAmount) {
    super("포인트가 부족합니다: 지갑 " + walletId + ", 필요 금액 " + requiredAmount);
  }
}
