package com.example.shinhandelivery.payment.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.service.PaymentService;
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
class PaymentWebControllerTest {

  private MockMvc mockMvc;

  @Mock private PaymentService paymentService;
  @Mock private MemberService memberService;
  @InjectMocks private PaymentWebController paymentWebController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(paymentWebController)
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
  @DisplayName("로그인하지 않은 상태로 포인트 지갑에 접근하면 로그인 페이지로 리다이렉트한다")
  void pointWalletWithoutPrincipalRedirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/point-wallet"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @DisplayName("로그인 상태로 포인트 지갑 요청 시 point-wallet 뷰와 잔액 모델 데이터를 반환한다")
  void pointWalletReturnsViewAndBalance() throws Exception {
    Authentication auth = authenticationFor(1L);
    when(paymentService.getOrCreateWallet(1L)).thenReturn(walletWithBalance(2500L));

    mockMvc
        .perform(get("/point-wallet").principal(auth))
        .andExpect(status().isOk())
        .andExpect(view().name("point-wallet"))
        .andExpect(model().attribute("balance", 2500L));
  }

  @Test
  @DisplayName("로그인하지 않은 상태로 포인트 충전에 접근하면 로그인 페이지로 리다이렉트한다")
  void pointChargeWithoutPrincipalRedirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/point-charge"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @DisplayName("로그인 상태로 포인트 충전 요청 시 point-charge 뷰와 잔액 모델 데이터를 반환한다")
  void pointChargeReturnsViewAndBalance() throws Exception {
    Authentication auth = authenticationFor(1L);
    when(paymentService.getOrCreateWallet(1L)).thenReturn(walletWithBalance(1000L));

    mockMvc
        .perform(get("/point-charge").principal(auth))
        .andExpect(status().isOk())
        .andExpect(view().name("point-charge"))
        .andExpect(model().attribute("balance", 1000L));
  }

  @Test
  @DisplayName("로그인하지 않은 상태로 결제 PIN 설정에 접근하면 로그인 페이지로 리다이렉트한다")
  void paymentPinSettingsWithoutPrincipalRedirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/payment-pin-settings"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @DisplayName("로그인 상태로 결제 PIN 설정 요청 시 hasPaymentPin 모델 데이터를 반환한다")
  void paymentPinSettingsReturnsViewAndHasPaymentPin() throws Exception {
    Authentication auth = authenticationFor(1L);
    when(memberService.getMyProfile(1L)).thenReturn(memberWith(true));

    mockMvc
        .perform(get("/payment-pin-settings").principal(auth))
        .andExpect(status().isOk())
        .andExpect(view().name("payment-pin-settings"))
        .andExpect(model().attribute("hasPaymentPin", true));
  }

  private Authentication authenticationFor(Long memberId) {
    CustomUserDetails principal =
        new CustomUserDetails(memberId, "user@example.com", "encoded-pw", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    return auth;
  }

  private PointWallet walletWithBalance(long balance) {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(balance);
    return wallet;
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
