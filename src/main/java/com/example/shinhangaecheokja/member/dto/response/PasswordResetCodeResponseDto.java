package com.example.shinhangaecheokja.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 재설정 인증코드 발송 응답 DTO. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetCodeResponseDto {

  private String message;
  private long expiresInSeconds;

  public static PasswordResetCodeResponseDto of(String message, long expiresInSeconds) {
    return PasswordResetCodeResponseDto.builder()
        .message(message)
        .expiresInSeconds(expiresInSeconds)
        .build();
  }
}
