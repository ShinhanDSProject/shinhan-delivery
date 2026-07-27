package com.example.shinhangaecheokja.payment.exception;

/** 주어진 id에 해당하는 PointWallet이 존재하지 않을 때 던진다. */
public class PointWalletNotFoundException extends RuntimeException {

  public PointWalletNotFoundException(Long walletId) {
    super("존재하지 않는 포인트 지갑입니다: " + walletId);
  }
}
