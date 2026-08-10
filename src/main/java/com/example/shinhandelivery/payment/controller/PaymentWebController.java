package com.example.shinhandelivery.payment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 결제/포인트(Payment/Point) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class PaymentWebController {

  @GetMapping({"/point-wallet", "/point-wallet.html"})
  public String pointWallet() {
    return "point-wallet";
  }

  @GetMapping({"/point-charge", "/point-charge.html"})
  public String pointCharge() {
    return "point-charge";
  }

  @GetMapping({"/payment-confirmation", "/payment-confirmation.html"})
  public String paymentConfirmation() {
    return "payment-confirmation";
  }

  @GetMapping({"/payment-pin", "/payment-pin.html"})
  public String paymentPin() {
    return "payment-pin";
  }

  @GetMapping({"/payment-pin-settings", "/payment-pin-settings.html"})
  public String paymentPinSettings() {
    return "payment-pin-settings";
  }

  @GetMapping({"/payment-complete", "/payment-complete.html"})
  public String paymentComplete() {
    return "payment-complete";
  }
}
