package com.example.shinhandelivery.address.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 주소(Address) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class AddressWebController {

  @GetMapping({"/address-management", "/address-management.html"})
  public String addressManagement() {
    return "address-management";
  }

  @GetMapping({"/address-input", "/address-input.html"})
  public String addressInput() {
    return "address-input";
  }

  @GetMapping({"/destination-map", "/destination-map.html"})
  public String destinationMap() {
    return "destination-map";
  }
}
