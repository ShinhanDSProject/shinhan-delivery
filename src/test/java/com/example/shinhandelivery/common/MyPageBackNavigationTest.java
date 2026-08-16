package com.example.shinhandelivery.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MyPageBackNavigationTest {

  @Test
  @DisplayName("마이페이지는 접근 가능한 공통 뒤로가기 버튼을 렌더링한다")
  void myPageShouldRenderSharedBackButton() throws IOException {
    String myPage = readResource("templates/my-page.html");
    String components = readResource("templates/fragments/components.html");

    assertThat(myPage).contains("safeBackButton('/home')").contains("/js/safe-back-navigation.js");
    assertThat(components)
        .contains("th:fragment=\"safeBackButton(fallbackPath)\"")
        .contains("aria-label=\"이전 화면으로 돌아가기\"")
        .contains("data-safe-back-button")
        .contains("data-fallback-path=${fallbackPath}");
  }

  @Test
  @DisplayName("뒤로가기는 동일 출처의 안전한 화면만 허용하고 나머지는 홈으로 대체한다")
  void backNavigationShouldGuardUnsafeHistory() throws IOException {
    String script = readResource("static/js/safe-back-navigation.js");

    assertThat(script)
        .contains("referrerUrl.origin === window.location.origin")
        .contains("BLOCKED_RETURN_PATHS")
        .contains("window.history.back()")
        .contains("button.dataset.fallbackPath || \"/home\"");
  }

  @Test
  @DisplayName("마이페이지의 기존 하단 이동 경로는 공통 내비게이션으로 유지한다")
  void myPageShouldKeepBottomNavigation() throws IOException {
    String myPage = readResource("templates/my-page.html");

    assertThat(myPage).contains("customerBottomNavigation('myPage')");
  }

  private String readResource(String resourcePath) throws IOException {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertThat(inputStream).as("리소스 %s", resourcePath).isNotNull();
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
