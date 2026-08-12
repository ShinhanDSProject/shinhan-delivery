package com.example.shinhandelivery.courier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 배송원(Courier) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class CourierWebController {

  @GetMapping("/courier-home")
  public String courierHome() {
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

  @GetMapping("/delivery-in-progress")
  public String deliveryInProgress() {
    return "delivery-in-progress";
  }
}
