package com.example.shinhangaecheokja.notice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.notice.entity.Notice;
import com.example.shinhangaecheokja.notice.exception.NoticeNotFoundException;
import com.example.shinhangaecheokja.notice.service.NoticeService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoticeController.class)
class NoticeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private NoticeService noticeService;

  @Test
  @DisplayName("GET /api/v1/notices 요청 시 공지사항 목록 페이징 결과를 반환한다")
  void getNoticesSuccess() throws Exception {
    Notice notice =
        Notice.builder()
            .id(1L)
            .title("[안내] 서비스 점검 안내")
            .content("내용")
            .category("SYSTEM")
            .isPinned(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    when(noticeService.list(any(), any())).thenReturn(new PageImpl<>(List.of(notice)));

    mockMvc
        .perform(get("/api/v1/notices"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].title").value("[안내] 서비스 점검 안내"))
        .andExpect(jsonPath("$.content[0].category").value("SYSTEM"))
        .andExpect(jsonPath("$.content[0].isPinned").value(true));
  }

  @Test
  @DisplayName("GET /api/v1/notices/{id} 요청 시 공지사항 상세 정보를 반환한다")
  void getNoticeDetailSuccess() throws Exception {
    Notice detail =
        Notice.builder()
            .id(1L)
            .title("[안내] 서비스 점검 안내")
            .content("점검 본문 내용입니다.")
            .category("SYSTEM")
            .isPinned(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    when(noticeService.getById(1L)).thenReturn(detail);

    mockMvc
        .perform(get("/api/v1/notices/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("[안내] 서비스 점검 안내"))
        .andExpect(jsonPath("$.content").value("점검 본문 내용입니다."));
  }

  @Test
  @DisplayName("존재하지 않는 공지사항 ID로 GET /api/v1/notices/{id} 요청 시 404 에러를 반환한다")
  void getNoticeDetailNotFound() throws Exception {
    when(noticeService.getById(999L)).thenThrow(new NoticeNotFoundException());

    mockMvc
        .perform(get("/api/v1/notices/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("N002"))
        .andExpect(jsonPath("$.message").value("존재하지 않는 공지사항입니다."));
  }
}
