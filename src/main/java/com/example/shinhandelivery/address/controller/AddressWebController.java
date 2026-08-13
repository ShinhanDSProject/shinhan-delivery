package com.example.shinhandelivery.address.controller;

import com.example.shinhandelivery.address.service.AddressService;
import com.example.shinhandelivery.common.annotation.CurrentUserId;
import com.example.shinhandelivery.common.security.WebSecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 주소(Address) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class AddressWebController {

  private final AddressService addressService;

  @GetMapping("/address-management")
  public String addressManagement(@CurrentUserId Long userId, Model model) {
    if (userId != null) {
      WebSecurityUtils.safeAddAttribute(model, "addresses", () -> addressService.list(userId));
    }
    return "address-management";
  }

  @GetMapping("/address-input")
  public String addressInput() {
    return "address-input";
  }

  @GetMapping("/destination-map")
  public String destinationMap() {
    return "destination-map";
  }
}
