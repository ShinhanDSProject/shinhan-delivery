package com.example.shinhandelivery.common.controller;

import com.example.shinhandelivery.category.dto.response.CategoryResponse;
import com.example.shinhandelivery.category.service.CategoryService;
import com.example.shinhandelivery.common.security.WebAuthHelper;
import com.example.shinhandelivery.delivery.dto.response.DeliveryResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.notification.service.NotificationService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 홈 화면의 서버 사이드 렌더링(SSR)을 제공하는 뷰 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class HomeWebController {

  private final CategoryService categoryService;
  private final DeliveryService deliveryService;
  private final NotificationService notificationService;
  private final WebAuthHelper webAuthHelper;

  @GetMapping("/home")
  public String home(Model model) {
    List<CategoryResponse> categories =
        categoryService.list().stream().map(CategoryResponse::from).toList();
    model.addAttribute("categories", categories);

    webAuthHelper
        .getCurrentMemberId()
        .ifPresent(
            memberId -> {
              List<DeliveryResponse> activeDeliveries = new ArrayList<>();
              deliveryService
                  .getMyDeliveryRequests(memberId, DeliveryStatus.REQUESTED, PageRequest.of(0, 50))
                  .getContent()
                  .forEach(req -> activeDeliveries.add(DeliveryResponse.from(req)));
              deliveryService
                  .getMyDeliveryRequests(memberId, DeliveryStatus.MATCHED, PageRequest.of(0, 50))
                  .getContent()
                  .forEach(req -> activeDeliveries.add(DeliveryResponse.from(req)));
              model.addAttribute("activeDeliveries", activeDeliveries);

              boolean hasUnread =
                  notificationService
                      .list(memberId, null, PageRequest.of(0, 20))
                      .getContent()
                      .stream()
                      .anyMatch(n -> !n.isRead());
              model.addAttribute("hasUnreadNotifications", hasUnread);
            });

    return "home";
  }
}
