package com.example.shinhangaecheokja.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PingServiceTest {

  private final PingService pingService = new PingService();

  @Test
  @DisplayName("getPingMessage() 호출 시 message가 'pong'이고 timestamp가 null이 아닌 PingResponse를 반환한다.")
  void getPingMessage_returnsValidPongResponse() {
    // when
    PingResponse response = pingService.getPingMessage();

    // then
    assertThat(response).isNotNull();
    assertThat(response.getMessage()).isEqualTo("pong");
    assertThat(response.getTimestamp()).isNotNull();
  }
}
