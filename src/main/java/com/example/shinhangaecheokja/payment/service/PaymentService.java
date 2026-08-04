package com.example.shinhangaecheokja.payment.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointUseRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointBalanceResponse;
import com.example.shinhangaecheokja.payment.entity.PointHistory;
import com.example.shinhangaecheokja.payment.entity.PointHistoryType;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.exception.InsufficientPointException;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
import com.example.shinhangaecheokja.payment.repository.PointHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PointWallet 관련 유스케이스(생성/조회/충전/사용/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final MemberService memberService;
  private final PointHistoryRepository pointHistoryRepository;

  /** 회원 존재 여부를 검증한 뒤 잔액 0인 포인트 지갑을 생성한다 (PointWallet Entity 리턴). */
  @Transactional
  public PointWallet create(PointWalletCreateRequest request) {
    memberService.getById(request.getMemberId());

    return paymentRepository.save(PointWallet.createEmpty(request.getMemberId()));
  }

  /** id로 포인트 지갑 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public PointWallet getById(Long walletId) {
    return findWalletOrThrow(walletId);
  }

  /** 전체 포인트 지갑 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<PointWallet> list() {
    return paymentRepository.findAll();
  }

  /**
   * 포인트 지갑에 포인트를 충전한다. 동일 지갑에 대한 동시 충전/차감 요청이 잔액을 잃어버리지 않도록 비관적 쓰기 락으로 지갑을 조회해 트랜잭션이 끝날 때까지 해당 지갑 락을
   * 점유한다.
   */
  @Transactional
  public PointWallet chargePoint(Long walletId, PointChargeRequest request) {
    PointWallet wallet = findWalletForUpdateOrThrow(walletId);
    wallet.setBalance(wallet.getBalance() + request.getAmount());
    return wallet;
  }

  /** 인증된 회원 기준으로 포인트를 충전하고, 동일 멱등 키 재호출 시 기존 결과를 재사용한다. */
  @Transactional
  public PointBalanceResponse chargePoint(
      Long memberId, String idempotencyKey, PointChargeRequest request) {
    memberService.getById(memberId);

    PointHistory existing =
        pointHistoryRepository.findByMemberIdAndIdempotencyKey(memberId, idempotencyKey).orElse(null);
    if (existing != null) {
      return new PointBalanceResponse(existing.getBalanceAfter(), existing.getCreatedAt());
    }

    PointWallet wallet = findWalletByMemberForUpdateOrThrow(memberId);
    wallet.setBalance(wallet.getBalance() + request.getAmount());

    LocalDateTime chargedAt = LocalDateTime.now();
    pointHistoryRepository.save(
        PointHistory.builder()
            .memberId(memberId)
            .walletId(wallet.getId())
            .amount(request.getAmount())
            .balanceAfter(wallet.getBalance())
            .type(PointHistoryType.CHARGE)
            .paymentMethod(request.getPaymentMethod())
            .idempotencyKey(idempotencyKey)
            .createdAt(chargedAt)
            .build());
    return new PointBalanceResponse(wallet.getBalance(), chargedAt);
  }

  /**
   * 다른 도메인 Service가 회원의 포인트를 차감할 때 사용한다. 잔액이 부족하면 InsufficientPointException. 동일 지갑에 대한 동시 차감 요청으로 잔액이 음수로
   * 가지 않도록 비관적 쓰기 락으로 지갑을 조회한다.
   */
  @Transactional
  public PointWallet usePoint(Long walletId, PointUseRequest request) {
    PointWallet wallet = findWalletForUpdateOrThrow(walletId);
    if (wallet.getBalance() < request.getAmount()) {
      throw new InsufficientPointException(walletId, request.getAmount());
    }
    wallet.setBalance(wallet.getBalance() - request.getAmount());
    return wallet;
  }

  /** id로 포인트 지갑을 조회해 삭제한다. 없으면 EntityNotFoundException. */
  @Transactional
  public void delete(Long walletId) {
    PointWallet wallet = findWalletOrThrow(walletId);
    paymentRepository.delete(wallet);
  }

  private PointWallet findWalletOrThrow(Long walletId) {
    return paymentRepository
        .findById(walletId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_WALLET_NOT_FOUND));
  }

  private PointWallet findWalletForUpdateOrThrow(Long walletId) {
    return paymentRepository
        .findByIdForUpdate(walletId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_WALLET_NOT_FOUND));
  }

  private PointWallet findWalletByMemberForUpdateOrThrow(Long memberId) {
    return paymentRepository
        .findByMemberIdForUpdate(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_WALLET_NOT_FOUND));
  }
}
