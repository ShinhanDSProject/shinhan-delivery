package com.example.shinhandelivery.notice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 공지사항 목록·상세 및 관리자 관리 화면을 제공하는 Thymeleaf 컨트롤러입니다. */
@Controller
public class NoticePageController {

  /** 공지사항 통합 화면을 렌더링합니다. */
  @GetMapping({"/announcements", "/announcements.html"})
  public String showAnnouncements() {
    return "notices/announcements";
  }
}
