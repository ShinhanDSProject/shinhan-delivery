package com.example.shinhangaecheokja.member.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class MemberNotFoundException extends BusinessException {

  public MemberNotFoundException() {
    super(ErrorCode.MEMBER_NOT_FOUND);
  }

  public MemberNotFoundException(Long id) {
    super(ErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다. (ID: " + id + ")");
  }

  public MemberNotFoundException(String message) {
    super(ErrorCode.MEMBER_NOT_FOUND, message);
  }
}
