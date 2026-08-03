package com.example.shinhangaecheokja.payment.service;

import com.example.shinhangaecheokja.common.exception.BusinessException;
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
  private final PointHistoryRepository pointHistoryRepository;
  private final MemberService memberService;

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
   * 포인트 지갑에 포인트를 충전한다. 동일 지갑에 대한 동시 충전·차감 요청이 잔액을 잃어버리지 않도록 비관적 쓰기 락으로 지갑을 조회해, 호출한 트랜잭션이 끝날 때까지 해당
   * 지갑 행을 잠근다.
   */
  @Transactional
  public PointWallet chargePoint(Long walletId, PointChargeRequest request) {
    PointWallet wallet = findWalletForUpdateOrThrow(walletId);
    wallet.setBalance(wallet.getBalance() + request.getAmount());
    return wallet;
  }

  /** 로그인 회원의 포인트를 멱등성 키 기준으로 한 번만 충전하고, 갱신된 잔액과 마지막 충전 시각을 반환한다. */
  @Transactional
  public PointBalanceResponse charge(
      Long memberId, String idempotencyKey, PointChargeRequest request) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Idempotency-Key 헤더는 필수입니다.");
    }

    PointWallet wallet = findWalletForUpdateByMemberIdOrThrow(memberId);
    PointHistory duplicatedHistory =
        pointHistoryRepository
            .findByWalletIdAndIdempotencyKey(wallet.getId(), idempotencyKey)
            .orElse(null);
    if (duplicatedHistory != null) {
      return new PointBalanceResponse(
          duplicatedHistory.getBalanceAfter(), duplicatedHistory.getCreatedAt());
    }

    long updatedBalance = wallet.getBalance() + request.getAmount();
    wallet.setBalance(updatedBalance);

    PointHistory history = new PointHistory();
    history.setWalletId(wallet.getId());
    history.setType(PointHistoryType.CHARGE);
    history.setAmount(request.getAmount());
    history.setPaymentMethod(request.getPaymentMethod());
    history.setIdempotencyKey(idempotencyKey);
    history.setBalanceAfter(updatedBalance);
    history.setCreatedAt(LocalDateTime.now());
    pointHistoryRepository.save(history);

    return new PointBalanceResponse(updatedBalance, history.getCreatedAt());
  }

  /** 로그인 회원의 현재 포인트 잔액과 마지막 충전 시각을 조회한다. */
  @Transactional(readOnly = true)
  public PointBalanceResponse getBalance(Long memberId) {
    PointWallet wallet = findWalletByMemberIdOrThrow(memberId);
    LocalDateTime lastChargedAt =
        pointHistoryRepository
            .findTopByWalletIdAndTypeOrderByCreatedAtDesc(wallet.getId(), PointHistoryType.CHARGE)
            .map(PointHistory::getCreatedAt)
            .orElse(null);
    return new PointBalanceResponse(wallet.getBalance(), lastChargedAt);
  }

  /**
   * 다른 도메인 Service가 회원의 포인트를 차감할 때 사용한다. 잔액이 부족하면 InsufficientPointException. 동일 지갑에 대한 동시 차감 요청으로
   * 잔액이 음수가 되지 않도록 비관적 쓰기 락으로 지갑을 조회한다.
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

  private PointWallet findWalletByMemberIdOrThrow(Long memberId) {
    return paymentRepository
        .findByMemberId(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_WALLET_NOT_FOUND));
  }

  private PointWallet findWalletForUpdateByMemberIdOrThrow(Long memberId) {
    return paymentRepository
        .findByMemberIdForUpdate(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POINT_WALLET_NOT_FOUND));
  }
}
