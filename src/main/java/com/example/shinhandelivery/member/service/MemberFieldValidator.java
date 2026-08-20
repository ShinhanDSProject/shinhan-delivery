package com.example.shinhandelivery.member.service;

import com.example.shinhandelivery.member.constant.MemberValidationConstants;
import com.example.shinhandelivery.member.dto.request.MemberFieldValidateRequest;
import com.example.shinhandelivery.member.dto.response.MemberFieldValidateResponse;
import java.util.function.Predicate;

/** 회원 필드(이메일, 전화번호, 비밀번호) 실시간 유효성을 검증하는 순수 도메인 유틸리티. */
public final class MemberFieldValidator {

  private MemberFieldValidator() {
    // Pure utility class instantiation prevention
  }

  /** 단건 회원 필드의 유효성 및 이메일 중복 람다 검증을 수행한다. */
  public static MemberFieldValidateResponse validate(
      MemberFieldValidateRequest request, Predicate<String> isEmailDuplicated) {
    if (request == null || request.getField() == null) {
      return MemberFieldValidateResponse.fail("검증 대상 필드 정보가 올바르지 않습니다.");
    }

    String value = request.getValue() != null ? request.getValue().trim() : "";

    return switch (request.getField()) {
      case EMAIL -> validateEmail(value, isEmailDuplicated);
      case PHONE_NUMBER -> validatePhoneNumber(value);
      case PASSWORD -> validatePassword(value);
    };
  }

  private static MemberFieldValidateResponse validateEmail(
      String value, Predicate<String> isEmailDuplicated) {
    if (value.isBlank()) {
      return MemberFieldValidateResponse.fail("이메일 주소를 입력해주세요.");
    }
    if (!MemberValidationConstants.EMAIL_PATTERN.matcher(value).matches()) {
      return MemberFieldValidateResponse.fail("올바른 이메일 형식이어야 합니다.");
    }
    if (isEmailDuplicated != null && isEmailDuplicated.test(value)) {
      return MemberFieldValidateResponse.fail("이미 가입된 이메일 주소입니다.");
    }
    return MemberFieldValidateResponse.ok("✓ 사용 가능한 이메일입니다.");
  }

  private static MemberFieldValidateResponse validatePhoneNumber(String value) {
    if (value.isBlank()) {
      return MemberFieldValidateResponse.fail("휴대폰 번호를 입력해주세요.");
    }
    if (!MemberValidationConstants.PHONE_PATTERN.matcher(value).matches()) {
      return MemberFieldValidateResponse.fail("올바른 전화번호 형식(예: 010-1234-5678)이어야 합니다.");
    }
    return MemberFieldValidateResponse.ok("✓ 올바른 전화번호 형식입니다.");
  }

  private static MemberFieldValidateResponse validatePassword(String value) {
    if (value.isBlank()) {
      return MemberFieldValidateResponse.fail("비밀번호를 입력해주세요.");
    }
    if (value.length() < 8 || value.length() > 100) {
      return MemberFieldValidateResponse.fail("비밀번호는 8자 이상 100자 이하이어야 합니다.");
    }
    if (!MemberValidationConstants.PASSWORD_PATTERN.matcher(value).matches()) {
      return MemberFieldValidateResponse.fail("비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
    }
    return MemberFieldValidateResponse.ok("✓ 사용 가능한 비밀번호 조합입니다.");
  }
}
