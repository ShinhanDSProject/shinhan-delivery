package com.example.shinhangaecheokja.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
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
  @Mock private PointHistoryRepository pointHistoryRepository;
  @Mock private MemberService memberService;
  @InjectMocks private PaymentService paymentService;

  @Test
  @DisplayName("회원이 존재하면 잔액0인 지갑을 생성한다")
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

  @Test
  @DisplayName("동일한 멱등성 키가 있으면 기존 충전 결과를 재사용한다")
  void chargeShouldReuseDuplicatedHistory() {
    PointWallet wallet = new PointWallet();
    wallet.setId(1L);
    wallet.setMemberId(7L);
    wallet.setBalance(10_000L);

    PointHistory history = new PointHistory();
    history.setWalletId(1L);
    history.setType(PointHistoryType.CHARGE);
    history.setBalanceAfter(12_000L);
    history.setCreatedAt(LocalDateTime.of(2026, 8, 3, 12, 0));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(2_000L);
    request.setPaymentMethod(PaymentMethod.CARD);

    when(paymentRepository.findByMemberIdForUpdate(7L)).thenReturn(Optional.of(wallet));
    when(pointHistoryRepository.findByWalletIdAndIdempotencyKey(1L, "dup-key"))
        .thenReturn(Optional.of(history));

    PointBalanceResponse response = paymentService.charge(7L, "dup-key", request);

    assertThat(response.balance()).isEqualTo(12_000L);
    assertThat(response.lastChargedAt()).isEqualTo(LocalDateTime.of(2026, 8, 3, 12, 0));
    assertThat(wallet.getBalance()).isEqualTo(10_000L);
  }

  @Test
  @DisplayName("잔액 조회 시 마지막 충전 시각을 함께 반환한다")
  void getBalanceShouldIncludeLastChargedAt() {
    PointWallet wallet = new PointWallet();
    wallet.setId(1L);
    wallet.setMemberId(7L);
    wallet.setBalance(25_000L);

    PointHistory history = new PointHistory();
    history.setWalletId(1L);
    history.setType(PointHistoryType.CHARGE);
    history.setCreatedAt(LocalDateTime.of(2026, 8, 3, 14, 30));

    when(paymentRepository.findByMemberId(7L)).thenReturn(Optional.of(wallet));
    when(pointHistoryRepository.findTopByWalletIdAndTypeOrderByCreatedAtDesc(1L, PointHistoryType.CHARGE))
        .thenReturn(Optional.of(history));

    PointBalanceResponse response = paymentService.getBalance(7L);

    assertThat(response.balance()).isEqualTo(25_000L);
    assertThat(response.lastChargedAt()).isEqualTo(LocalDateTime.of(2026, 8, 3, 14, 30));
  }
}
