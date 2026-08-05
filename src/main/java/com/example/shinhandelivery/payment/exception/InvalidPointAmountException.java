package com.example.shinhandelivery.payment.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

public class InvalidPointAmountException extends BusinessException {

  public InvalidPointAmountException(long amount) {
    super(ErrorCode.INVALID_POINT_AMOUNT, "포인트 금액은 0보다 커야 합니다. (요청 금액: " + amount + ")");
  }
}
