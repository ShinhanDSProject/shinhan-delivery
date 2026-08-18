package com.example.shinhandelivery.payment.controller;

import com.example.shinhandelivery.common.security.WebAuthHelper;
import com.example.shinhandelivery.payment.dto.response.PointWalletResponse;
import com.example.shinhandelivery.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 결제/포인트(Payment/Point) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class PaymentWebController {

  private final PaymentService paymentService;
  private final WebAuthHelper webAuthHelper;

  @GetMapping("/point-wallet")
  public String pointWallet(Model model) {
    webAuthHelper
        .getCurrentMemberId()
        .ifPresent(
            memberId -> {
              paymentService
                  .findWalletByMemberId(memberId)
                  .ifPresent(
                      wallet ->
                          model.addAttribute("pointWallet", PointWalletResponse.from(wallet)));
            });
    model.addAttribute("homePath", webAuthHelper.getHomePath());
    return "point-wallet";
  }

  @GetMapping("/point-charge")
  public String pointCharge() {
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
