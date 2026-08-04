package com.example.shinhandelivery.payment.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.dto.request.PointChargeRequest;
import com.example.shinhandelivery.payment.dto.request.PointUseRequest;
import com.example.shinhandelivery.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.exception.InsufficientPointException;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
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
}
