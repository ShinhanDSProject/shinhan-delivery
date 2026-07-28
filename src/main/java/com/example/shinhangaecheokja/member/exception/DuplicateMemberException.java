package com.example.shinhangaecheokja.member.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class DuplicateMemberException extends BusinessException {

  public DuplicateMemberException() {
    super(ErrorCode.DUPLICATE_EMAIL);
  }

  public DuplicateMemberException(String email) {
    super(ErrorCode.DUPLICATE_EMAIL, "이미 가입된 이메일 주소입니다. (Email: " + email + ")");
  }
}
