package com.example.shinhandelivery.courier.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CourierWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("고객 계정으로 배송원 홈 SSR 페이지 요청 시 고객 홈으로 리다이렉트한다")
  void courierHomeRedirectsCustomerToHome() throws Exception {
    CustomUserDetails customer =
        new CustomUserDetails(1L, "customer@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities());

    mockMvc
        .perform(get("/courier-home").with(authentication(auth)))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"));
  }

  @Test
  @DisplayName("배송원 계정으로 배송원 홈 SSR 페이지 요청 시 courier-home 뷰를 반환한다")
  void courierHomeReturnsViewForCourier() throws Exception {
    CustomUserDetails courier = new CustomUserDetails(1L, "courier@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(courier, null, courier.getAuthorities());

    mockMvc
        .perform(get("/courier-home").with(authentication(auth)))
        .andExpect(status().isOk())
        .andExpect(view().name("courier-home"));
  }
}
