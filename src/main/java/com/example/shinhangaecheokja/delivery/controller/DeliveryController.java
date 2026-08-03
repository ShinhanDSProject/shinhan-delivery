package com.example.shinhangaecheokja.delivery.controller;

import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryDetailResponseDto;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryListResponseDto;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** DeliveryRequest CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/delivery-requests")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;

  /** 배송을 요청한다. */
  @PostMapping
  public ResponseEntity<DeliveryResponse> requestDelivery(
      @RequestBody @Valid DeliveryCreateRequest request) {
    DeliveryRequest created = deliveryService.requestDelivery(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(DeliveryResponse.from(created));
  }

  /** 배송 요청을 생성하지 않고 예상 요금만 미리 계산한다. */
  @PostMapping("/estimate")
  public ResponseEntity<DeliveryEstimateResponse> estimateFee(
      @RequestBody @Valid DeliveryEstimateRequest request) {
    return ResponseEntity.ok(deliveryService.estimateFee(request));
  }

  /** 배송 요청 상세를 조회한다(배송원 이름·증거사진 포함). */
  @GetMapping("/{deliveryRequestId}")
  public ResponseEntity<DeliveryDetailResponseDto> getDeliveryRequest(
      @PathVariable Long deliveryRequestId) {
    return ResponseEntity.ok(deliveryService.getDeliveryRequestDetail(deliveryRequestId));
  }

  /** 로그인 회원 본인의 배송 내역을 최신순으로 페이징 조회한다. status로 선택적 필터링이 가능하다. */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Page<DeliveryListResponseDto>> getDeliveryRequests(
      @AuthenticationPrincipal CustomUserDetails principal,
      @RequestParam(required = false) DeliveryStatus status,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<DeliveryListResponseDto> responses =
        deliveryService
            .getMyDeliveryRequests(principal.getId(), status, pageable)
            .map(DeliveryListResponseDto::from);
    return ResponseEntity.ok(responses);
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

  /** 배송원의 픽업 완료를 처리한다. */
  @PatchMapping("/{deliveryRequestId}/pickup")
  public ResponseEntity<DeliveryResponse> confirmPickup(@PathVariable Long deliveryRequestId) {
    DeliveryRequest pickedUp = deliveryService.confirmPickup(deliveryRequestId);
    return ResponseEntity.ok(DeliveryResponse.from(pickedUp));
  }

  /** 배송을 완료 처리하고 증거 사진 URL을 저장한다. */
  @PatchMapping("/{deliveryRequestId}/complete")
  public ResponseEntity<DeliveryResponse> completeDelivery(
      @PathVariable Long deliveryRequestId, @RequestBody @Valid DeliveryCompleteRequest request) {
    DeliveryRequest completed = deliveryService.completeDelivery(deliveryRequestId, request);
    return ResponseEntity.ok(DeliveryResponse.from(completed));
  }

  /** 배송 완료 증거 사진을 조회한다. */
  @GetMapping("/{deliveryRequestId}/proof-photo")
  public ResponseEntity<ProofPhotoResponse> getProofPhoto(@PathVariable Long deliveryRequestId) {
    return ResponseEntity.ok(deliveryService.getProofPhoto(deliveryRequestId));
  }
}
