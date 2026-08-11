package com.example.shinhandelivery.payment.service;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.dto.request.PinVerifyRequestDto;
import com.example.shinhandelivery.payment.dto.request.PointChargeRequest;
import com.example.shinhandelivery.payment.dto.request.PointUseRequest;
import com.example.shinhandelivery.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhandelivery.payment.dto.response.PinVerifyResponseDto;
import com.example.shinhandelivery.payment.dto.response.PointBalanceResponse;
import com.example.shinhandelivery.payment.dto.response.PointUseResultResponse;
import com.example.shinhandelivery.payment.entity.PointHistory;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import com.example.shinhandelivery.payment.repository.PointHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PointWallet 관련 유스케이스를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final MemberService memberService;
  private final PointHistoryRepository pointHistoryRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public PointWallet create(PointWalletCreateRequest request) {
    memberService.getById(request.getMemberId());
    return paymentRepository.save(PointWallet.createEmpty(request.getMemberId()));
  }

  @Transactional(readOnly = true)
  public PointWallet getById(Long walletId) {
    return findWalletOrThrow(walletId);
  }

  /** 로그인 회원의 포인트 지갑을 조회한다. 아직 지갑이 없으면 잔액 0으로 새로 만든다. */
  @Transactional
  public PointWallet getOrCreateWallet(Long memberId) {
    return paymentRepository
        .findByMemberId(memberId)
        .orElseGet(() -> paymentRepository.save(PointWallet.createEmpty(memberId)));
  }

  @Transactional(readOnly = true)
  public List<PointWallet> list() {
    return paymentRepository.findAll();
  }

  @Transactional(noRollbackFor = BusinessException.class)
  public PinVerifyResponseDto verifyPin(Long memberId, PinVerifyRequestDto request) {
    Member member = memberService.getByIdForUpdate(memberId);
    if (member.isPinLocked()) {
      throw new BusinessException(ErrorCode.PIN_LOCKED);
    }

    if (member.getPinHash() == null
        || !passwordEncoder.matches(request.getPin(), member.getPinHash())) {
      member.recordPinFailure();
      if (member.isPinLocked()) {
        throw new BusinessException(ErrorCode.PIN_LOCKED);
      }
      return new PinVerifyResponseDto(false);
    }

    member.resetPinFailures();
    return new PinVerifyResponseDto(true);
  }

  @Transactional
  public PointWallet chargePoint(Long walletId, PointChargeRequest request) {
    return findWalletForUpdateOrThrow(walletId).charge(request.getAmount());
  }

  @Transactional
  public PointBalanceResponse chargePoint(
      Long memberId, String idempotencyKey, PointChargeRequest request) {
    memberService.getById(memberId);

    PointHistory existing =
        pointHistoryRepository
            .findByMemberIdAndIdempotencyKey(memberId, idempotencyKey)
            .orElse(null);
    if (existing != null) {
      return new PointBalanceResponse(existing.getBalanceAfter(), existing.getCreatedAt());
    }

    PointWallet wallet = findWalletByMemberForUpdateOrThrow(memberId).charge(request.getAmount());

    PointHistory history =
        pointHistoryRepository.save(
            PointHistory.ofCharge(
                memberId,
                wallet.getId(),
                request.getAmount(),
                wallet.getBalance(),
                request.getPaymentMethod(),
                idempotencyKey));
    return new PointBalanceResponse(wallet.getBalance(), history.getCreatedAt());
  }

  @Transactional
  public PointWallet usePoint(Long walletId, PointUseRequest request) {
    return findWalletForUpdateOrThrow(walletId).use(request.getAmount());
  }

  @Transactional
  public PointUseResultResponse usePoint(
      Long memberId, String idempotencyKey, PointUseRequest request) {
    memberService.getById(memberId);

    PointHistory existing =
        pointHistoryRepository
            .findByMemberIdAndIdempotencyKey(memberId, idempotencyKey)
            .orElse(null);
    if (existing != null) {
      return new PointUseResultResponse(
          existing.getBalanceAfter(), existing.getAmount(), existing.getCreatedAt());
    }

    PointWallet wallet = findWalletByMemberForUpdateOrThrow(memberId).use(request.getAmount());
    PointHistory history =
        pointHistoryRepository.save(
            PointHistory.ofUse(
                memberId,
                wallet.getId(),
                request.getAmount(),
                wallet.getBalance(),
                idempotencyKey));
    return new PointUseResultResponse(
        wallet.getBalance(), request.getAmount(), history.getCreatedAt());
  }

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
