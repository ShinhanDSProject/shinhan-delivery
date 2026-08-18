package com.example.shinhandelivery.address.controller;

import com.example.shinhandelivery.address.dto.response.AddressResponse;
import com.example.shinhandelivery.address.service.AddressService;
import com.example.shinhandelivery.common.security.WebAuthHelper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 주소(Address) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class AddressWebController {

  private final AddressService addressService;
  private final WebAuthHelper webAuthHelper;

  @GetMapping("/address-management")
  public String addressManagement(Model model) {
    webAuthHelper
        .getCurrentMemberId()
        .ifPresent(
            memberId -> {
              List<AddressResponse> addresses =
                  addressService.list(memberId).stream().map(AddressResponse::from).toList();
              model.addAttribute("addresses", addresses);
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
