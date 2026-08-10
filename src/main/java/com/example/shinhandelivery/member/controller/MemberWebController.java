package com.example.shinhandelivery.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 회원(Member) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class MemberWebController {

  @GetMapping({"/my-page", "/my-page.html"})
  public String myPage() {
    return "my-page";
  }

  @GetMapping({"/profile-edit", "/profile-edit.html"})
  public String profileEdit() {
    return "profile-edit";
  }

  @GetMapping({"/change-password", "/change-password.html"})
  public String changePassword() {
    return "change-password";
  }

  @GetMapping({"/role-selection", "/role-selection.html"})
  public String roleSelection() {
    return "role-selection";
  }

  @GetMapping({"/login", "/login.html"})
  public String login() {
    return "login";
  }

  @GetMapping({"/signup", "/signup.html"})
  public String signup() {
    return "signup";
  }
}
