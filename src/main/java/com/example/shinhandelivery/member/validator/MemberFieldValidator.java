package com.example.shinhandelivery.member.validator;

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
      return MemberFieldValidateResponse.fail(MemberValidationConstants.MSG_INVALID_FIELD_INFO);
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
      return MemberFieldValidateResponse.fail(MemberValidationConstants.MSG_EMAIL_EMPTY);
    }
    if (!MemberValidationConstants.EMAIL_PATTERN.matcher(value).matches()) {
      return MemberFieldValidateResponse.fail(MemberValidationConstants.MSG_EMAIL_INVALID_FORMAT);
    }
    if (isEmailDuplicated != null && isEmailDuplicated.test(value)) {
      return MemberFieldValidateResponse.fail(MemberValidationConstants.MSG_EMAIL_DUPLICATED);
    }
    return MemberFieldValidateResponse.ok(MemberValidationConstants.MSG_EMAIL_SUCCESS);
  }

  private static MemberFieldValidateResponse validatePhoneNumber(String value) {
    if (value.isBlank()) {
      return MemberFieldValidateResponse.fail(MemberValidationConstants.MSG_PHONE_EMPTY);
    }
    if (!MemberValidationConstants.PHONE_PATTERN.matcher(value).matches()) {
      return MemberFieldValidateResponse.fail(MemberValidationConstants.MSG_PHONE_INVALID_FORMAT);
    }
    return MemberFieldValidateResponse.ok(MemberValidationConstants.MSG_PHONE_SUCCESS);
  }

  private static MemberFieldValidateResponse validatePassword(String value) {
    if (value.isBlank()) {
      return MemberFieldValidateResponse.fail(MemberValidationConstants.MSG_PASSWORD_EMPTY);
    }
    if (value.length() < 8 || value.length() > 100) {
      return MemberFieldValidateResponse.fail(
          MemberValidationConstants.MSG_PASSWORD_INVALID_LENGTH);
    }
    if (!MemberValidationConstants.PASSWORD_PATTERN.matcher(value).matches()) {
      return MemberFieldValidateResponse.fail(
          MemberValidationConstants.MSG_PASSWORD_INVALID_PATTERN);
    }
    return MemberFieldValidateResponse.ok(MemberValidationConstants.MSG_PASSWORD_SUCCESS);
  }
}
