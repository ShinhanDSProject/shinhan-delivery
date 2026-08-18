package com.example.shinhandelivery.member.controller;

import com.example.shinhandelivery.common.security.WebAuthHelper;
import com.example.shinhandelivery.member.dto.response.MemberProfileResponse;
import com.example.shinhandelivery.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 회원(Member) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class MemberWebController {

  private final MemberService memberService;
  private final WebAuthHelper webAuthHelper;

  @GetMapping("/my-page")
  public String myPage(Model model) {
    webAuthHelper
        .getCurrentMemberId()
        .ifPresent(
            memberId -> {
              MemberProfileResponse profile =
                  MemberProfileResponse.from(memberService.getMyProfile(memberId));
              model.addAttribute("member", profile);
            });
    model.addAttribute("homePath", webAuthHelper.getHomePath());
    return "my-page";
  }

  @GetMapping("/profile-edit")
  public String profileEdit() {
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
