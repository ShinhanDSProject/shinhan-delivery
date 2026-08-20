package com.example.shinhandelivery.member.constant;

import java.util.regex.Pattern;

/** 회원 관련 유효성 검사 정규식 및 패턴 상수 정의. */
public final class MemberValidationConstants {

  public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
  public static final String PHONE_REGEX = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
  public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).+$";

  public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
  public static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
  public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

  private MemberValidationConstants() {
    // Utility class instantiation prevent
  }
}
