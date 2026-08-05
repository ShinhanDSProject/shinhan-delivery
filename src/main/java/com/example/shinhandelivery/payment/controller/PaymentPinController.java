package com.example.shinhandelivery.payment.controller;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.payment.dto.request.PinVerifyRequestDto;
import com.example.shinhandelivery.payment.dto.response.PinVerifyResponseDto;
import com.example.shinhandelivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 사용자 기준 결제 PIN 검증 API. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentPinController {

  private final PaymentService paymentService;

  @PostMapping("/verify-pin")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PinVerifyResponseDto> verifyPin(
      @Valid @RequestBody PinVerifyRequestDto request) {
    CustomUserDetails userDetails = resolveUserDetails();
    return ResponseEntity.ok(paymentService.verifyPin(userDetails.getId(), request));
  }

  private CustomUserDetails resolveUserDetails() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null
        && auth.getPrincipal() instanceof CustomUserDetails customUser
        && customUser.getId() != null) {
      return customUser;
    }
    throw new BusinessException(ErrorCode.UNAUTHORIZED);
  }
}
