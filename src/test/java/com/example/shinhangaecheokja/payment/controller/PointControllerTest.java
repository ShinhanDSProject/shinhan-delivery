package com.example.shinhangaecheokja.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.common.security.SecurityConfig;
import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointBalanceResponse;
import com.example.shinhangaecheokja.payment.entity.PaymentMethod;
import com.example.shinhangaecheokja.payment.service.PaymentService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PointController.class)
@Import(SecurityConfig.class)
class PointControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PaymentService paymentService;
  @MockitoBean private JwtProvider jwtProvider;

  private CustomUserDetails userDetails;

  @BeforeEach
  void setUp() {
    userDetails = new CustomUserDetails(7L, "point@example.com", "", "CUSTOMER");
  }

  @Test
  void 로그인_회원이_포인트를_충전하면_갱신된_잔액을_반환한다() throws Exception {
    String idempotencyKey = UUID.randomUUID().toString();
    LocalDateTime chargedAt = LocalDateTime.of(2026, 7, 28, 13, 0);
    PointChargeRequest request = chargeRequest(10_000L, PaymentMethod.CARD);
    when(paymentService.charge(eq(7L), eq(idempotencyKey), any(PointChargeRequest.class)))
        .thenReturn(new PointBalanceResponse(10_000L, chargedAt));

    mockMvc
        .perform(
            post("/api/points/charge")
                .with(user(userDetails))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(10_000L))
        .andExpect(jsonPath("$.lastChargedAt").value("2026-07-28T13:00:00"));
  }

  @Test
  void 로그인_회원이_잔액을_조회하면_마지막_충전시각을_함께_반환한다() throws Exception {
    LocalDateTime chargedAt = LocalDateTime.of(2026, 7, 28, 13, 30);
    when(paymentService.getBalance(7L)).thenReturn(new PointBalanceResponse(25_000L, chargedAt));

    mockMvc
        .perform(get("/api/points/balance").with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(25_000L))
        .andExpect(jsonPath("$.lastChargedAt").value("2026-07-28T13:30:00"));
  }

  @Test
  void 인증하지_않으면_포인트_API는_401을_반환한다() throws Exception {
    mockMvc.perform(get("/api/points/balance")).andExpect(status().isUnauthorized());
  }

  @Test
  void 멱등성키_헤더가_없으면_400을_반환한다() throws Exception {
    PointChargeRequest request = chargeRequest(10_000L, PaymentMethod.CARD);
    when(paymentService.charge(eq(7L), isNull(), any(PointChargeRequest.class)))
        .thenThrow(
            new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Idempotency-Key 헤더는 필수입니다."));

    mockMvc
        .perform(
            post("/api/points/charge")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("C001"));
  }

  @Test
  void 충전금액이_0이면_400을_반환한다() throws Exception {
    PointChargeRequest request = chargeRequest(0L, PaymentMethod.CARD);

    mockMvc
        .perform(
            post("/api/points/charge")
                .with(user(userDetails))
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("C001"));
  }

  @Test
  void 지원하지_않는_결제수단이면_400을_반환한다() throws Exception {
    String requestBody = "{\"amount\":10000,\"paymentMethod\":\"CRYPTO\"}";

    mockMvc
        .perform(
            post("/api/points/charge")
                .with(user(userDetails))
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("C001"));
  }

  private PointChargeRequest chargeRequest(long amount, PaymentMethod paymentMethod) {
    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(amount);
    request.setPaymentMethod(paymentMethod);
    return request;
  }
}
