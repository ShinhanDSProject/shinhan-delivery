package com.example.shinhangaecheokja.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.notification.dto.response.NotificationResponse;
import com.example.shinhangaecheokja.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 이 저장소에서 실제 {@code @PreAuthorize} 인증 강제를 처음 쓰는 컨트롤러라, {@code @WebMvcTest} 슬라이스로는 보안 필터체인/메서드 시큐리티가
 * 실리지 않아 검증이 안 된다. 실제 {@code SecurityConfig}/{@code JwtProvider}까지 뜨는 {@code @SpringBootTest}로 인증
 * 흐름 자체를 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtProvider jwtProvider;

  @MockitoBean private NotificationService notificationService;

  @Test
  void 인증_토큰이_없으면_403을_반환한다() throws Exception {
    mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isForbidden());
  }

  @Test
  void 인증된_회원은_본인_알림_목록을_조회한다() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    NotificationResponse notification =
        new NotificationResponse(1L, "제목", "내용", "DELIVERY", false, LocalDateTime.now());
    when(notificationService.getNotifications(eq(1L), isNull(), any()))
        .thenReturn(new PageImpl<>(List.of(notification)));

    mockMvc
        .perform(get("/api/v1/notifications").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("제목"));
  }

  @Test
  void 읽음_처리에_성공하면_read가_true인_알림을_반환한다() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    NotificationResponse updated =
        new NotificationResponse(1L, "제목", "내용", "DELIVERY", true, LocalDateTime.now());
    when(notificationService.markAsRead(1L, 1L)).thenReturn(updated);

    mockMvc
        .perform(patch("/api/v1/notifications/1/read").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.read").value(true));
  }
}
