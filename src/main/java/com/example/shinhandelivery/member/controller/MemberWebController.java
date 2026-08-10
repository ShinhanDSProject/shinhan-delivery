package com.example.shinhandelivery.member.controller;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 회원(Member) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class MemberWebController {

  private final MemberService memberService;

  @GetMapping("/my-page")
  public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    if (userDetails != null && userDetails.getId() != null) {
      try {
        Member member = memberService.getMyProfile(userDetails.getId());
        model.addAttribute("member", member);
      } catch (Exception ignored) {
      }
    }
    return "my-page";
  }

  @GetMapping("/profile-edit")
  public String profileEdit(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    if (userDetails != null && userDetails.getId() != null) {
      try {
        Member member = memberService.getMyProfile(userDetails.getId());
        model.addAttribute("member", member);
      } catch (Exception ignored) {
      }
    }
    return "profile-edit";
  }

  @GetMapping("/change-password")
  public String changePassword() {
    return "change-password";
  }

  @GetMapping("/role-selection")
  public String roleSelection() {
    return "role-selection";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/signup")
  public String signup() {
    return "signup";
  }
}
