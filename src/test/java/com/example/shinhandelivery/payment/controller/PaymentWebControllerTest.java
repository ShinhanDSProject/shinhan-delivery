package com.example.shinhandelivery.payment.controller;

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
class PaymentWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("포인트 지갑 뷰 요청 시 point-wallet 뷰를 반환한다")
  void pointWalletReturnsView() throws Exception {
    mockMvc
        .perform(get("/point-wallet"))
        .andExpect(status().isOk())
        .andExpect(view().name("point-wallet"));
  }
}
