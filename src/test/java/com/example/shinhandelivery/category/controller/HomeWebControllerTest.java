package com.example.shinhandelivery.category.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.category.entity.Category;
import com.example.shinhandelivery.category.service.CategoryService;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.RedirectView;

@ExtendWith(MockitoExtension.class)
class HomeWebControllerTest {

  private MockMvc mockMvc;

  @Mock private CategoryService categoryService;
  @Mock private MemberService memberService;
  @Mock private DeliveryService deliveryService;
  @Mock private NotificationService notificationService;
  @InjectMocks private HomeWebController homeWebController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(homeWebController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setViewResolvers(
                (viewName, locale) ->
                    viewName.startsWith("redirect:")
                        ? new RedirectView(viewName.substring("redirect:".length()))
                        : (model, request, response) -> {})
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("로그인하지 않은 상태로 홈에 접근하면 로그인 페이지로 리다이렉트한다")
  void homeWithoutPrincipalRedirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/home"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @DisplayName("결제 PIN 미설정 회원은 PIN 설정 화면으로 리다이렉트한다")
  void homeWithoutPaymentPinRedirectsToPinSettings() throws Exception {
    Authentication auth = authenticationFor(1L);
    when(memberService.getMyProfile(1L)).thenReturn(memberWith(false));

    mockMvc
        .perform(get("/home").principal(auth))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/payment-pin-settings?required=1&returnUrl=/home"));
  }

  @Test
  @DisplayName("로그인 + PIN 설정된 회원은 home 뷰와 회원/배송/알림 모델 데이터를 반환한다")
  void homeReturnsViewAndModelForAuthenticatedMember() throws Exception {
    Authentication auth = authenticationFor(1L);
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

    mockMvc
        .perform(get("/home").principal(auth))
        .andExpect(status().isOk())
        .andExpect(view().name("home"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attribute("memberName", "홍길동"))
        .andExpect(model().attribute("waitingCount", 0L))
        .andExpect(model().attribute("hasUnreadNotification", true));
  }

  private Authentication authenticationFor(Long memberId) {
    CustomUserDetails principal =
        new CustomUserDetails(memberId, "user@example.com", "encoded-pw", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    return auth;
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
