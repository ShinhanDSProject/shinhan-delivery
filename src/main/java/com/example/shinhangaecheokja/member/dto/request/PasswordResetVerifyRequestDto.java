package com.example.shinhangaecheokja.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 재설정 인증코드 검증 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetVerifyRequestDto {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "인증번호는 필수 입력값입니다.")
  @Size(min = 6, max = 6, message = "인증번호는 6자리 숫자여야 합니다.")
  private String code;
}
