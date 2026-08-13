package com.example.shinhandelivery.delivery.controller;

import com.example.shinhandelivery.common.annotation.CurrentUserId;
import com.example.shinhandelivery.common.security.WebSecurityUtils;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** 배송(Delivery) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class DeliveryWebController {

  private final DeliveryService deliveryService;

  @GetMapping("/delivery-history")
  public String deliveryHistory(@CurrentUserId Long userId, Model model) {
    if (userId != null) {
      WebSecurityUtils.safeAddAttribute(
          model,
          "deliveries",
          () ->
              deliveryService
                  .getMyDeliveryRequests(userId, null, PageRequest.of(0, 20))
                  .getContent());
    }
    return "delivery-history";
  }

  @GetMapping("/delivery-cancel-list")
  public String deliveryCancelList(@CurrentUserId Long userId, Model model) {
    if (userId != null) {
      WebSecurityUtils.safeAddAttribute(
          model,
          "deliveries",
          () ->
              deliveryService
                  .getMyDeliveryRequests(userId, DeliveryStatus.CANCELLED, PageRequest.of(0, 20))
                  .getContent());
    }
    return "delivery-cancel-list";
  }

  @GetMapping("/delivery-detail")
  public String deliveryDetail(@RequestParam(required = false) Long id, Model model) {
    if (id != null) {
      WebSecurityUtils.safeAddAttribute(
          model, "delivery", () -> deliveryService.getDeliveryRequest(id));
    }
    return "delivery-detail";
  }

  @GetMapping("/item-detail")
  public String itemDetail() {
    return "item-detail";
  }

  @GetMapping("/door-photo")
  public String doorPhoto() {
    return "door-photo";
  }

  @GetMapping("/cancel-detail")
  public String cancelDetail(@RequestParam(required = false) Long id, Model model) {
    if (id != null) {
      WebSecurityUtils.safeAddAttribute(
          model, "delivery", () -> deliveryService.getDeliveryRequest(id));
    }
    return "cancel-detail";
  }

  @GetMapping("/matching-wait")
  public String matchingWait(@RequestParam(required = false) Long id, Model model) {
    if (id != null) {
      WebSecurityUtils.safeAddAttribute(
          model, "delivery", () -> deliveryService.getDeliveryRequest(id));
    }
    return "matching-wait";
  }

  @GetMapping("/matching-complete")
  public String matchingComplete(@RequestParam(required = false) Long id, Model model) {
    if (id != null) {
      WebSecurityUtils.safeAddAttribute(
          model, "delivery", () -> deliveryService.getDeliveryRequest(id));
    }
    return "matching-complete";
  }
}
