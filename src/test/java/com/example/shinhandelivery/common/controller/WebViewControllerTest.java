package com.example.shinhandelivery.common.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("스타일 가이드 뷰 요청 시 style-guide 뷰를 반환한다")
  void styleGuideReturnsView() throws Exception {
    mockMvc
        .perform(get("/style-guide"))
        .andExpect(status().isOk())
        .andExpect(view().name("style-guide"));
  }

  @Test
  @DisplayName("마이페이지 뷰 요청 시 my-page 뷰를 반환한다")
  void myPageReturnsView() throws Exception {
    mockMvc.perform(get("/my-page")).andExpect(status().isOk()).andExpect(view().name("my-page"));
  }

  @Test
  @DisplayName("포인트 지갑 뷰 요청 시 point-wallet 뷰를 반환한다")
  void pointWalletReturnsView() throws Exception {
    mockMvc
        .perform(get("/point-wallet"))
        .andExpect(status().isOk())
        .andExpect(view().name("point-wallet"));
  }

  @Test
  @DisplayName("온보딩 뷰 요청 시 onboarding/index 뷰를 반환한다")
  void onboardingReturnsView() throws Exception {
    mockMvc
        .perform(get("/onboarding"))
        .andExpect(status().isOk())
        .andExpect(view().name("onboarding/index"));
  }
}
