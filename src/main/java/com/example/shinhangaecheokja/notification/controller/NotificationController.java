package com.example.shinhangaecheokja.notification.controller;

import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.notification.dto.response.NotificationResponse;
import com.example.shinhangaecheokja.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 회원 본인의 알림 목록 조회/읽음 처리 API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

  private final NotificationService notificationService;

  /** 로그인 회원 본인의 알림을 최신순으로 페이징 조회한다. category로 선택적 필터링이 가능하다. */
  @GetMapping
  public ResponseEntity<Page<NotificationResponse>> getNotifications(
      @AuthenticationPrincipal CustomUserDetails principal,
      @RequestParam(required = false) String category,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<NotificationResponse> responses =
        notificationService
            .getNotifications(principal.getId(), category, pageable)
            .map(NotificationResponse::from);
    return ResponseEntity.ok(responses);
  }

  /** 알림을 읽음 처리한다. 본인 알림이 아니면 거절된다. */
  @PatchMapping("/{id}/read")
  public ResponseEntity<NotificationResponse> markAsRead(
      @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
    return ResponseEntity.ok(
        NotificationResponse.from(notificationService.markAsRead(id, principal.getId())));
  }
}
