package com.example.shinhandelivery.member.controller;

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
class MemberWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("마이페이지 뷰 요청 시 my-page 뷰를 반환한다")
  void myPageReturnsView() throws Exception {
    mockMvc.perform(get("/my-page")).andExpect(status().isOk()).andExpect(view().name("my-page"));
  }

  @Test
  @DisplayName("로그인 뷰 요청 시 login 뷰를 반환한다")
  void loginReturnsView() throws Exception {
    mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
  }
}
