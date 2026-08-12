package com.example.shinhandelivery.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 관리자(Admin) 전용 웹 화면 SSR 라우팅 컨트롤러. */
@Controller
public class AdminWebController {

  /** 관리자 전용 로그인 화면으로 이동한다. */
  @GetMapping("/admin-login")
  public String adminLogin() {
    return "admin-login";
  }

  /** 관리자 전용 대시보드 (배송원 자격 심사 등) 화면으로 이동한다. */
  @GetMapping("/admin-dashboard")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminDashboard() {
    return "admin-dashboard";
  }
}
