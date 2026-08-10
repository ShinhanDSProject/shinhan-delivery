package com.example.shinhandelivery.delivery.controller;

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
class DeliveryWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("배송 내역 뷰 요청 시 delivery-history 뷰를 반환한다")
  void deliveryHistoryReturnsView() throws Exception {
    mockMvc
        .perform(get("/delivery-history"))
        .andExpect(status().isOk())
        .andExpect(view().name("delivery-history"));
  }
}
