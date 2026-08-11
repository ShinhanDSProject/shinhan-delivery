package com.example.shinhandelivery.home.dto.response;

import com.example.shinhandelivery.category.dto.response.CategoryResponse;
import java.util.List;

/** 홈 화면 SSR 렌더링에 필요한 데이터를 담는 응답 DTO. */
public record HomePageResponse(
    boolean paymentPinRequired,
    String memberName,
    List<CategoryResponse> categories,
    List<HomeDeliveryCardResponse> activeDeliveries,
    long waitingCount,
    boolean hasUnreadNotification) {

  /** 결제 PIN이 설정되지 않아 홈 데이터를 조합하지 않고 리다이렉트만 필요한 경우 반환한다. */
  public static HomePageResponse forPaymentPinRequired() {
    return new HomePageResponse(true, null, List.of(), List.of(), 0, false);
  }
}
