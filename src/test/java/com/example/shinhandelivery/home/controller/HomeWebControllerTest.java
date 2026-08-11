package com.example.shinhandelivery.home.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.category.dto.response.CategoryResponse;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.home.dto.response.HomeDeliveryCardResponse;
import com.example.shinhandelivery.home.dto.response.HomePageResponse;
import com.example.shinhandelivery.home.service.HomeService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

  @Mock private HomeService homeService;
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
    when(homeService.load(1L)).thenReturn(HomePageResponse.forPaymentPinRequired());

    mockMvc
        .perform(get("/home").principal(auth))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/payment-pin-settings?required=1&returnUrl=/home"));
  }

  @Test
  @DisplayName("로그인 + PIN 설정된 회원은 home 뷰와 HomeService가 조합한 모델 데이터를 반환한다")
  void homeReturnsViewAndModelForAuthenticatedMember() throws Exception {
    Authentication auth = authenticationFor(1L);
    List<HomeDeliveryCardResponse> activeDeliveries =
        List.of(new HomeDeliveryCardResponse(10L, "강남구 → 서초구", "예상 도착: 오후 3:45"));
    HomePageResponse homePage =
        new HomePageResponse(
            false, "홍길동", List.of(new CategoryResponse(1L, "식품/음료")), activeDeliveries, 0L, true);
    when(homeService.load(1L)).thenReturn(homePage);

    mockMvc
        .perform(get("/home").principal(auth))
        .andExpect(status().isOk())
        .andExpect(view().name("home"))
        .andExpect(model().attributeExists("categories"))
        .andExpect(model().attribute("memberName", "홍길동"))
        .andExpect(model().attribute("waitingCount", 0L))
        .andExpect(model().attribute("hasUnreadNotification", true))
        .andExpect(model().attribute("activeDeliveries", activeDeliveries));
  }

  private Authentication authenticationFor(Long memberId) {
    CustomUserDetails principal =
        new CustomUserDetails(memberId, "user@example.com", "encoded-pw", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    return auth;
  }
}
