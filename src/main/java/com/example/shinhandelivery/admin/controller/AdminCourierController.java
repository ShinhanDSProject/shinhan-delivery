package com.example.shinhandelivery.admin.controller;

import com.example.shinhandelivery.admin.dto.response.PendingCourierResponseDto;
import com.example.shinhandelivery.admin.service.AdminCourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 전용 배송원 자격 검증 및 승인 관리 컨트롤러. */
@RestController
@RequestMapping("/api/v1/admin/couriers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourierController {

  private final AdminCourierService adminCourierService;

  /** 승인 대기 중인 배송원 목록을 조회한다. */
  @GetMapping("/pending")
  public ResponseEntity<Page<PendingCourierResponseDto>> getPendingCouriers(
      @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(adminCourierService.getPendingCouriers(pageable));
  }

  /** 대기 배송원의 자격 심사를 최종 승인한다. */
  @PatchMapping("/{courierId}/approve")
  public ResponseEntity<PendingCourierResponseDto> approveCourier(@PathVariable Long courierId) {
    return ResponseEntity.ok(adminCourierService.approveCourier(courierId));
  }

  /** 대기 배송원의 자격 심사를 거절한다. */
  @PatchMapping("/{courierId}/reject")
  public ResponseEntity<PendingCourierResponseDto> rejectCourier(@PathVariable Long courierId) {
    return ResponseEntity.ok(adminCourierService.rejectCourier(courierId));
  }
}
