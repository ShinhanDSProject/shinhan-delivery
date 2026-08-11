package com.example.shinhandelivery.home.controller;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.home.dto.response.HomePageResponse;
import com.example.shinhandelivery.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 홈 화면의 서버 사이드 렌더링(SSR)을 제공하는 뷰 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class HomeWebController {

  private final HomeService homeService;

  @GetMapping("/home")
  public String home(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
    if (principal == null) {
      return "redirect:/login";
    }

    HomePageResponse homePage = homeService.load(principal.getId());
    if (homePage.paymentPinRequired()) {
      return "redirect:/payment-pin-settings?required=1&returnUrl=/home";
    }

    model.addAttribute("categories", homePage.categories());
    model.addAttribute("memberName", homePage.memberName());
    model.addAttribute("activeDeliveries", homePage.activeDeliveries());
    model.addAttribute("waitingCount", homePage.waitingCount());
    model.addAttribute("hasUnreadNotification", homePage.hasUnreadNotification());
    return "home";
  }
}
