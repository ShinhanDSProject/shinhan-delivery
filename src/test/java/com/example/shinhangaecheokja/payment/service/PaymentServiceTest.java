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
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.exception.InsufficientPointException;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
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
}
