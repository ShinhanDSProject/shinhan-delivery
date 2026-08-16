package com.example.shinhandelivery.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointWalletNavigationTest {

  private static final List<String> MY_PAGE_TEMPLATES =
      List.of(
          "my-page.html",
          "announcements.html",
          "address-management.html",
          "change-password.html",
          "profile-edit.html",
          "delivery-history.html");

  @Test
  @DisplayName("마이페이지 계열 화면의 포인트 버튼은 포인트 지갑으로 이동한다")
  void pointNavigationShouldLinkToPointWallet() throws IOException {
    for (String template : MY_PAGE_TEMPLATES) {
      String html = readTemplate(template);

      assertThat(html)
          .as("%s의 포인트 버튼 이동 경로", template)
          .contains("href=\"/point-wallet\" aria-label=\"포인트\"")
          .doesNotContain("href=\"/my-page\" aria-label=\"포인트\"");
    }
  }

  private String readTemplate(String template) throws IOException {
    String resourcePath = "templates/" + template;
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertThat(inputStream).as("템플릿 리소스 %s", resourcePath).isNotNull();
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
