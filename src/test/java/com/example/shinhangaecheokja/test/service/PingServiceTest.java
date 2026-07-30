package com.example.shinhangaecheokja.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PingServiceTest {

  private final PingService pingService = new PingService();

  @Test
  void ping_요청에_pong과_현재_시각을_반환한다() {
    LocalDateTime beforeCall = LocalDateTime.now();

    PingResponse response = pingService.getPingMessage();

    assertThat(response.getMessage()).isEqualTo("pong");
    assertThat(response.getTimestamp()).isBetween(beforeCall, LocalDateTime.now());
  }
}
