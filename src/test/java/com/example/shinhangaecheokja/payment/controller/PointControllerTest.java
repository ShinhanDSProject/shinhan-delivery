package com.example.shinhangaecheokja.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.exception.GlobalExceptionHandler;
import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointBalanceResponse;
import com.example.shinhangaecheokja.payment.entity.PaymentMethod;
import com.example.shinhangaecheokja.payment.service.PaymentService;
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
class PointControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private PaymentService paymentService;
  @InjectMocks private PointController pointController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(pointController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("인증된 회원이 포인트를 충전하면 잔액과 마지막 충전 시각을 반환한다")
  void chargePointSuccess() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(5000L);
    request.setPaymentMethod(PaymentMethod.CARD);

    when(paymentService.chargePoint(eq(10L), eq("idem-123"), any(PointChargeRequest.class)))
        .thenReturn(new PointBalanceResponse(15000L, java.time.LocalDateTime.of(2026, 8, 4, 1, 30)));

    mockMvc
        .perform(
            post("/api/v1/points/charge")
                .principal(auth)
                .header("Idempotency-Key", "idem-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(15000L))
        .andExpect(jsonPath("$.lastChargedAt").value("2026-08-04T01:30:00"));
  }

  @Test
  @DisplayName("인증되지 않은 사용자가 포인트 충전을 요청하면 401을 반환한다")
  void chargePointUnauthenticatedShouldReturn401() throws Exception {
    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(5000L);
    request.setPaymentMethod(PaymentMethod.CARD);

    mockMvc
        .perform(
            post("/api/v1/points/charge")
                .header("Idempotency-Key", "idem-unauth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }
}
