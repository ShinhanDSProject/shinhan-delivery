package com.example.shinhandelivery.courier.controller;

import com.example.shinhandelivery.common.security.WebAuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 배송원(Courier) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class CourierWebController {

  private final WebAuthHelper webAuthHelper;

  @GetMapping("/courier-home")
  public String courierHome() {
    if (webAuthHelper.currentUserHasRole("CUSTOMER")) {
      return "redirect:/home";
    }
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

  @GetMapping("/courier-pending")
  public String courierPending() {
    return "courier-pending";
  }

  @GetMapping("/pickup-guide")
  public String pickupGuide() {
    return "pickup-guide";
  }

  @GetMapping("/pickup-map")
  public String pickupMap() {
    return "pickup-map";
  }

  @GetMapping("/delivery-in-progress")
  public String deliveryInProgress() {
    return "delivery-in-progress";
  }

  @GetMapping("/courier-equipment")
  public String courierEquipment() {
    return "courier-equipment";
  }
}
