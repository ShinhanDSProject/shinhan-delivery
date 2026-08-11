package com.example.shinhandelivery.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.category.entity.Category;
import com.example.shinhandelivery.category.service.CategoryService;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.home.dto.response.HomePageResponse;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

  @Mock private CategoryService categoryService;
  @Mock private MemberService memberService;
  @Mock private DeliveryService deliveryService;
  @Mock private NotificationService notificationService;
  @InjectMocks private HomeService homeService;

  @Test
  @DisplayName("결제 PIN이 없으면 나머지 데이터를 조회하지 않고 즉시 반환한다")
  void loadReturnsPaymentPinRequiredWithoutFurtherLookups() {
    when(memberService.getMyProfile(1L)).thenReturn(memberWith(false));

    HomePageResponse result = homeService.load(1L);

    assertThat(result.paymentPinRequired()).isTrue();
    verify(deliveryService, never()).getMyDeliveryRequests(any(), any(), any());
    verify(notificationService, never()).list(any(), any(), any());
  }

  @Test
  @DisplayName("결제 PIN이 있으면 회원/배송/알림 데이터를 조합해 반환한다")
  void loadAggregatesHomePageData() {
    when(memberService.getMyProfile(1L)).thenReturn(memberWith(true));

    Category category = new Category();
    category.setId(1L);
    category.setName("식품/음료");
    when(categoryService.list()).thenReturn(List.of(category));

    DeliveryRequest matched =
        DeliveryRequest.builder()
            .id(10L)
            .memberId(1L)
            .pickupAddress("서울 강남구 테헤란로")
            .dropoffAddress("서울 서초구 반포대로")
            .status(DeliveryStatus.MATCHED)
            .build();
    when(deliveryService.getMyDeliveryRequests(eq(1L), eq(DeliveryStatus.MATCHED), any()))
        .thenReturn(new PageImpl<>(List.of(matched)));
    when(deliveryService.getMyDeliveryRequests(eq(1L), eq(DeliveryStatus.REQUESTED), any()))
        .thenReturn(new PageImpl<>(List.of()));

    Notification unread = Notification.of(1L, "제목", "내용", "DELIVERY", false);
    Page<Notification> notificationPage = new PageImpl<>(List.of(unread));
    when(notificationService.list(eq(1L), isNull(), any())).thenReturn(notificationPage);

    HomePageResponse result = homeService.load(1L);

    assertThat(result.paymentPinRequired()).isFalse();
    assertThat(result.memberName()).isEqualTo("홍길동");
    assertThat(result.categories()).extracting("name").containsExactly("식품/음료");
    assertThat(result.waitingCount()).isZero();
    assertThat(result.hasUnreadNotification()).isTrue();
    assertThat(result.activeDeliveries()).hasSize(1);
    assertThat(result.activeDeliveries().get(0).id()).isEqualTo(10L);
    assertThat(result.activeDeliveries().get(0).routeLabel()).isEqualTo("강남구 → 서초구");
  }

  private Member memberWith(boolean hasPin) {
    return Member.builder()
        .id(1L)
        .email("user@example.com")
        .password("encoded-pw")
        .name("홍길동")
        .phoneNumber("010-1234-5678")
        .role(MemberRole.CUSTOMER)
        .pinHash(hasPin ? "encoded-pin" : null)
        .build();
  }
}
