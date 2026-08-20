package com.example.shinhandelivery.member.constant;

import java.util.regex.Pattern;

/** 회원 관련 유효성 검사 정규식, 패턴 및 안내 메시지 상수 정의. */
public final class MemberValidationConstants {

  // 정규식 문자열 및 패턴
  public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
  public static final String PHONE_REGEX = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
  public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).+$";

  public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
  public static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
  public static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

  // 공통 검증 메시지
  public static final String MSG_INVALID_FIELD_INFO = "검증 대상 필드 정보가 올바르지 않습니다.";

  // 이메일 검증 메시지
  public static final String MSG_EMAIL_EMPTY = "이메일 주소를 입력해주세요.";
  public static final String MSG_EMAIL_INVALID_FORMAT = "올바른 이메일 형식이어야 합니다.";
  public static final String MSG_EMAIL_DUPLICATED = "이미 가입된 이메일 주소입니다.";
  public static final String MSG_EMAIL_SUCCESS = "✓ 사용 가능한 이메일입니다.";

  // 전화번호 검증 메시지
  public static final String MSG_PHONE_EMPTY = "휴대폰 번호를 입력해주세요.";
  public static final String MSG_PHONE_INVALID_FORMAT = "올바른 전화번호 형식(예: 010-1234-5678)이어야 합니다.";
  public static final String MSG_PHONE_SUCCESS = "✓ 올바른 전화번호 형식입니다.";

  // 비밀번호 검증 메시지
  public static final String MSG_PASSWORD_EMPTY = "비밀번호를 입력해주세요.";
  public static final String MSG_PASSWORD_INVALID_LENGTH = "비밀번호는 8자 이상 100자 이하이어야 합니다.";
  public static final String MSG_PASSWORD_INVALID_PATTERN = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.";
  public static final String MSG_PASSWORD_SUCCESS = "✓ 사용 가능한 비밀번호 조합입니다.";

  private MemberValidationConstants() {
    // Utility class instantiation prevention
  }
}
