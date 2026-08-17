package com.example.shinhandelivery.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerBottomNavigationTest {

  private static final Map<String, String> AFFECTED_TEMPLATES =
      Map.of(
          "address-management.html", "myPage",
          "announcements.html", "myPage",
          "change-password.html", "myPage",
          "delivery-cancel-list.html", "myPage",
          "delivery-history.html", "myPage",
          "matching-wait.html", "",
          "point-wallet.html", "point",
          "profile-edit.html", "myPage",
          "payment-pin-settings.html", "myPage");

  @Test
  @DisplayName("공통 고객 하단 내비게이션은 포인트 홈 마이페이지 경로를 제공한다")
  void sharedNavigationShouldProvideCustomerDestinations() throws IOException {
    String fragment = readTemplate("fragments/components.html");

    assertThat(fragment)
        .contains("th:fragment=\"customerBottomNavigation(activeItem)\"")
        .contains("href=\"/point-wallet\"")
        .contains("href=\"/home\"")
        .contains("href=\"/my-page\"");
  }

  @Test
  @DisplayName("고객 화면은 공통 하단 내비게이션과 콘텐츠 안전 여백을 사용한다")
  void affectedTemplatesShouldUseSharedNavigation() throws IOException {
    for (Map.Entry<String, String> entry : AFFECTED_TEMPLATES.entrySet()) {
      String html = readTemplate(entry.getKey());

      assertThat(html)
          .as("%s의 공통 하단 내비게이션", entry.getKey())
          .contains("customerBottomNavigation('" + entry.getValue() + "')")
          .contains("customer-bottom-nav-content");
    }
  }

  @Test
  @DisplayName("강제 결제 PIN 설정에서는 하단 내비게이션을 서버 렌더링하지 않는다")
  void requiredPaymentPinShouldConditionallyExcludeNavigation() throws IOException {
    String html = readTemplate("payment-pin-settings.html");

    assertThat(html)
        .contains("param.required[0] != '1'")
        .contains("th:block th:if=")
        .contains("customerBottomNavigation('myPage')");
  }

  private String readTemplate(String template) throws IOException {
    String resourcePath = "templates/" + template;
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertThat(inputStream).as("템플릿 리소스 %s", resourcePath).isNotNull();
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
