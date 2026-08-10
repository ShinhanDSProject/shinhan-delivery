package com.example.shinhandelivery.courier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 배송원(Courier) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class CourierWebController {

  @GetMapping({"/courier-home", "/courier-home.html"})
  public String courierHome() {
    return "courier-home";
  }

  @GetMapping({"/courier-login", "/courier-login.html"})
  public String courierLogin() {
    return "courier-login";
  }

  @GetMapping({"/courier-signup", "/courier-signup.html"})
  public String courierSignup() {
    return "courier-signup";
  }

  @GetMapping({"/pickup-guide", "/pickup-guide.html"})
  public String pickupGuide() {
    return "pickup-guide";
  }

  @GetMapping({"/pickup-map", "/pickup-map.html"})
  public String pickupMap() {
    return "pickup-map";
  }
}
