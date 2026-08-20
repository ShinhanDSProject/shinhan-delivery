package com.example.shinhandelivery.member.validator;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.member.constant.MemberValidationConstants;
import com.example.shinhandelivery.member.dto.request.MemberFieldValidateRequest;
import com.example.shinhandelivery.member.dto.response.MemberFieldValidateResponse;
import com.example.shinhandelivery.member.entity.MemberValidationField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberFieldValidatorTest {

  @Test
  @DisplayName("이미 가입된 이메일인 경우 validate는 fail 응답을 반환한다")
  void validateDuplicateEmail() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.EMAIL, "user@example.com");

    MemberFieldValidateResponse response =
        MemberFieldValidator.validate(request, email -> "user@example.com".equals(email));

    assertThat(response.isValid()).isFalse();
    assertThat(response.getMessage()).isEqualTo(MemberValidationConstants.MSG_EMAIL_DUPLICATED);
  }

  @Test
  @DisplayName("올바른 이메일인 경우 validate는 ok 응답을 반환한다")
  void validateSuccessEmail() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.EMAIL, "newuser@example.com");

    MemberFieldValidateResponse response = MemberFieldValidator.validate(request, email -> false);

    assertThat(response.isValid()).isTrue();
    assertThat(response.getMessage()).isEqualTo(MemberValidationConstants.MSG_EMAIL_SUCCESS);
  }

  @Test
  @DisplayName("올바른 전화번호인 경우 validate는 ok 응답을 반환한다")
  void validateSuccessPhoneNumber() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.PHONE_NUMBER, "010-1234-5678");

    MemberFieldValidateResponse response = MemberFieldValidator.validate(request, null);

    assertThat(response.isValid()).isTrue();
    assertThat(response.getMessage()).isEqualTo(MemberValidationConstants.MSG_PHONE_SUCCESS);
  }

  @Test
  @DisplayName("올바른 비밀번호 조합인 경우 validate는 ok 응답을 반환한다")
  void validateSuccessPassword() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.PASSWORD, "password123!");

    MemberFieldValidateResponse response = MemberFieldValidator.validate(request, null);

    assertThat(response.isValid()).isTrue();
    assertThat(response.getMessage()).isEqualTo(MemberValidationConstants.MSG_PASSWORD_SUCCESS);
  }
}
