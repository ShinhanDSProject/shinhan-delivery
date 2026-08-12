package com.example.shinhandelivery.notification.controller;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.notice.dto.response.NoticeResponse;
import com.example.shinhandelivery.notice.service.NoticeService;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 알림(Notification) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class NotificationWebController {

  private final NotificationService notificationService;
  private final NoticeService noticeService;

  @GetMapping("/notifications")
  public String notifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    if (userDetails != null && userDetails.getId() != null) {
      try {
        Page<Notification> notifications =
            notificationService.list(userDetails.getId(), null, PageRequest.of(0, 30));
        model.addAttribute("notifications", notifications.getContent());
      } catch (Exception ignored) {
      }
    }
    try {
      Page<NoticeResponse> notices =
          noticeService.list(null, PageRequest.of(0, 30)).map(NoticeResponse::from);
      model.addAttribute("notices", notices.getContent());
    } catch (Exception ignored) {
    }
    return "notifications";
  }
}
