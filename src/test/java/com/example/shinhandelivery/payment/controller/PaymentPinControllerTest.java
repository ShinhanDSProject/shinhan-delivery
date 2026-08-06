package com.example.shinhandelivery.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.exception.GlobalExceptionHandler;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.payment.dto.request.PinVerifyRequestDto;
import com.example.shinhandelivery.payment.dto.response.PinVerifyResponseDto;
import com.example.shinhandelivery.payment.service.PaymentService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentPinControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private PaymentService paymentService;
  @InjectMocks private PaymentPinController paymentPinController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(paymentPinController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("올바른 PIN이면 verified=true를 반환한다")
  void verifyPinSuccess() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("123456");

    when(paymentService.verifyPin(eq(10L), any(PinVerifyRequestDto.class)))
        .thenReturn(new PinVerifyResponseDto(true));

    mockMvc
        .perform(
            post("/api/payments/verify-pin")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.verified").value(true));
  }

  @Test
  @DisplayName("인증되지 않은 사용자는 401을 반환한다")
  void verifyPinUnauthenticatedShouldReturn401() throws Exception {
    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("123456");

    mockMvc
        .perform(
            post("/api/payments/verify-pin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("잠긴 PIN이면 PIN_LOCKED를 반환한다")
  void verifyPinLockedShouldReturnConflict() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("123456");

    when(paymentService.verifyPin(eq(10L), any(PinVerifyRequestDto.class)))
        .thenThrow(new BusinessException(ErrorCode.PIN_LOCKED));

    mockMvc
        .perform(
            post("/api/payments/verify-pin")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("P005"));
  }
}
