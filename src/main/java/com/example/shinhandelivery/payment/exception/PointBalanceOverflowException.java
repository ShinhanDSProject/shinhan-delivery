package com.example.shinhandelivery.payment.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

public class PointBalanceOverflowException extends BusinessException {

  public PointBalanceOverflowException(Long walletId, long balance, long amount) {
    super(
        ErrorCode.POINT_BALANCE_OVERFLOW,
        "포인트 잔액 한도를 초과했습니다. (Wallet ID: "
            + walletId
            + ", 현재 잔액: "
            + balance
            + ", 충전 금액: "
            + amount
            + ")");
  }
}
