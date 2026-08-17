package com.example.shinhandelivery.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchingWaitPointNavigationTest {

  @Test
  @DisplayName("매칭 대기 화면의 포인트 버튼은 요청 ID와 무관하게 포인트 지갑으로 이동한다")
  void matchingWaitShouldUseSharedPointWalletNavigation() throws IOException {
    String matchingWait = readTemplate("matching-wait.html");
    String components = readTemplate("fragments/components.html");

    assertThat(matchingWait)
        .contains("customerBottomNavigation('')")
        .contains("customer-bottom-nav-content")
        .doesNotContain("onclick=\"showToast('아직 준비 중인 기능입니다.')\"");
    assertThat(components)
        .contains("href=\"/point-wallet\"")
        .contains("href=\"/home\"")
        .contains("href=\"/my-page\"")
        .contains("aria-label=\"포인트\"");
  }

  private String readTemplate(String template) throws IOException {
    String resourcePath = "templates/" + template;
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertThat(inputStream).as("템플릿 리소스 %s", resourcePath).isNotNull();
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
