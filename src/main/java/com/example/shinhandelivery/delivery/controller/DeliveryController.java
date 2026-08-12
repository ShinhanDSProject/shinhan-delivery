package com.example.shinhandelivery.delivery.controller;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPayRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPickupRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryDetailResponseDto;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryListResponseDto;
import com.example.shinhandelivery.delivery.dto.response.DeliveryPaymentResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryResponse;
import com.example.shinhandelivery.delivery.dto.response.MatchingResponse;
import com.example.shinhandelivery.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.service.DeliveryMatchingService;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** DeliveryRequest CRUD 및 대기열/수락 API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/delivery-requests")
@RequiredArgsConstructor
@Validated
public class DeliveryController {

  private final DeliveryService deliveryService;
  private final DeliveryMatchingService deliveryMatchingService;
  private final MemberService memberService;

  /** 로그인한 고객 본인 명의로 배송을 요청한다. */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DeliveryResponse> requestDelivery(
      @RequestBody @Valid DeliveryCreateRequest request) {
    DeliveryRequest created =
        deliveryService.requestDelivery(resolveUserDetails().getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(DeliveryResponse.from(created));
  }

  /** 배송 요청을 생성하지 않고 예상 요금만 미리 계산한다. */
  @PostMapping("/estimate")
  public ResponseEntity<DeliveryEstimateResponse> estimateFee(
      @RequestBody @Valid DeliveryEstimateRequest request) {
    return ResponseEntity.ok(deliveryService.estimateFee(request));
  }

  /** 배송 결제와 배송 요청 생성을 원자적으로 처리한다. */
  @PostMapping("/pay")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DeliveryPaymentResponse> payDelivery(
      @RequestHeader("Idempotency-Key")
          @NotBlank(message = "Idempotency-Key는 필수입니다.")
          @Size(max = 100, message = "Idempotency-Key는 100자 이하여야 합니다.")
          String idempotencyKey,
      @RequestBody @Valid DeliveryPayRequest request) {
    Long memberId = resolveUserDetails().getId();
    memberService.requirePaymentPinConfigured(memberId);
    return ResponseEntity.ok(deliveryService.payDelivery(memberId, idempotencyKey, request));
  }

  /**
   * 배송 요청 상세를 조회한다(배송원 이름·증거사진 포함). 고객 본인 또는 배정된 배송원 본인만 조회할 수 있다. 매칭 대기·완료 화면이 실시간 상태 폴링에 이 API를
   * 쓰므로, 캐시된 과거 상태를 다시 보여주지 않도록 Cache-Control: no-store를 응답에 명시한다.
   */
  @GetMapping("/{deliveryRequestId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DeliveryDetailResponseDto> getDeliveryRequest(
      @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long deliveryRequestId) {
    memberService.requirePaymentPinConfigured(principal.getId());
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(deliveryService.getDeliveryRequestDetail(principal.getId(), deliveryRequestId));
  }

  /** 로그인 회원 본인의 배송 내역을 최신순으로 페이징 조회한다. status로 선택적 필터링이 가능하다. */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Page<DeliveryListResponseDto>> getDeliveryRequests(
      @RequestParam(required = false) DeliveryStatus status,
      @PageableDefault(size = 10) Pageable pageable) {
    Long memberId = resolveUserDetails().getId();
    memberService.requirePaymentPinConfigured(memberId);
    Page<DeliveryListResponseDto> responses =
        deliveryService
            .getMyDeliveryRequests(memberId, status, pageable)
            .map(DeliveryListResponseDto::from);
    return ResponseEntity.ok(responses);
  }

  /** 주변 3km 이내의 대기 중인(REQUESTED) 배송 요청 목록을 배송원 위치 기준으로 거리가 가까운 순으로 조회한다. */
  @GetMapping("/available")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<AvailableDeliveryResponse>> getAvailableDeliveries(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) Double longitude,
      @RequestParam(required = false, defaultValue = "3.0") Double radiusKm) {
    Long memberId = (userDetails != null) ? userDetails.getId() : resolveUserDetails().getId();
    List<AvailableDeliveryResponse> responses =
        deliveryMatchingService.getAvailableDeliveries(memberId, latitude, longitude, radiusKm);
    return ResponseEntity.ok(responses);
  }

  /** 배송원이 특정 대기 중인 주문을 수락(Catch)한다. 동시 수락 시 단 1명만 성공하며, 패배 시 409 Conflict를 반환한다. */
  @PostMapping("/{deliveryRequestId}/catch")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<MatchingResponse> catchDelivery(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long deliveryRequestId) {
    Long memberId = (userDetails != null) ? userDetails.getId() : resolveUserDetails().getId();
    Matching matching = deliveryMatchingService.catchDelivery(memberId, deliveryRequestId);
    return ResponseEntity.status(HttpStatus.CREATED).body(MatchingResponse.from(matching));
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

  /** 배송 요청의 픽업지·도착지를 수정한다. */
  @PutMapping("/{deliveryRequestId}")
  public ResponseEntity<DeliveryResponse> updateDeliveryRequest(
      @PathVariable Long deliveryRequestId, @RequestBody @Valid DeliveryUpdateRequest request) {
    DeliveryRequest updated = deliveryService.updateDeliveryRequest(deliveryRequestId, request);
    return ResponseEntity.ok(DeliveryResponse.from(updated));
  }

  /** 배송 요청을 삭제한다. */
  @DeleteMapping("/{deliveryRequestId}")
  public ResponseEntity<Void> deleteDeliveryRequest(@PathVariable Long deliveryRequestId) {
    deliveryService.deleteDeliveryRequest(deliveryRequestId);
    return ResponseEntity.noContent().build();
  }

  /** 배송원의 픽업 완료를 처리하고 물품 확인 사진 URL을 저장한다. 배정된 배송원 본인만 처리할 수 있다. */
  @PatchMapping("/{deliveryRequestId}/pickup")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DeliveryResponse> confirmPickup(
      @PathVariable Long deliveryRequestId, @RequestBody @Valid DeliveryPickupRequest request) {
    Long memberId = resolveUserDetails().getId();
    DeliveryRequest pickedUp = deliveryService.confirmPickup(memberId, deliveryRequestId, request);
    return ResponseEntity.ok(DeliveryResponse.from(pickedUp));
  }

  /** 배송을 완료 처리하고 증거 사진 URL을 저장한다. 배정된 배송원 본인만 처리할 수 있다. */
  @PatchMapping("/{deliveryRequestId}/complete")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DeliveryResponse> completeDelivery(
      @PathVariable Long deliveryRequestId, @RequestBody @Valid DeliveryCompleteRequest request) {
    Long memberId = resolveUserDetails().getId();
    DeliveryRequest completed =
        deliveryService.completeDelivery(memberId, deliveryRequestId, request);
    return ResponseEntity.ok(DeliveryResponse.from(completed));
  }

  /** 배송 완료 증거 사진을 조회한다. 배송 요청의 고객 본인 또는 배정된 배송원 본인만 조회할 수 있다. */
  @GetMapping("/{deliveryRequestId}/proof-photo")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ProofPhotoResponse> getProofPhoto(@PathVariable Long deliveryRequestId) {
    Long memberId = resolveUserDetails().getId();
    memberService.requirePaymentPinConfigured(memberId);
    return ResponseEntity.ok(deliveryService.getProofPhoto(memberId, deliveryRequestId));
  }
}
