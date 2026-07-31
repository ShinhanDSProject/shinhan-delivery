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
import com.example.shinhangaecheokja.notification.entity.Notification;
import com.example.shinhangaecheokja.notification.exception.NotificationAccessDeniedException;
import com.example.shinhangaecheokja.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("인증 토큰이 없으면 403을 반환한다")
  void getNotificationsUnauthenticatedShouldReturn403() throws Exception {
    mockMvc.perform(get("/api/v1/notifications")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("인증된 회원은 본인 알림 목록을 조회한다")
  void getNotificationsAuthenticatedShouldReturnNotifications() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    Notification notification = new Notification();
    notification.setId(1L);
    notification.setMemberId(1L);
    notification.setTitle("제목");
    notification.setMessage("내용");
    notification.setCategory("DELIVERY");
    notification.setRead(false);
    notification.setCreatedAt(LocalDateTime.now());

    when(notificationService.list(eq(1L), isNull(), any()))
        .thenReturn(new PageImpl<>(List.of(notification)));

    mockMvc
        .perform(get("/api/v1/notifications").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("제목"));
  }

  @Test
  @DisplayName("읽음 처리에 성공하면 read가 true인 알림을 반환한다")
  void markAsReadSuccess() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    Notification updated = new Notification();
    updated.setId(1L);
    updated.setMemberId(1L);
    updated.setTitle("제목");
    updated.setMessage("내용");
    updated.setCategory("DELIVERY");
    updated.setRead(true);
    updated.setCreatedAt(LocalDateTime.now());

    when(notificationService.markAsRead(1L, 1L)).thenReturn(updated);

    mockMvc
        .perform(patch("/api/v1/notifications/1/read").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.read").value(true));
  }

  @Test
  @DisplayName("본인 알림이 아니면 403을 반환한다")
  void markAsReadForbiddenShouldReturn403() throws Exception {
    String token = jwtProvider.createAccessToken(2L, "stranger@test.com", "CUSTOMER");
    when(notificationService.markAsRead(2L, 1L))
        .thenThrow(new NotificationAccessDeniedException(1L, 2L));

    mockMvc
        .perform(patch("/api/v1/notifications/1/read").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("C007"));
  }
}
