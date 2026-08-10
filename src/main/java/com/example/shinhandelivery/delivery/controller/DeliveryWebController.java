package com.example.shinhandelivery.delivery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 배송(Delivery) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
public class DeliveryWebController {

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

  @GetMapping({"/matching-wait", "/matching-wait.html"})
  public String matchingWait() {
    return "matching-wait";
  }

  @GetMapping({"/matching-complete", "/matching-complete.html"})
  public String matchingComplete() {
    return "matching-complete";
  }
}
