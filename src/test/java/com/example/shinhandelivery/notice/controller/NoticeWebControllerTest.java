package com.example.shinhandelivery.notice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.notice.entity.Notice;
import com.example.shinhandelivery.notice.service.NoticeService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NoticeWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private NoticeService noticeService;

  @Test
  @DisplayName("공지사항 SSR 페이지 요청 시 announcements 뷰와 모델 데이터를 반환한다")
  void announcementsReturnsViewAndModel() throws Exception {
    Notice notice = Notice.builder().title("테스트 공지사항").content("내용").isPinned(false).build();
    given(noticeService.list(any(), any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(notice)));

    mockMvc
        .perform(get("/announcements"))
        .andExpect(status().isOk())
        .andExpect(view().name("announcements"))
        .andExpect(model().attributeExists("notices"));
  }
}
