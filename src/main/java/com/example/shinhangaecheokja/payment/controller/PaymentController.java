package com.example.shinhangaecheokja.payment.controller;

import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointUseRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointWalletResponse;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.service.PaymentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PointWallet CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/point-wallets")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  /** 포인트 지갑을 생성한다. */
  @PostMapping
  public ResponseEntity<PointWalletResponse> createWallet(
      @RequestBody PointWalletCreateRequest request) {
    PointWallet created = paymentService.createWallet(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(PointWalletResponse.from(created));
  }

  /** 포인트 지갑 단건을 조회한다. */
  @GetMapping("/{walletId}")
  public ResponseEntity<PointWalletResponse> getWallet(@PathVariable Long walletId) {
    return ResponseEntity.ok(paymentService.getWallet(walletId));
  }

  /** 포인트 지갑 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<PointWalletResponse>> getWallets() {
    return ResponseEntity.ok(paymentService.getWallets());
  }

  /** 포인트를 충전한다. */
  @PostMapping("/{walletId}/charge")
  public ResponseEntity<PointWalletResponse> chargePoint(
      @PathVariable Long walletId, @RequestBody PointChargeRequest request) {
    return ResponseEntity.ok(paymentService.chargePoint(walletId, request));
  }

  /** 포인트를 사용한다. */
  @PostMapping("/{walletId}/use")
  public ResponseEntity<PointWalletResponse> usePoint(
      @PathVariable Long walletId, @RequestBody PointUseRequest request) {
    return ResponseEntity.ok(paymentService.usePoint(walletId, request));
  }

  /** 포인트 지갑을 삭제한다. */
  @DeleteMapping("/{walletId}")
  public ResponseEntity<Void> deleteWallet(@PathVariable Long walletId) {
    paymentService.deleteWallet(walletId);
    return ResponseEntity.noContent().build();
  }
}
