package com.example.shinhandelivery.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminWebController.class)
class AdminWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser
  @DisplayName("GET /admin-login 요청 시 admin-login 뷰 템플릿을 반환한다")
  void adminLoginReturnsView() throws Exception {
    mockMvc
        .perform(get("/admin-login"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin-login"));
  }

  @Test
  @WithMockUser
  @DisplayName("GET /admin-dashboard 요청 시 admin-dashboard 뷰 템플릿을 반환한다")
  void adminDashboardReturnsView() throws Exception {
    mockMvc
        .perform(get("/admin-dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin-dashboard"));
  }
}
