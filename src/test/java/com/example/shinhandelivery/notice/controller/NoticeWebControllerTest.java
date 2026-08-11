package com.example.shinhandelivery.notice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    Notice notice = Notice.builder().id(1L).title("테스트 공지사항").content("내용").isPinned(false).build();
    given(noticeService.list(any(), any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(notice)));

    mockMvc
        .perform(get("/announcements"))
        .andExpect(status().isOk())
        .andExpect(view().name("announcements"))
        .andExpect(model().attributeExists("notices"))
        .andExpect(model().attributeDoesNotExist("selectedNotice"))
        .andExpect(content().string(containsString("테스트 공지사항")))
        .andExpect(content().string(containsString("/announcements?id=1")));
  }

  @Test
  @DisplayName("공지사항 ID가 주어지면 상세 DTO를 SSR 모델에 추가한다")
  void announcementsWithIdReturnsSelectedNotice() throws Exception {
    Notice notice =
        Notice.builder()
            .id(1L)
            .title("상세 공지사항")
            .content("상세 내용")
            .category("SYSTEM")
            .isPinned(true)
            .build();
    given(noticeService.list(any(), any(Pageable.class)))
        .willReturn(new PageImpl<>(List.of(notice)));
    given(noticeService.getById(1L)).willReturn(notice);

    mockMvc
        .perform(get("/announcements").param("id", "1"))
        .andExpect(status().isOk())
        .andExpect(view().name("announcements"))
        .andExpect(model().attributeExists("notices", "selectedNotice"))
        .andExpect(content().string(containsString("상세 공지사항")))
        .andExpect(content().string(containsString("상세 내용")))
        .andExpect(content().string(containsString("SYSTEM")));
  }
}
