package com.example.shinhandelivery.payment.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class PaymentWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("포인트 지갑 뷰 요청 시 point-wallet 뷰를 반환한다")
  void pointWalletReturnsView() throws Exception {
    mockMvc
        .perform(get("/point-wallet"))
        .andExpect(status().isOk())
        .andExpect(view().name("point-wallet"))
        .andExpect(content().string(containsString("class=\"customer-bottom-nav\"")));
  }

  @Test
  @DisplayName("일반 결제 PIN 설정 화면에는 고객 하단 내비게이션을 표시한다")
  void paymentPinSettingsShowsNavigationForNormalEntry() throws Exception {
    mockMvc
        .perform(get("/payment-pin-settings"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("class=\"customer-bottom-nav\"")));
  }

  @Test
  @DisplayName("필수 결제 PIN 설정 화면에서는 고객 하단 내비게이션을 표시하지 않는다")
  void paymentPinSettingsHidesNavigationWhenRequired() throws Exception {
    mockMvc
        .perform(get("/payment-pin-settings").param("required", "1"))
        .andExpect(status().isOk())
        .andExpect(content().string(not(containsString("class=\"customer-bottom-nav\""))));
  }
}
