package com.example.shinhangaecheokja.payment.controller;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointBalanceResponse;
import com.example.shinhangaecheokja.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증된 회원 기준 포인트 충전 API. */
@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

  private final PaymentService paymentService;

  @PostMapping("/charge")
  public ResponseEntity<PointBalanceResponse> chargePoint(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody PointChargeRequest request) {
    CustomUserDetails resolved = resolveUserDetails(userDetails);
    return ResponseEntity.ok(paymentService.chargePoint(resolved.getId(), idempotencyKey, request));
  }

  private CustomUserDetails resolveUserDetails(CustomUserDetails userDetails) {
    if (userDetails != null && userDetails.getId() != null) {
      return userDetails;
    }
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null
        && auth.getPrincipal() instanceof CustomUserDetails customUser
        && customUser.getId() != null) {
      return customUser;
    }
    throw new BusinessException(ErrorCode.UNAUTHORIZED);
  }
}
