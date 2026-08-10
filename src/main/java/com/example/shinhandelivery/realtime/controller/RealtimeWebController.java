package com.example.shinhandelivery.realtime.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 실시간 위치 추적(Realtime Tracking) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class RealtimeWebController {

  @GetMapping({"/realtime-tracking", "/realtime-tracking.html"})
  public String realtimeTracking() {
    return "realtime-tracking";
  }
}
