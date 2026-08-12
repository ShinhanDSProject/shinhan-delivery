package com.example.shinhandelivery.vehicle.controller;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhandelivery.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhandelivery.vehicle.dto.response.VehicleResponse;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Vehicle CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;

  /** 운송수단을 등록한다. */
  @PostMapping
  public ResponseEntity<VehicleResponse> registerVehicle(
      @RequestBody @Valid VehicleCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails != null) {
      request.setMemberId(userDetails.getId());
    }
    if (request.getMemberId() == null || request.getMemberId() <= 0) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 인증 정보가 유효하지 않습니다.");
    }
    Vehicle created = vehicleService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(VehicleResponse.from(created));
  }

  /** 내 소유의 운송수단 목록을 조회한다. */
  @GetMapping("/my")
  public ResponseEntity<List<VehicleResponse>> getMyVehicles(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "로그인이 필요합니다.");
    }
    Long memberId = userDetails.getId();
    List<VehicleResponse> responses =
        vehicleService.getVehiclesByMemberId(memberId).stream().map(VehicleResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  /** 운송수단 단건을 조회한다. */
  @GetMapping("/{vehicleId}")
  public ResponseEntity<VehicleResponse> getVehicle(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(VehicleResponse.from(vehicleService.getById(vehicleId)));
  }

  /** 운송수단 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<VehicleResponse>> getVehicles() {
    List<VehicleResponse> responses =
        vehicleService.list().stream().map(VehicleResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  /** 운송수단을 단일 활성화(Exclusive Toggle)한다. */
  @PatchMapping("/{vehicleId}/activate")
  public ResponseEntity<VehicleResponse> activateVehicle(
      @PathVariable Long vehicleId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails.getId();
    Vehicle activated = vehicleService.activateVehicle(memberId, vehicleId);
    return ResponseEntity.ok(VehicleResponse.from(activated));
  }

  /** 관리자가 운송수단을 승인한다. */
  @PatchMapping("/{vehicleId}/approve")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<VehicleResponse> approveVehicle(@PathVariable Long vehicleId) {
    Vehicle approved = vehicleService.approveVehicle(vehicleId);
    return ResponseEntity.ok(VehicleResponse.from(approved));
  }

  /** 관리자가 운송수단을 반려한다. */
  @PatchMapping("/{vehicleId}/reject")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<VehicleResponse> rejectVehicle(@PathVariable Long vehicleId) {
    Vehicle rejected = vehicleService.rejectVehicle(vehicleId);
    return ResponseEntity.ok(VehicleResponse.from(rejected));
  }

  /** 운송수단 정보를 수정한다. */
  @PutMapping("/{vehicleId}")
  public ResponseEntity<VehicleResponse> updateVehicle(
      @PathVariable Long vehicleId, @RequestBody @Valid VehicleUpdateRequest request) {
    Vehicle updated = vehicleService.update(vehicleId, request);
    return ResponseEntity.ok(VehicleResponse.from(updated));
  }

  /** 운송수단을 삭제한다. */
  @DeleteMapping("/{vehicleId}")
  public ResponseEntity<Void> deleteVehicle(@PathVariable Long vehicleId) {
    vehicleService.delete(vehicleId);
    return ResponseEntity.noContent().build();
  }
}
