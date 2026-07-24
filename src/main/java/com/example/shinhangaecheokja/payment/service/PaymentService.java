package com.example.shinhangaecheokja.payment.service;

import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointUseRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointWalletResponse;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.exception.InsufficientPointException;
import com.example.shinhangaecheokja.payment.exception.PointWalletNotFoundException;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
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

  /** 회원 존재 여부를 검증한 뒤 잔액 0인 포인트 지갑을 생성한다. */
  @Transactional
  public PointWalletResponse createWallet(PointWalletCreateRequest request) {
    memberService.getMember(request.getMemberId());

    PointWallet wallet = new PointWallet();
    wallet.setMemberId(request.getMemberId());
    wallet.setBalance(0L);

    return PointWalletResponse.from(paymentRepository.save(wallet));
  }

  /** id로 포인트 지갑 단건을 조회한다. 없으면 PointWalletNotFoundException. */
  @Transactional(readOnly = true)
  public PointWalletResponse getWallet(Long walletId) {
    return PointWalletResponse.from(findWalletOrThrow(walletId));
  }

  /** 전체 포인트 지갑 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<PointWalletResponse> getWallets() {
    return paymentRepository.findAll().stream().map(PointWalletResponse::from).toList();
  }

  /** 포인트 지갑에 포인트를 충전한다. */
  @Transactional
  public PointWalletResponse chargePoint(Long walletId, PointChargeRequest request) {
    PointWallet wallet = findWalletOrThrow(walletId);
    wallet.setBalance(wallet.getBalance() + request.getAmount());
    return PointWalletResponse.from(wallet);
  }

  /** 다른 도메인 Service가 회원의 포인트를 차감할 때 사용한다. 잔액이 부족하면 InsufficientPointException. */
  @Transactional
  public PointWalletResponse usePoint(Long walletId, PointUseRequest request) {
    PointWallet wallet = findWalletOrThrow(walletId);
    if (wallet.getBalance() < request.getAmount()) {
      throw new InsufficientPointException(walletId, request.getAmount());
    }
    wallet.setBalance(wallet.getBalance() - request.getAmount());
    return PointWalletResponse.from(wallet);
  }

  /** id로 포인트 지갑을 조회해 삭제한다. 없으면 PointWalletNotFoundException. */
  @Transactional
  public void deleteWallet(Long walletId) {
    PointWallet wallet = findWalletOrThrow(walletId);
    paymentRepository.delete(wallet);
  }

  private PointWallet findWalletOrThrow(Long walletId) {
    return paymentRepository
        .findById(walletId)
        .orElseThrow(() -> new PointWalletNotFoundException(walletId));
  }
}
