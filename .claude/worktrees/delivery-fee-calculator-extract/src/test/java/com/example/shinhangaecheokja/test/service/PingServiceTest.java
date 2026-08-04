package com.example.shinhandelivery.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.test.dto.PingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PingServiceTest {

  private final PingService pingService = new PingService();

  @Test
  @DisplayName("getPingMessage 호출 시 message는 pong, timestamp는 현재 시각으로 채워진 응답을 반환한다")
  void getPingMessageReturnsPongWithTimestamp() {
    PingResponse response = pingService.getPingMessage();

    assertThat(response).isNotNull();
    assertThat(response.getMessage()).isEqualTo("pong");
    assertThat(response.getTimestamp()).isNotNull();
  }
}
