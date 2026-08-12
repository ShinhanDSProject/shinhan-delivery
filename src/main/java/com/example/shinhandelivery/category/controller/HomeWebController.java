package com.example.shinhandelivery.category.controller;

import com.example.shinhandelivery.category.dto.response.CategoryResponse;
import com.example.shinhandelivery.category.service.CategoryService;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 홈 화면의 서버 사이드 렌더링(SSR)을 제공하는 뷰 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class HomeWebController {

  private final CategoryService categoryService;
  private final MemberService memberService;

  @GetMapping("/home")
  public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    List<CategoryResponse> categories =
        categoryService.list().stream().map(CategoryResponse::from).toList();
    model.addAttribute("categories", categories);

    if (userDetails != null && userDetails.getId() != null) {
      try {
        Member member = memberService.getMyProfile(userDetails.getId());
        model.addAttribute("member", member);
      } catch (Exception ignored) {
      }
    }
    return "home";
  }
}
