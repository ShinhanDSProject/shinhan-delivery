package com.example.shinhandelivery.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.exception.GlobalExceptionHandler;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.member.dto.request.MemberPaymentPinUpdateRequest;
import com.example.shinhandelivery.member.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MemberPaymentPinControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private MemberService memberService;
  @InjectMocks private MemberController memberController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(memberController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("인증된 사용자는 결제 PIN을 설정할 수 있다")
  void updatePaymentPinSuccess() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    MemberPaymentPinUpdateRequest request =
        new MemberPaymentPinUpdateRequest("", "123456", "123456");

    mockMvc
        .perform(
            patch("/api/v1/members/payment-pin")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(memberService).updatePaymentPin(eq(10L), any(MemberPaymentPinUpdateRequest.class));
  }

  @Test
  @DisplayName("6자리가 아닌 PIN은 400을 반환한다")
  void updatePaymentPinInvalidShouldReturn400() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    MemberPaymentPinUpdateRequest request = new MemberPaymentPinUpdateRequest("", "12345", "12345");

    mockMvc
        .perform(
            patch("/api/v1/members/payment-pin")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
