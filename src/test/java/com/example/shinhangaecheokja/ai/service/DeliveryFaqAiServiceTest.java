package com.example.shinhangaecheokja.ai.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.ai.dto.DeliveryFaqRequest;
import com.example.shinhangaecheokja.ai.dto.DeliveryFaqResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeliveryFaqAiServiceTest {

  @Autowired private DeliveryFaqAiService deliveryFaqAiService;

  @Test
  @DisplayName("LangChain4j RAG 질의 시 배송 규정에 알맞은 답변과 출처가 정상 반환된다")
  void askFaq_validQuestion_returnsAnswer() {
    // given
    DeliveryFaqRequest request = new DeliveryFaqRequest("자전거로 10kg 화물 배송할 수 있나요?");

    // when
    DeliveryFaqResponse response = deliveryFaqAiService.askFaq(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getQuestion()).isEqualTo("자전거로 10kg 화물 배송할 수 있나요?");
    assertThat(response.getAnswer()).isNotEmpty();
    assertThat(response.getSource()).contains("LangChain4j RAG Engine");
  }
}
