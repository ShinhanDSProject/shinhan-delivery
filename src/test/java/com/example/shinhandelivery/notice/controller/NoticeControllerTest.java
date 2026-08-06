package com.example.shinhandelivery.notice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.security.JwtProvider;
import com.example.shinhandelivery.common.security.SecurityConfig;
import com.example.shinhandelivery.notice.dto.request.NoticeCreateRequest;
import com.example.shinhandelivery.notice.dto.request.NoticeUpdateRequest;
import com.example.shinhandelivery.notice.entity.Notice;
import com.example.shinhandelivery.notice.exception.NoticeNotFoundException;
import com.example.shinhandelivery.notice.service.NoticeService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoticeController.class)
@Import(SecurityConfig.class)
class NoticeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private NoticeService noticeService;

  @MockitoBean private JwtProvider jwtProvider;

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

  @Test
  @DisplayName("관리자는 POST /api/v1/notices로 공지사항을 생성한다")
  void createNoticeSuccess() throws Exception {
    Notice created = createNotice(4L, "새 공지", "새 본문", "SYSTEM", true);
    when(noticeService.create(any(NoticeCreateRequest.class))).thenReturn(created);

    mockMvc
        .perform(
            post("/api/v1/notices")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/notices/4"))
        .andExpect(jsonPath("$.id").value(4))
        .andExpect(jsonPath("$.title").value("새 공지"));
  }

  @Test
  @DisplayName("관리자는 PUT /api/v1/notices/{id}로 공지사항을 수정한다")
  void updateNoticeSuccess() throws Exception {
    Notice updated = createNotice(1L, "수정 공지", "수정 본문", "SERVICE", false);
    when(noticeService.update(eq(1L), any(NoticeUpdateRequest.class))).thenReturn(updated);

    mockMvc
        .perform(
            put("/api/v1/notices/1")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("수정 공지"));
  }

  @Test
  @DisplayName("관리자는 DELETE /api/v1/notices/{id}로 공지사항을 삭제한다")
  void deleteNoticeSuccess() throws Exception {
    doNothing().when(noticeService).delete(1L);

    mockMvc
        .perform(delete("/api/v1/notices/1").with(user("admin").roles("ADMIN")))
        .andExpect(status().isNoContent());

    verify(noticeService).delete(1L);
  }

  @Test
  @DisplayName("비로그인 사용자의 공지사항 생성 요청은 401을 반환한다")
  void createNoticeUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/notices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("A001"));
  }

  @Test
  @DisplayName("일반 사용자의 공지사항 수정 요청은 403을 반환한다")
  void updateNoticeForbidden() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/notices/1")
                .with(user("customer").roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("C007"));
  }

  @Test
  @DisplayName("필수 입력값이 없으면 공지사항 생성 요청은 400을 반환한다")
  void createNoticeInvalidInput() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/notices")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":" ","content":"본문","category":"INVALID","isPinned":true}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("C001"))
        .andExpect(jsonPath("$.errors").isArray());
  }

  @Test
  @DisplayName("존재하지 않는 공지사항 수정 요청은 404를 반환한다")
  void updateNoticeNotFound() throws Exception {
    when(noticeService.update(eq(999L), any(NoticeUpdateRequest.class)))
        .thenThrow(new NoticeNotFoundException());

    mockMvc
        .perform(
            put("/api/v1/notices/999")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("N002"));
  }

  private String validRequestJson() {
    return """
        {
          "title":"요청 제목",
          "content":"요청 본문",
          "category":"SYSTEM",
          "isPinned":true
        }
        """;
  }

  private Notice createNotice(
      Long id, String title, String content, String category, boolean isPinned) {
    return Notice.builder()
        .id(id)
        .title(title)
        .content(content)
        .category(category)
        .isPinned(isPinned)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
