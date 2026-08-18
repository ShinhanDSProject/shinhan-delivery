package com.example.shinhandelivery.notification.controller;

import com.example.shinhandelivery.common.security.WebAuthHelper;
import com.example.shinhandelivery.notice.dto.response.NoticeResponse;
import com.example.shinhandelivery.notice.service.NoticeService;
import com.example.shinhandelivery.notification.dto.response.NotificationResponse;
import com.example.shinhandelivery.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 알림(Notification) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class NotificationWebController {

  private final NotificationService notificationService;
  private final NoticeService noticeService;
  private final WebAuthHelper webAuthHelper;

  @GetMapping("/notifications")
  public String notifications(Model model) {
    List<NoticeResponse> notices =
        noticeService.list(null, PageRequest.of(0, 30)).getContent().stream()
            .map(NoticeResponse::from)
            .toList();
    model.addAttribute("notices", notices);

    webAuthHelper
        .getCurrentMemberId()
        .ifPresent(
            memberId -> {
              List<NotificationResponse> notifications =
                  notificationService
                      .list(memberId, null, PageRequest.of(0, 30))
                      .getContent()
                      .stream()
                      .map(NotificationResponse::from)
                      .toList();
              model.addAttribute("notifications", notifications);
            });
    return "notifications";
  }
}
