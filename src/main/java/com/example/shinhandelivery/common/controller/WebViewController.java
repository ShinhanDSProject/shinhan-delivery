package com.example.shinhandelivery.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 프로젝트 내 UI 뷰 페이지들에 대한 Thymeleaf SSR 라우팅을 제공하는 뷰 컨트롤러입니다. */
@Controller
public class WebViewController {

  @GetMapping({"/style-guide", "/style-guide.html"})
  public String styleGuide() {
    return "style-guide";
  }

  @GetMapping({"/my-page", "/my-page.html"})
  public String myPage() {
    return "my-page";
  }

  @GetMapping({"/point-wallet", "/point-wallet.html"})
  public String pointWallet() {
    return "point-wallet";
  }

  @GetMapping({"/point-charge", "/point-charge.html"})
  public String pointCharge() {
    return "point-charge";
  }

  @GetMapping({"/delivery-history", "/delivery-history.html"})
  public String deliveryHistory() {
    return "delivery-history";
  }

  @GetMapping({"/delivery-cancel-list", "/delivery-cancel-list.html"})
  public String deliveryCancelList() {
    return "delivery-cancel-list";
  }

  @GetMapping({"/delivery-detail", "/delivery-detail.html"})
  public String deliveryDetail() {
    return "delivery-detail";
  }

  @GetMapping({"/notifications", "/notifications.html"})
  public String notifications() {
    return "notifications";
  }

  @GetMapping({"/role-selection", "/role-selection.html"})
  public String roleSelection() {
    return "role-selection";
  }

  @GetMapping({"/profile-edit", "/profile-edit.html"})
  public String profileEdit() {
    return "profile-edit";
  }

  @GetMapping({"/change-password", "/change-password.html"})
  public String changePassword() {
    return "change-password";
  }

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

  @GetMapping({"/item-detail", "/item-detail.html"})
  public String itemDetail() {
    return "item-detail";
  }

  @GetMapping({"/door-photo", "/door-photo.html"})
  public String doorPhoto() {
    return "door-photo";
  }

  @GetMapping({"/cancel-detail", "/cancel-detail.html"})
  public String cancelDetail() {
    return "cancel-detail";
  }

  @GetMapping({"/pickup-guide", "/pickup-guide.html"})
  public String pickupGuide() {
    return "pickup-guide";
  }

  @GetMapping({"/pickup-map", "/pickup-map.html"})
  public String pickupMap() {
    return "pickup-map";
  }

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

  @GetMapping({"/login", "/login.html"})
  public String login() {
    return "login";
  }

  @GetMapping({"/signup", "/signup.html"})
  public String signup() {
    return "signup";
  }

  @GetMapping({"/realtime-tracking", "/realtime-tracking.html"})
  public String realtimeTracking() {
    return "realtime-tracking";
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

  @GetMapping({"/matching-wait", "/matching-wait.html"})
  public String matchingWait() {
    return "matching-wait";
  }

  @GetMapping({"/matching-complete", "/matching-complete.html"})
  public String matchingComplete() {
    return "matching-complete";
  }

  @GetMapping({"/langgraph-opensource-dashboard", "/langgraph-opensource-dashboard.html"})
  public String langgraphDashboard() {
    return "langgraph-opensource-dashboard";
  }

  @GetMapping({"/langgraph-visualization", "/langgraph-visualization.html"})
  public String langgraphVisualization() {
    return "langgraph-visualization";
  }

  @GetMapping({"/onboarding", "/onboarding/", "/onboarding/index.html"})
  public String onboarding() {
    return "onboarding/index";
  }
}
