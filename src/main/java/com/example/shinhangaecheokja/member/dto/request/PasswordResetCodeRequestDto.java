package com.example.shinhangaecheokja.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 재설정 이메일 인증코드 발송 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetCodeRequestDto {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;
}
