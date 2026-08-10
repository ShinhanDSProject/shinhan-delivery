package com.example.shinhandelivery.notification.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 알림(Notification) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class NotificationWebController {

  @GetMapping({"/notifications", "/notifications.html"})
  public String notifications() {
    return "notifications";
  }
}
