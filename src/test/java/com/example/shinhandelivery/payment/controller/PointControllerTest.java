package com.example.shinhandelivery.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.exception.GlobalExceptionHandler;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.payment.dto.request.PointChargeRequest;
import com.example.shinhandelivery.payment.dto.response.PointBalanceResponse;
import com.example.shinhandelivery.payment.dto.response.PointHistoryItemResponse;
import com.example.shinhandelivery.payment.entity.PaymentMethod;
import com.example.shinhandelivery.payment.entity.PointHistoryType;
import com.example.shinhandelivery.payment.service.PaymentService;
import java.time.LocalDateTime;
import java.util.List;
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
  @DisplayName("인증 회원은 최근 포인트 이력을 조회할 수 있다")
  void getRecentPointHistoriesSuccess() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(paymentService.getRecentPointHistories(10L))
        .thenReturn(
            List.of(
                PointHistoryItemResponse.builder()
                    .historyId(1L)
                    .type(PointHistoryType.CHARGE)
                    .amount(5000L)
                    .balanceAfter(15000L)
                    .paymentMethod(PaymentMethod.CARD)
                    .createdAt(LocalDateTime.of(2026, 8, 18, 9, 30))
                    .build()));

    mockMvc
        .perform(get("/api/v1/points/histories").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].historyId").value(1L))
        .andExpect(jsonPath("$[0].type").value("CHARGE"))
        .andExpect(jsonPath("$[0].amount").value(5000L))
        .andExpect(jsonPath("$[0].paymentMethod").value("CARD"))
        .andExpect(jsonPath("$[0].createdAt").value("2026-08-18T09:30:00"));
  }

  @Test
  @DisplayName("인증 회원이 포인트를 충전하면 잔액과 마지막 충전 시각을 반환한다")
  void chargePointSuccess() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(5000L);
    request.setPaymentMethod(PaymentMethod.CARD);

    when(paymentService.chargePoint(eq(10L), eq("idem-123"), any(PointChargeRequest.class)))
        .thenReturn(new PointBalanceResponse(15000L, LocalDateTime.of(2026, 8, 4, 1, 30)));

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
  @DisplayName("비인증 사용자가 포인트 충전을 요청하면 401을 반환한다")
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
