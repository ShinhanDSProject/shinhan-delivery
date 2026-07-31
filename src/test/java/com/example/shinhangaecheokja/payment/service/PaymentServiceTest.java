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
import com.example.shinhangaecheokja.payment.dto.response.PointWalletResponse;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.exception.InsufficientPointException;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
import java.util.Optional;
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
  void 회원이_존재하면_잔액0인_지갑을_생성한다() {
    PointWalletCreateRequest request = new PointWalletCreateRequest();
    request.setMemberId(1L);

    when(paymentRepository.save(any(PointWallet.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PointWallet response = paymentService.createWallet(request);

    assertThat(response.getMemberId()).isEqualTo(1L);
    assertThat(response.getBalance()).isEqualTo(0L);
  }

  @Test
  void 존재하지_않는_회원이면_EntityNotFoundException을_던진다() {
    PointWalletCreateRequest request = new PointWalletCreateRequest();
    request.setMemberId(999L);

    when(memberService.getMember(999L))
        .thenThrow(new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    assertThatThrownBy(() -> paymentService.createWallet(request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void 존재하지_않는_지갑을_조회하면_EntityNotFoundException을_던진다() {
    when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.getWallet(1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void 존재하는_지갑을_조회하면_PointWalletResponse를_반환한다() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findById(1L)).thenReturn(Optional.of(wallet));

    PointWalletResponse response = paymentService.getWallet(1L);

    assertThat(response.balance()).isEqualTo(1000L);
  }

  @Test
  void 포인트를_충전하면_잔액이_증가한다() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(500L);

    PointWalletResponse response = paymentService.chargePoint(1L, request);

    assertThat(response.balance()).isEqualTo(1500L);
  }

  @Test
  void 잔액이_충분하면_포인트를_사용한다() {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(1L);
    wallet.setBalance(1000L);
    when(paymentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(wallet));

    PointUseRequest request = new PointUseRequest();
    request.setAmount(300L);

    PointWalletResponse response = paymentService.usePoint(1L, request);

    assertThat(response.balance()).isEqualTo(700L);
  }

  @Test
  void 잔액이_부족하면_InsufficientPointException을_던진다() {
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
