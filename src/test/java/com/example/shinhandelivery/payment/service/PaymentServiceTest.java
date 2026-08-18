package com.example.shinhandelivery.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.dto.request.PinVerifyRequestDto;
import com.example.shinhandelivery.payment.dto.request.PointChargeRequest;
import com.example.shinhandelivery.payment.dto.request.PointRefundRequest;
import com.example.shinhandelivery.payment.dto.request.PointUseRequest;
import com.example.shinhandelivery.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhandelivery.payment.dto.response.PinVerifyResponseDto;
import com.example.shinhandelivery.payment.dto.response.PointBalanceResponse;
import com.example.shinhandelivery.payment.dto.response.PointHistoryItemResponse;
import com.example.shinhandelivery.payment.dto.response.PointRefundResponse;
import com.example.shinhandelivery.payment.dto.response.PointUseResultResponse;
import com.example.shinhandelivery.payment.entity.PaymentMethod;
import com.example.shinhandelivery.payment.entity.PointHistory;
import com.example.shinhandelivery.payment.entity.PointHistoryType;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.exception.InsufficientPointException;
import com.example.shinhandelivery.payment.exception.InvalidPointAmountException;
import com.example.shinhandelivery.payment.exception.PointBalanceOverflowException;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import com.example.shinhandelivery.payment.repository.PointHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private MemberService memberService;
  @Mock private PointHistoryRepository pointHistoryRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private PointWalletProvisioningService pointWalletProvisioningService;
  @InjectMocks private PaymentService paymentService;

  @Test
  @DisplayName("올바른 PIN이면 verified=true를 반환하고 실패 횟수를 초기화한다")
  void verifyPinSuccess() {
    Member member = new Member();
    member.setId(1L);
    member.setPinHash("encoded-pin");
    member.setPinFailCount(2);
    when(memberService.getByIdForUpdate(1L)).thenReturn(member);
    when(passwordEncoder.matches("123456", "encoded-pin")).thenReturn(true);

    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("123456");

    PinVerifyResponseDto response = paymentService.verifyPin(1L, request);

    assertThat(response.verified()).isTrue();
    assertThat(member.getPinFailCount()).isEqualTo(0);
    assertThat(member.isPinLocked()).isFalse();
  }

  @Test
  @DisplayName("잘못된 PIN이면 verified=false를 반환한다")
  void verifyPinFailure() {
    Member member = new Member();
    member.setId(1L);
    member.setPinHash("encoded-pin");
    when(memberService.getByIdForUpdate(1L)).thenReturn(member);
    when(passwordEncoder.matches("654321", "encoded-pin")).thenReturn(false);

    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("654321");

    PinVerifyResponseDto response = paymentService.verifyPin(1L, request);

    assertThat(response.verified()).isFalse();
    assertThat(member.getPinFailCount()).isEqualTo(1);
    assertThat(member.isPinLocked()).isFalse();
  }

  @Test
  @DisplayName("3회 연속 실패하면 PIN_LOCKED 예외를 던진다")
  void verifyPinLocksAfterThreeFailures() {
    Member member = new Member();
    member.setId(1L);
    member.setPinHash("encoded-pin");
    member.setPinFailCount(2);
    when(memberService.getByIdForUpdate(1L)).thenReturn(member);
    when(passwordEncoder.matches("654321", "encoded-pin")).thenReturn(false);

    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("654321");

    assertThatThrownBy(() -> paymentService.verifyPin(1L, request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PIN_LOCKED.getMessage());
    assertThat(member.getPinFailCount()).isEqualTo(3);
    assertThat(member.isPinLocked()).isTrue();
  }

  @Test
  @DisplayName("이미 잠긴 회원은 즉시 PIN_LOCKED 예외를 던진다")
  void verifyPinAlreadyLockedShouldThrowException() {
    Member member = new Member();
    member.setId(1L);
    member.setPinLocked(true);
    when(memberService.getByIdForUpdate(1L)).thenReturn(member);

    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("123456");

    assertThatThrownBy(() -> paymentService.verifyPin(1L, request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.PIN_LOCKED.getMessage());
  }

  @Test
  @DisplayName("회원이 존재하면 잔액 0인 지갑을 생성한다")
  void createWalletSuccess() {
    PointWalletCreateRequest request = new PointWalletCreateRequest();
    request.setMemberId(1L);

    when(memberService.getById(1L)).thenReturn(new Member());
    when(pointWalletProvisioningService.ensureWallet(1L)).thenReturn(PointWallet.createEmpty(1L));

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
  @DisplayName("충전 금액이 0 이하이면 InvalidPointAmountException을 던진다")
  void chargePointNonPositiveAmountShouldThrowException() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(0L);
    request.setPaymentMethod(PaymentMethod.CARD);

    assertThatThrownBy(() -> paymentService.chargePoint(1L, request))
        .isInstanceOf(InvalidPointAmountException.class);
  }

  @Test
  @DisplayName("충전 후 잔액이 long 범위를 넘어서면 PointBalanceOverflowException을 던진다")
  void chargePointOverflowShouldThrowException() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(Long.MAX_VALUE);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(1L);
    request.setPaymentMethod(PaymentMethod.CARD);

    assertThatThrownBy(() -> paymentService.chargePoint(1L, request))
        .isInstanceOf(PointBalanceOverflowException.class);
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
    when(pointWalletProvisioningService.ensureWallet(1L))
        .thenReturn(PointWallet.createEmpty(1L));

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

  @Test
  @DisplayName("사용 금액이 0 이하이면 InvalidPointAmountException을 던진다")
  void usePointNonPositiveAmountShouldThrowException() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointUseRequest request = new PointUseRequest();
    request.setAmount(-1L);

    assertThatThrownBy(() -> paymentService.usePoint(1L, request))
        .isInstanceOf(InvalidPointAmountException.class);
  }

  @Test
  @DisplayName("회원 기준 포인트 차감 시 사용 이력을 저장하고 결과를 반환한다")
  void usePointByMemberCreatesHistory() {
    Member member = new Member();
    member.setId(1L);
    when(memberService.getById(1L)).thenReturn(member);
    when(pointHistoryRepository.findByMemberIdAndIdempotencyKey(1L, "pay-1"))
        .thenReturn(Optional.empty());
    when(pointWalletProvisioningService.ensureWallet(1L))
        .thenReturn(PointWallet.createEmpty(1L));

    PointWallet wallet = new PointWallet();
    wallet.setId(10L);
    wallet.setMemberId(1L);
    wallet.setBalance(5000L);
    when(paymentRepository.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));
    when(pointHistoryRepository.save(any(PointHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointUseRequest request = new PointUseRequest();
    request.setAmount(3000L);

    PointUseResultResponse response = paymentService.usePoint(1L, "pay-1", request);

    ArgumentCaptor<PointHistory> historyCaptor = ArgumentCaptor.forClass(PointHistory.class);
    verify(pointHistoryRepository).save(historyCaptor.capture());
    PointHistory savedHistory = historyCaptor.getValue();

    assertThat(response.balance()).isEqualTo(2000L);
    assertThat(response.usedAmount()).isEqualTo(3000L);
    assertThat(response.paidAt()).isNotNull();
    assertThat(savedHistory.getMemberId()).isEqualTo(1L);
    assertThat(savedHistory.getIdempotencyKey()).isEqualTo("pay-1");
    assertThat(savedHistory.getAmount()).isEqualTo(3000L);
  }

  @Test
  @DisplayName("동일 멱등 키의 포인트 차감 재호출 시 기존 결과를 재사용한다")
  void usePointByMemberDuplicateIdempotencyKeyReusesExistingHistory() {
    Member member = new Member();
    member.setId(1L);
    when(memberService.getById(1L)).thenReturn(member);

    PointHistory history =
        PointHistory.builder()
            .memberId(1L)
            .walletId(10L)
            .amount(2000L)
            .balanceAfter(7000L)
            .type(PointHistoryType.USE)
            .idempotencyKey("pay-2")
            .createdAt(LocalDateTime.of(2026, 8, 4, 10, 0))
            .build();
    when(pointHistoryRepository.findByMemberIdAndIdempotencyKey(1L, "pay-2"))
        .thenReturn(Optional.of(history));

    PointUseRequest request = new PointUseRequest();
    request.setAmount(2000L);

    PointUseResultResponse response = paymentService.usePoint(1L, "pay-2", request);

    assertThat(response.balance()).isEqualTo(7000L);
    assertThat(response.usedAmount()).isEqualTo(2000L);
    assertThat(response.paidAt()).isEqualTo(LocalDateTime.of(2026, 8, 4, 10, 0));
    verify(paymentRepository, never()).findByMemberIdForUpdate(1L);
    verify(pointWalletProvisioningService, never()).ensureWallet(1L);
    verify(pointHistoryRepository, never()).save(any(PointHistory.class));
  }

  @Test
  @DisplayName("지갑이 없는 회원도 첫 포인트 충전 시 지갑을 자동 생성한 뒤 성공한다")
  void chargePointCreatesWalletWhenMissing() {
    Member member = new Member();
    member.setId(1L);
    when(memberService.getById(1L)).thenReturn(member);
    when(pointHistoryRepository.findByMemberIdAndIdempotencyKey(1L, "idem-first"))
        .thenReturn(Optional.empty());
    when(pointWalletProvisioningService.ensureWallet(1L))
        .thenReturn(PointWallet.createEmpty(1L));

    PointWallet wallet = new PointWallet();
    wallet.setId(20L);
    wallet.setMemberId(1L);
    wallet.setBalance(0L);
    when(paymentRepository.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));
    when(pointHistoryRepository.save(any(PointHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(7000L);
    request.setPaymentMethod(PaymentMethod.CARD);

    PointBalanceResponse response = paymentService.chargePoint(1L, "idem-first", request);

    assertThat(response.balance()).isEqualTo(7000L);
    verify(pointWalletProvisioningService).ensureWallet(1L);
  }

  @Test
  @DisplayName("배송 취소 환불은 지갑 잔액을 늘리고 참조 배송 이력을 저장한다")
  void refundPointCreatesRefundHistory() {
    when(memberService.getById(1L)).thenReturn(new Member());
    PointWallet wallet = PointWallet.builder().id(10L).memberId(1L).balance(1000L).build();
    when(paymentRepository.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));
    when(pointHistoryRepository.findByTypeAndReferenceId(PointHistoryType.REFUND, 55L))
        .thenReturn(Optional.empty());
    when(pointHistoryRepository.save(any(PointHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointRefundResponse response =
        paymentService.refundPoint(1L, "refund-55", PointRefundRequest.of(3000L, 55L, "자동 취소 환불"));

    ArgumentCaptor<PointHistory> historyCaptor = ArgumentCaptor.forClass(PointHistory.class);
    verify(pointHistoryRepository).save(historyCaptor.capture());
    PointHistory history = historyCaptor.getValue();
    assertThat(response.balance()).isEqualTo(4000L);
    assertThat(response.refundedAmount()).isEqualTo(3000L);
    assertThat(history.getType()).isEqualTo(PointHistoryType.REFUND);
    assertThat(history.getReferenceId()).isEqualTo(55L);
    assertThat(history.getDescription()).isEqualTo("자동 취소 환불");
  }

  @Test
  @DisplayName("동일 멱등 키 환불은 기존 이력을 반환하고 잔액을 다시 늘리지 않는다")
  void refundPointDuplicateKeyReusesExistingHistory() {
    when(memberService.getById(1L)).thenReturn(new Member());
    PointWallet wallet = PointWallet.builder().id(10L).memberId(1L).balance(4000L).build();
    when(paymentRepository.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));
    PointHistory history =
        PointHistory.builder()
            .memberId(1L)
            .walletId(10L)
            .amount(3000L)
            .balanceAfter(4000L)
            .type(PointHistoryType.REFUND)
            .idempotencyKey("refund-55")
            .referenceId(55L)
            .createdAt(LocalDateTime.of(2026, 8, 11, 16, 0))
            .build();
    when(pointHistoryRepository.findByTypeAndReferenceId(PointHistoryType.REFUND, 55L))
        .thenReturn(Optional.of(history));

    PointRefundResponse response =
        paymentService.refundPoint(1L, "refund-55", PointRefundRequest.of(3000L, 55L, "자동 취소 환불"));

    assertThat(response.balance()).isEqualTo(4000L);
    assertThat(wallet.getBalance()).isEqualTo(4000L);
    verify(pointHistoryRepository, never()).save(any(PointHistory.class));
  }

  @Test
  @DisplayName("회원 기준 최근 포인트 이력을 최신순 응답 DTO로 반환한다")
  void getRecentPointHistoriesReturnsMappedResponses() {
    Member member = new Member();
    member.setId(1L);
    when(memberService.getById(1L)).thenReturn(member);
    when(paymentRepository.findByMemberId(1L))
        .thenReturn(
            Optional.of(PointWallet.builder().id(10L).memberId(1L).balance(15000L).build()));

    PointHistory chargeHistory =
        PointHistory.builder()
            .id(11L)
            .memberId(1L)
            .walletId(10L)
            .amount(5000L)
            .balanceAfter(15000L)
            .type(PointHistoryType.CHARGE)
            .paymentMethod(PaymentMethod.CARD)
            .createdAt(LocalDateTime.of(2026, 8, 18, 9, 0))
            .build();
    PointHistory useHistory =
        PointHistory.builder()
            .id(12L)
            .memberId(1L)
            .walletId(10L)
            .amount(2000L)
            .balanceAfter(13000L)
            .type(PointHistoryType.USE)
            .createdAt(LocalDateTime.of(2026, 8, 18, 8, 30))
            .build();

    when(pointHistoryRepository.findTop20ByMemberIdOrderByCreatedAtDescIdDesc(1L))
        .thenReturn(List.of(chargeHistory, useHistory));

    List<PointHistoryItemResponse> responses = paymentService.getRecentPointHistories(1L);

    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).historyId()).isEqualTo(11L);
    assertThat(responses.get(0).type()).isEqualTo(PointHistoryType.CHARGE);
    assertThat(responses.get(0).paymentMethod()).isEqualTo(PaymentMethod.CARD);
    assertThat(responses.get(1).historyId()).isEqualTo(12L);
    assertThat(responses.get(1).type()).isEqualTo(PointHistoryType.USE);
  }

  @Test
  @DisplayName("포인트 지갑이 없으면 최근 포인트 이력 조회 시 예외를 던진다")
  void getRecentPointHistoriesWithoutWalletShouldThrowException() {
    when(memberService.getById(1L)).thenReturn(new Member());
    when(paymentRepository.findByMemberId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.getRecentPointHistories(1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(ErrorCode.POINT_WALLET_NOT_FOUND.getMessage());
  }
}
