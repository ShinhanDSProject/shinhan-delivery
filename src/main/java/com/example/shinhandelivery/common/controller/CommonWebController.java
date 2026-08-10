package com.example.shinhandelivery.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 공통 시스템 UI 뷰(스타일가이드, 온보딩, 대시보드 등)의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class CommonWebController {

  @GetMapping({"/style-guide", "/style-guide.html"})
  public String styleGuide() {
    return "style-guide";
  }

  @GetMapping({"/langgraph-opensource-dashboard", "/langgraph-opensource-dashboard.html"})
  public String langgraphDashboard() {
    return "langgraph-opensource-dashboard";
  }

  @GetMapping({"/langgraph-visualization", "/langgraph-visualization.html"})
  public String langgraphVisualization() {
    return "langgraph-visualization";
  }

  @GetMapping({"/onboarding", "/onboarding/", "/onboarding/index.html"})
  public String onboarding() {
    return "onboarding/index";
  }
}
