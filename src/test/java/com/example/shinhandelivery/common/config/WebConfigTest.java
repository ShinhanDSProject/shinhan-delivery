package com.example.shinhandelivery.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("루트 경로(\"/\")로 접속하면 홈 화면(/home)으로 리다이렉트된다")
  void rootPathRedirectsToHomePage() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"));
  }

  @Test
  @DisplayName("index.html 경로(\"/index.html\")로 접속하면 홈 화면(/home)으로 리다이렉트된다")
  void indexPathRedirectsToHomePage() throws Exception {
    mockMvc
        .perform(get("/index.html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"));
  }
}
