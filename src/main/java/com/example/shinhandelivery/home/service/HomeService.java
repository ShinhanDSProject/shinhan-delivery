package com.example.shinhandelivery.home.service;

import com.example.shinhandelivery.category.dto.response.CategoryResponse;
import com.example.shinhandelivery.category.service.CategoryService;
import com.example.shinhandelivery.delivery.dto.response.DeliveryListResponseDto;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.home.dto.response.HomeDeliveryCardResponse;
import com.example.shinhandelivery.home.dto.response.HomePageResponse;
import com.example.shinhandelivery.member.dto.response.MemberProfileResponseDto;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 홈 화면 SSR 렌더링에 필요한 회원·배송·알림·카테고리 데이터를 조합하는 서비스. */
@Service
@RequiredArgsConstructor
public class HomeService {

  private final CategoryService categoryService;
  private final MemberService memberService;
  private final DeliveryService deliveryService;
  private final NotificationService notificationService;

  /** 로그인 회원의 홈 화면 데이터를 조합한다. 결제 PIN이 없으면 나머지 조회 없이 즉시 반환한다. */
  @Transactional(readOnly = true)
  public HomePageResponse load(Long memberId) {
    MemberProfileResponseDto profile =
        MemberProfileResponseDto.from(memberService.getMyProfile(memberId));
    if (!profile.hasPaymentPin()) {
      return HomePageResponse.forPaymentPinRequired();
    }

    List<CategoryResponse> categories =
        categoryService.list().stream().map(CategoryResponse::from).toList();

    List<HomeDeliveryCardResponse> activeDeliveries =
        deliveryService
            .getMyDeliveryRequests(memberId, DeliveryStatus.MATCHED, PageRequest.of(0, 100))
            .map(DeliveryListResponseDto::from)
            .map(this::toHomeDeliveryCard)
            .toList();
    long waitingCount =
        deliveryService
            .getMyDeliveryRequests(memberId, DeliveryStatus.REQUESTED, PageRequest.of(0, 100))
            .getTotalElements();

    boolean hasUnreadNotification =
        notificationService.list(memberId, null, PageRequest.of(0, 20)).stream()
            .anyMatch(notification -> !notification.isRead());

    return new HomePageResponse(
        false, profile.name(), categories, activeDeliveries, waitingCount, hasUnreadNotification);
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
