package com.example.shinhandelivery.member.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

public class DuplicateMemberException extends BusinessException {

  public DuplicateMemberException() {
    super(ErrorCode.DUPLICATE_EMAIL);
  }

  public DuplicateMemberException(String email) {
    super(ErrorCode.DUPLICATE_EMAIL, "이미 가입된 이메일 주소입니다. (Email: " + email + ")");
  }
}
