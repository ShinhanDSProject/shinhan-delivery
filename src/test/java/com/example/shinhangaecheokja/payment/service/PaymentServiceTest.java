package com.example.shinhangaecheokja.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointUseRequest;
import com.example.shinhangaecheokja.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointBalanceResponse;
import com.example.shinhangaecheokja.payment.entity.PaymentMethod;
import com.example.shinhangaecheokja.payment.entity.PointHistory;
import com.example.shinhangaecheokja.payment.entity.PointHistoryType;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.exception.InsufficientPointException;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
import com.example.shinhangaecheokja.payment.repository.PointHistoryRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private MemberService memberService;
  @Mock private PointHistoryRepository pointHistoryRepository;
  @InjectMocks private PaymentService paymentService;

  @Test
  @DisplayName("회원이 존재하면 잔액 0인 지갑을 생성한다")
  void createWalletSuccess() {
    PointWalletCreateRequest request = new PointWalletCreateRequest();
    request.setMemberId(1L);

    when(paymentRepository.save(any(PointWallet.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointWallet response = paymentService.create(request);

    assertThat(response.getMemberId()).isEqualTo(1L);
    assertThat(response.getBalance()).isEqualTo(0L);
  }

  @Test
  @DisplayName("존재하지 않는 회원이면 EntityNotFoundException을 던진다")
  void createWalletMemberNotFoundShouldThrowException() {
    PointWalletCreateRequest request = new PointWalletCreateRequest();
    request.setMemberId(999L);

    when(memberService.getById(999L))
        .thenThrow(new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    assertThatThrownBy(() -> paymentService.create(request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하지 않는 지갑을 조회하면 EntityNotFoundException을 던진다")
  void getWalletNotFoundShouldThrowException() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.getById(1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하는 지갑을 조회하면 PointWallet을 반환한다")
  void getWalletSuccess() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(wallet));

    PointWallet response = paymentService.getById(1L);

    assertThat(response.getBalance()).isEqualTo(1000L);
  }

  @Test
  @DisplayName("포인트를 충전하면 잔액이 증가한다")
  void chargePointSuccess() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(500L);
    request.setPaymentMethod(PaymentMethod.CARD);

    PointWallet response = paymentService.chargePoint(1L, request);

    assertThat(response.getBalance()).isEqualTo(1500L);
  }

  @Test
  @DisplayName("동일 멱등 키 재호출 시 기존 충전 결과를 재사용한다")
  void chargePointWithDuplicateIdempotencyKeyReusesExistingHistory() {
    Member member = new Member();
    member.setId(1L);
    when(memberService.getById(1L)).thenReturn(member);

    PointHistory history =
        PointHistory.builder()
            .memberId(1L)
            .walletId(2L)
            .amount(3000L)
            .balanceAfter(13000L)
            .type(PointHistoryType.CHARGE)
            .paymentMethod(PaymentMethod.CARD)
            .idempotencyKey("idem-1")
            .createdAt(LocalDateTime.of(2026, 8, 4, 1, 0))
            .build();
    when(pointHistoryRepository.findByMemberIdAndIdempotencyKey(1L, "idem-1"))
        .thenReturn(Optional.of(history));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(3000L);
    request.setPaymentMethod(PaymentMethod.CARD);

    PointBalanceResponse response = paymentService.chargePoint(1L, "idem-1", request);

    assertThat(response.balance()).isEqualTo(13000L);
    assertThat(response.lastChargedAt()).isEqualTo(LocalDateTime.of(2026, 8, 4, 1, 0));
  }

  @Test
  @DisplayName("신규 멱등 키 충전이면 이력을 저장하고 마지막 충전 시각을 반환한다")
  void chargePointCreatesHistoryAndReturnsLastChargedAt() {
    Member member = new Member();
    member.setId(1L);
    when(memberService.getById(1L)).thenReturn(member);
    when(pointHistoryRepository.findByMemberIdAndIdempotencyKey(1L, "idem-2"))
        .thenReturn(Optional.empty());

    PointWallet wallet = new PointWallet();
    wallet.setId(10L);
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));
    when(pointHistoryRepository.save(any(PointHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(500L);
    request.setPaymentMethod(PaymentMethod.EASY_PAY);

    PointBalanceResponse response = paymentService.chargePoint(1L, "idem-2", request);

    assertThat(response.balance()).isEqualTo(1500L);
    assertThat(response.lastChargedAt()).isNotNull();
  }

  @Test
  @DisplayName("잔액이 충분하면 포인트를 사용한다")
  void usePointSuccess() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointUseRequest request = new PointUseRequest();
    request.setAmount(300L);

    PointWallet response = paymentService.usePoint(1L, request);

    assertThat(response.getBalance()).isEqualTo(700L);
  }

  @Test
  @DisplayName("잔액이 부족하면 InsufficientPointException을 던진다")
  void usePointInsufficientPointShouldThrowException() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(100L);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointUseRequest request = new PointUseRequest();
    request.setAmount(500L);

    assertThatThrownBy(() -> paymentService.usePoint(1L, request))
        .isInstanceOf(InsufficientPointException.class);
  }
}
