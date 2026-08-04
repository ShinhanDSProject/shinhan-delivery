package com.example.shinhangaecheokja.delivery.controller;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryPayRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryPaymentResponse;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FE 호환용 배송 결제 alias 컨트롤러. */
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryPaymentAliasController {

  private final DeliveryService deliveryService;

  @PostMapping("/pay")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DeliveryPaymentResponse> payDelivery(
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @RequestBody @Valid DeliveryPayRequest request) {
    return ResponseEntity.ok(
        deliveryService.payDelivery(resolveUserDetails().getId(), idempotencyKey, request));
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
