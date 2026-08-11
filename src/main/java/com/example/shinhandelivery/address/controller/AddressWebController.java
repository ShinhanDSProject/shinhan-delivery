package com.example.shinhandelivery.address.controller;

import com.example.shinhandelivery.address.dto.response.AddressResponse;
import com.example.shinhandelivery.address.service.AddressService;
import com.example.shinhandelivery.common.security.CustomUserDetails;
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
      @AuthenticationPrincipal CustomUserDetails principal, Model model) {
    return renderAddressView("address-management", principal, model);
  }

  @GetMapping("/address-input")
  public String addressInput(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
    return renderAddressView("address-input", principal, model);
  }

  @GetMapping("/destination-map")
  public String destinationMap() {
    return "destination-map";
  }

  private String renderAddressView(String viewName, CustomUserDetails principal, Model model) {
    if (principal == null || principal.getId() == null) {
      return "redirect:/login";
    }
    model.addAttribute(
        "addresses",
        addressService.list(principal.getId()).stream().map(AddressResponse::from).toList());
    return viewName;
  }
}
