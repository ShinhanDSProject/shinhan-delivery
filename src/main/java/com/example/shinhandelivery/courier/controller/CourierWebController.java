package com.example.shinhandelivery.courier.controller;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.courier.dto.response.CourierHomePageResponse;
import com.example.shinhandelivery.courier.service.CourierHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 배송원(Courier) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class CourierWebController {

  private final CourierHomeService courierHomeService;

  @GetMapping("/courier-home")
  public String courierHome(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
    if (principal == null) {
      return "redirect:/courier-login";
    }

    CourierHomePageResponse homePage = courierHomeService.load(principal.getId());
    model.addAttribute("memberName", homePage.memberName());
    model.addAttribute("transportMode", homePage.transportMode());
    model.addAttribute("workStatus", homePage.workStatus());
    model.addAttribute("isOnline", homePage.isOnline());
    model.addAttribute("latitude", homePage.latitude());
    model.addAttribute("longitude", homePage.longitude());
    model.addAttribute("availableDeliveries", homePage.availableDeliveries());
    model.addAttribute("availableCount", homePage.availableCount());

    return "courier-home";
  }

  @GetMapping("/courier-login")
  public String courierLogin() {
    return "courier-login";
  }

  @GetMapping("/courier-signup")
  public String courierSignup() {
    return "courier-signup";
  }

  @GetMapping("/pickup-guide")
  public String pickupGuide() {
    return "pickup-guide";
  }

  @GetMapping("/pickup-map")
  public String pickupMap() {
    return "pickup-map";
  }
}
