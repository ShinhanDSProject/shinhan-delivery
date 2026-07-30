package com.example.shinhangaecheokja.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인한 회원의 비밀번호 변경 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberPasswordUpdateRequest {

  @NotBlank(message = "현재 비밀번호는 필수 입력 값입니다.")
  private String currentPassword;

  @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
  @Size(min = 8, max = 100, message = "새 비밀번호는 8자 이상 100자 이하이어야 합니다.")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).+$",
      message = "새 비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
  private String newPassword;

  @NotBlank(message = "새 비밀번호 확인은 필수 입력 값입니다.")
  private String confirmNewPassword;
}
