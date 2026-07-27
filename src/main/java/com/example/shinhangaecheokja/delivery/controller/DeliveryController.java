package com.example.shinhangaecheokja.delivery.controller;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** DeliveryRequest CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/delivery-requests")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryService deliveryService;

  /** 배송을 요청한다. */
  @PostMapping
  public ResponseEntity<DeliveryResponse> requestDelivery(
      @RequestBody @Valid DeliveryCreateRequest request) {
    return ResponseEntity.ok(deliveryService.requestDelivery(request));
  }

  /** 배송 요청 단건을 조회한다. */
  @GetMapping("/{deliveryRequestId}")
  public ResponseEntity<DeliveryResponse> getDeliveryRequest(
      @PathVariable Long deliveryRequestId) {
    return ResponseEntity.ok(deliveryService.getDeliveryRequest(deliveryRequestId));
  }

  /** 배송 요청 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<DeliveryResponse>> getDeliveryRequests() {
    return ResponseEntity.ok(deliveryService.getDeliveryRequests());
  }

  /** 배송 요청의 픽업지·도착지를 수정한다. */
  @PutMapping("/{deliveryRequestId}")
  public ResponseEntity<DeliveryResponse> updateDeliveryRequest(
      @PathVariable Long deliveryRequestId, @RequestBody @Valid DeliveryUpdateRequest request) {
    return ResponseEntity.ok(deliveryService.updateDeliveryRequest(deliveryRequestId, request));
  }

  /** 배송 요청을 삭제한다. */
  @DeleteMapping("/{deliveryRequestId}")
  public ResponseEntity<Void> deleteDeliveryRequest(@PathVariable Long deliveryRequestId) {
    deliveryService.deleteDeliveryRequest(deliveryRequestId);
    return ResponseEntity.noContent().build();
  }
}
