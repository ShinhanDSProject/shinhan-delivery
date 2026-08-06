package com.example.shinhandelivery.notice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.common.security.JwtProvider;
import com.example.shinhandelivery.common.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoticePageController.class)
@Import(SecurityConfig.class)
class NoticePageControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtProvider jwtProvider;

  @Test
  @DisplayName("공지사항 경로는 공통 프래그먼트를 조립한 관리 화면을 렌더링한다")
  void showAnnouncementsSuccess() throws Exception {
    mockMvc
        .perform(get("/announcements"))
        .andExpect(status().isOk())
        .andExpect(view().name("notices/announcements"))
        .andExpect(content().string(containsString("id=\"createButton\"")))
        .andExpect(content().string(containsString("id=\"noticeForm\"")))
        .andExpect(content().string(containsString("/css/design-system.css")));
  }

  @Test
  @DisplayName("기존 공지사항 HTML 경로도 동일한 Thymeleaf 화면을 렌더링한다")
  void showAnnouncementsLegacyPathSuccess() throws Exception {
    mockMvc
        .perform(get("/announcements.html"))
        .andExpect(status().isOk())
        .andExpect(view().name("notices/announcements"));
  }
}
