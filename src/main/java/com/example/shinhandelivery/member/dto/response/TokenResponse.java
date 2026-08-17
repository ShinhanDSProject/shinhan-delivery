package com.example.shinhandelivery.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType;

  public static TokenResponse of(String accessToken, String refreshToken) {
    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .build();
  }
}
