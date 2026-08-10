package com.example.shinhandelivery.payment.controller;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.payment.entity.PointWallet;
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

  @GetMapping("/point-wallet")
  public String pointWallet(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    if (userDetails != null && userDetails.getId() != null) {
      try {
        PointWallet wallet = paymentService.getByMemberId(userDetails.getId());
        model.addAttribute(
            "wallet", wallet != null ? wallet : PointWallet.createEmpty(userDetails.getId()));
      } catch (Exception ignored) {
      }
    }
    return "point-wallet";
  }

  @GetMapping("/point-charge")
  public String pointCharge(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
    if (userDetails != null && userDetails.getId() != null) {
      try {
        PointWallet wallet = paymentService.getByMemberId(userDetails.getId());
        model.addAttribute(
            "wallet", wallet != null ? wallet : PointWallet.createEmpty(userDetails.getId()));
      } catch (Exception ignored) {
      }
    }
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
  public String paymentPinSettings() {
    return "payment-pin-settings";
  }

  @GetMapping("/payment-complete")
  public String paymentComplete() {
    return "payment-complete";
  }
}
