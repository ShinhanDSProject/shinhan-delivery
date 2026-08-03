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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 로그인 회원의 포인트 충전 및 잔액 조회 API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

  private final PaymentService paymentService;

  /** 멱등성 키를 사용해 로그인 회원의 포인트를 충전한다. */
  @PostMapping("/charge")
  public ResponseEntity<PointBalanceResponse> charge(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @Valid @RequestBody PointChargeRequest request) {
    CustomUserDetails resolved = resolveUserDetails(userDetails);
    return ResponseEntity.ok(paymentService.charge(resolved.getId(), idempotencyKey, request));
  }

  /** 로그인 회원의 포인트 잔액과 마지막 충전 시각을 조회한다. */
  @GetMapping("/balance")
  public ResponseEntity<PointBalanceResponse> getBalance(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    CustomUserDetails resolved = resolveUserDetails(userDetails);
    return ResponseEntity.ok(paymentService.getBalance(resolved.getId()));
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
