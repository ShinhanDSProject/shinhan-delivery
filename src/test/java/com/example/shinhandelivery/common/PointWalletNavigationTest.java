package com.example.shinhandelivery.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointWalletNavigationTest {

  @Test
  @DisplayName("마이페이지 계열 화면은 포인트 지갑 경로가 있는 공통 내비게이션을 사용한다")
  void sharedNavigationTemplatesShouldLinkToPointWallet() throws IOException {
    String components = readTemplate("fragments/components.html");

    for (String template :
        List.of(
            "my-page.html",
            "announcements.html",
            "address-management.html",
            "change-password.html",
            "profile-edit.html",
            "delivery-history.html")) {
      assertThat(readTemplate(template))
          .as("%s의 공통 포인트 지갑 내비게이션", template)
          .contains("customerBottomNavigation('myPage')");
    }
    assertThat(components)
        .contains("th:fragment=\"customerBottomNavigation(activeItem)\"")
        .contains("href=\"/point-wallet\"");
  }

  @Test
  @DisplayName("배송 내역 뒤로가기는 마이페이지로 이동한다")
  void deliveryHistoryBackButtonShouldReturnToMyPage() throws IOException {
    String deliveryHistory = readTemplate("delivery-history.html");

    assertThat(deliveryHistory)
        .contains("aria-label=\"마이페이지로 돌아가기\"")
        .contains("onclick=\"location.href='/my-page'\"")
        .doesNotContain("onclick=\"location.href='/home'\"");
  }

  private String readTemplate(String template) throws IOException {
    String resourcePath = "templates/" + template;
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertThat(inputStream).as("템플릿 리소스 %s", resourcePath).isNotNull();
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
