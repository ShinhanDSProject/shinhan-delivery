package com.example.shinhandelivery.notice.controller;

import com.example.shinhandelivery.notice.dto.response.NoticeResponse;
import com.example.shinhandelivery.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 공지사항 화면의 서버 사이드 렌더링(SSR)을 제공하는 뷰 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class NoticeWebController {

  private final NoticeService noticeService;

  @GetMapping("/announcements")
  public String announcements(@RequestParam(required = false) String category, Model model) {
    Page<NoticeResponse> notices =
        noticeService.list(category, PageRequest.of(0, 50)).map(NoticeResponse::from);
    model.addAttribute("notices", notices.getContent());
    return "announcements";
  }
}
