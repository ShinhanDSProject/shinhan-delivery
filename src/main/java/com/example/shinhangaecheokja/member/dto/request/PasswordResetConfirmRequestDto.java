package com.example.shinhangaecheokja.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 최종 변경 확정 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetConfirmRequestDto {

  @NotBlank(message = "비밀번호 재설정 토큰은 필수입니다.")
  private String resetToken;

  @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
  private String newPassword;

  @NotBlank(message = "새 비밀번호 확인은 필수 입력값입니다.")
  private String newPasswordConfirm;
}
