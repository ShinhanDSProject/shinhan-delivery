package com.example.shinhandelivery.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 회원 필드 실시간 검증 응답 DTO. */
@Getter
@AllArgsConstructor
public class MemberFieldValidateResponse {

  private final boolean valid;
  private final String message;

  public static MemberFieldValidateResponse ok(String message) {
    return new MemberFieldValidateResponse(true, message);
  }

  public static MemberFieldValidateResponse fail(String message) {
    return new MemberFieldValidateResponse(false, message);
  }
}
