package com.example.shinhandelivery.address.controller;

import com.example.shinhandelivery.address.entity.Address;
import com.example.shinhandelivery.address.service.AddressService;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.common.security.WebSecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 주소(Address) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class AddressWebController {

  private final AddressService addressService;

  @GetMapping("/address-management")
  public String addressManagement(
      @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    WebSecurityUtils.getUserId(userDetails)
        .ifPresent(
            userId -> {
              try {
                List<Address> addresses = addressService.list(userId);
                model.addAttribute("addresses", addresses);
              } catch (Exception ignored) {
              }
            });
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
