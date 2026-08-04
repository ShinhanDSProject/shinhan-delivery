package com.example.shinhangaecheokja.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 재설정 인증코드 검증 응답 DTO. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetVerifyResponseDto {

  private String resetToken;
  private long expiresInSeconds;

  public static PasswordResetVerifyResponseDto of(String resetToken, long expiresInSeconds) {
    return PasswordResetVerifyResponseDto.builder()
        .resetToken(resetToken)
        .expiresInSeconds(expiresInSeconds)
        .build();
  }
}
