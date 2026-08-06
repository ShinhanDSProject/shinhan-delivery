package com.example.shinhandelivery.courier.controller;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.courier.dto.request.CourierStatusUpdateRequest;
import com.example.shinhandelivery.courier.dto.response.CourierStatusResponse;
import com.example.shinhandelivery.courier.service.CourierStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 배송원(Courier/Rider)의 영업 상태 토글 및 상태 관리를 전담하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/couriers")
@RequiredArgsConstructor
public class CourierController {

  private final CourierStatusService courierStatusService;

  /** 배송원 영업 상태(ONLINE / OFFLINE) 및 GPS 위치를 변경한다. */
  @PatchMapping("/status")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<CourierStatusResponse> updateWorkStatus(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid CourierStatusUpdateRequest request) {
    Long memberId = (userDetails != null) ? userDetails.getId() : resolveUserDetails().getId();
    CourierStatusResponse response = courierStatusService.updateWorkStatus(memberId, request);
    return ResponseEntity.ok(response);
  }

  /** 배송원 본인의 현재 영업 상태를 조회한다. */
  @GetMapping("/status")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<CourierStatusResponse> getWorkStatus(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = (userDetails != null) ? userDetails.getId() : resolveUserDetails().getId();
    CourierStatusResponse response = courierStatusService.getWorkStatus(memberId);
    return ResponseEntity.ok(response);
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
