package com.example.shinhandelivery.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.member.dto.request.MemberFieldValidateRequest;
import com.example.shinhandelivery.member.dto.response.MemberFieldValidateResponse;
import com.example.shinhandelivery.member.entity.MemberValidationField;
import com.example.shinhandelivery.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberFieldValidatorTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberFieldValidator memberFieldValidator;

  @Test
  @DisplayName("이미 가입된 이메일인 경우 validate는 fail 응답을 반환한다")
  void validateDuplicateEmail() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.EMAIL, "user@example.com");
    when(memberRepository.existsByEmail("user@example.com")).thenReturn(true);

    MemberFieldValidateResponse response = memberFieldValidator.validate(request);

    assertThat(response.isValid()).isFalse();
    assertThat(response.getMessage()).isEqualTo("이미 가입된 이메일 주소입니다.");
  }

  @Test
  @DisplayName("올바른 이메일인 경우 validate는 ok 응답을 반환한다")
  void validateSuccessEmail() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.EMAIL, "newuser@example.com");
    when(memberRepository.existsByEmail("newuser@example.com")).thenReturn(false);

    MemberFieldValidateResponse response = memberFieldValidator.validate(request);

    assertThat(response.isValid()).isTrue();
    assertThat(response.getMessage()).isEqualTo("✓ 사용 가능한 이메일입니다.");
  }

  @Test
  @DisplayName("올바른 전화번호인 경우 validate는 ok 응답을 반환한다")
  void validateSuccessPhoneNumber() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.PHONE_NUMBER, "010-1234-5678");

    MemberFieldValidateResponse response = memberFieldValidator.validate(request);

    assertThat(response.isValid()).isTrue();
    assertThat(response.getMessage()).isEqualTo("✓ 올바른 전화번호 형식입니다.");
  }

  @Test
  @DisplayName("올바른 비밀번호 조함인 경우 validate는 ok 응답을 반환한다")
  void validateSuccessPassword() {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.PASSWORD, "password123!");

    MemberFieldValidateResponse response = memberFieldValidator.validate(request);

    assertThat(response.isValid()).isTrue();
    assertThat(response.getMessage()).isEqualTo("✓ 사용 가능한 비밀번호 조합입니다.");
  }
}
