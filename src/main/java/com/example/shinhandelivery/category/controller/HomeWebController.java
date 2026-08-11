package com.example.shinhandelivery.category.controller;

import com.example.shinhandelivery.category.dto.response.CategoryResponse;
import com.example.shinhandelivery.category.dto.response.HomeDeliveryCardResponse;
import com.example.shinhandelivery.category.service.CategoryService;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.delivery.dto.response.DeliveryListResponseDto;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.member.dto.response.MemberProfileResponseDto;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 홈 화면의 서버 사이드 렌더링(SSR)을 제공하는 뷰 컨트롤러입니다. */
@Controller
@RequiredArgsConstructor
public class HomeWebController {

  private final CategoryService categoryService;
  private final MemberService memberService;
  private final DeliveryService deliveryService;
  private final NotificationService notificationService;

  @GetMapping("/home")
  public String home(Model model, @AuthenticationPrincipal CustomUserDetails principal) {
    if (principal == null) {
      return "redirect:/login";
    }

    MemberProfileResponseDto profile =
        MemberProfileResponseDto.from(memberService.getMyProfile(principal.getId()));
    if (!profile.hasPaymentPin()) {
      return "redirect:/payment-pin-settings?required=1&returnUrl=/home";
    }

    List<CategoryResponse> categories =
        categoryService.list().stream().map(CategoryResponse::from).toList();

    List<HomeDeliveryCardResponse> activeDeliveries =
        deliveryService
            .getMyDeliveryRequests(
                principal.getId(), DeliveryStatus.MATCHED, PageRequest.of(0, 100))
            .map(DeliveryListResponseDto::from)
            .map(this::toHomeDeliveryCard)
            .toList();
    long waitingCount =
        deliveryService
            .getMyDeliveryRequests(
                principal.getId(), DeliveryStatus.REQUESTED, PageRequest.of(0, 100))
            .getTotalElements();

    boolean hasUnreadNotification =
        notificationService.list(principal.getId(), null, PageRequest.of(0, 20)).stream()
            .anyMatch(notification -> !notification.isRead());

    model.addAttribute("categories", categories);
    model.addAttribute("memberName", profile.name());
    model.addAttribute("activeDeliveries", activeDeliveries);
    model.addAttribute("waitingCount", waitingCount);
    model.addAttribute("hasUnreadNotification", hasUnreadNotification);
    return "home";
  }

  private HomeDeliveryCardResponse toHomeDeliveryCard(DeliveryListResponseDto delivery) {
    return new HomeDeliveryCardResponse(
        delivery.id(),
        formatRoute(delivery.pickupAddress(), delivery.dropoffAddress()),
        placeholderEtaLabel());
  }

  private String formatRoute(String pickupAddress, String dropoffAddress) {
    return shortLocation(pickupAddress) + " → " + shortLocation(dropoffAddress);
  }

  private String shortLocation(String address) {
    String[] tokens = address.split(" ");
    for (String token : tokens) {
      if (token.endsWith("구") || token.endsWith("군")) {
        return token;
      }
    }
    return tokens.length > 1 ? tokens[1] : address;
  }

  /** 백엔드에 실제 예상도착 시간 필드가 추가되면 이 임시 추정치를 대체한다. */
  private String placeholderEtaLabel() {
    LocalDateTime eta = LocalDateTime.now().plusMinutes(90);
    int hour24 = eta.getHour();
    String period = hour24 < 12 ? "오전" : "오후";
    int hour12 = ((hour24 + 11) % 12) + 1;
    return String.format("예상 도착: %s %d:%02d", period, hour12, eta.getMinute());
  }
}
