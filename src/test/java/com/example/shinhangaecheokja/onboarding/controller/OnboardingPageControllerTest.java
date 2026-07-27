package com.example.shinhangaecheokja.onboarding.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OnboardingPageControllerTest {

  private final OnboardingPageController onboardingPageController = new OnboardingPageController();

  @Test
  void 루트_요청이면_온보딩_정적_화면으로_전달한다() {
    String viewName = onboardingPageController.showOnboarding();

    assertThat(viewName).isEqualTo("forward:/onboarding/index.html");
  }
}
