package com.example.shinhandelivery.notification.controller;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.common.security.WebSecurityUtils;
import com.example.shinhandelivery.notice.dto.response.NoticeResponse;
import com.example.shinhandelivery.notice.service.NoticeService;
import com.example.shinhandelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
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
    WebSecurityUtils.ifAuthenticated(
        userDetails,
        userId ->
            WebSecurityUtils.safeAddAttribute(
                model,
                "notifications",
                () -> notificationService.list(userId, null, PageRequest.of(0, 30)).getContent()));

    WebSecurityUtils.safeAddAttribute(
        model,
        "notices",
        () ->
            noticeService.list(null, PageRequest.of(0, 30)).map(NoticeResponse::from).getContent());

    return "notifications";
  }
}
