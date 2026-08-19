package com.example.shinhandelivery.delivery.controller;

import com.example.shinhandelivery.common.security.WebAuthHelper;
import com.example.shinhandelivery.delivery.dto.response.DeliveryListResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 배송(Delivery) 도메인 화면의 SSR 라우팅을 담당하는 Web Controller입니다. */
@Controller
@RequiredArgsConstructor
public class DeliveryWebController {

  private final DeliveryService deliveryService;
  private final WebAuthHelper webAuthHelper;

  @GetMapping("/delivery-history")
  public String deliveryHistory(Model model) {
    webAuthHelper
        .getCurrentMemberId()
        .ifPresent(
            memberId -> {
              List<DeliveryListResponse> deliveries =
                  deliveryService
                      .getMyDeliveryRequests(memberId, null, PageRequest.of(0, 10))
                      .getContent()
                      .stream()
                      .map(DeliveryListResponse::from)
                      .toList();
              model.addAttribute("deliveries", deliveries);
            });
    return "delivery-history";
  }

  @GetMapping("/delivery-cancel-list")
  public String deliveryCancelList(Model model) {
    webAuthHelper
        .getCurrentMemberId()
        .ifPresent(
            memberId -> {
              List<DeliveryListResponse> cancelledDeliveries =
                  deliveryService
                      .getMyDeliveryRequests(
                          memberId, DeliveryStatus.CANCELLED, PageRequest.of(0, 10))
                      .getContent()
                      .stream()
                      .map(DeliveryListResponse::from)
                      .toList();
              model.addAttribute("cancelledDeliveries", cancelledDeliveries);
            });
    return "delivery-cancel-list";
  }

  @GetMapping("/delivery-detail")
  public String deliveryDetail() {
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

  @GetMapping("/pickup-photo")
  public String pickupPhoto() {
    return "pickup-photo";
  }

  @GetMapping("/cancel-detail")
  public String cancelDetail() {
    return "cancel-detail";
  }

  @GetMapping("/matching-wait")
  public String matchingWait() {
    return "matching-wait";
  }

  @GetMapping("/matching-complete")
  public String matchingComplete() {
    return "matching-complete";
  }
}
