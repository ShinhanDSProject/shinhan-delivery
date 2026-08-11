package com.example.shinhandelivery.payment.controller;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.member.dto.response.MemberProfileResponseDto;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 결제/포인트(Payment/Point) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class PaymentWebController {

  private final PaymentService paymentService;
  private final MemberService memberService;

  @GetMapping("/point-wallet")
  public String pointWallet(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
    if (principal == null) {
      return "redirect:/login";
    }
    model.addAttribute("balance", paymentService.getOrCreateWallet(principal.getId()).getBalance());
    return "point-wallet";
  }

  @GetMapping("/point-charge")
  public String pointCharge(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
    if (principal == null) {
      return "redirect:/login";
    }
    model.addAttribute("balance", paymentService.getOrCreateWallet(principal.getId()).getBalance());
    return "point-charge";
  }

  @GetMapping("/payment-confirmation")
  public String paymentConfirmation() {
    return "payment-confirmation";
  }

  @GetMapping("/payment-pin")
  public String paymentPin() {
    return "payment-pin";
  }

  @GetMapping("/payment-pin-settings")
  public String paymentPinSettings(
      Model model, @AuthenticationPrincipal CustomUserDetails principal) {
    if (principal == null) {
      return "redirect:/login";
    }
    boolean hasPaymentPin =
        MemberProfileResponseDto.from(memberService.getMyProfile(principal.getId()))
            .hasPaymentPin();
    model.addAttribute("hasPaymentPin", hasPaymentPin);
    return "payment-pin-settings";
  }

  @GetMapping("/payment-complete")
  public String paymentComplete() {
    return "payment-complete";
  }
}
