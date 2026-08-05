package com.example.shinhandelivery.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.JwtProvider;
import com.example.shinhandelivery.member.dto.request.MemberPaymentPinUpdateRequest;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberPaymentPinUpdateTest {

  @Mock private MemberRepository memberRepository;
  @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  @Mock private JwtProvider jwtProvider;
  @InjectMocks private MemberService memberService;

  private Member member;

  @BeforeEach
  void setUp() {
    member =
        Member.builder()
            .id(1L)
            .email("user@example.com")
            .password(passwordEncoder.encode("OldPassword1!"))
            .name("홍길동")
            .phoneNumber("010-1234-5678")
            .role(MemberRole.CUSTOMER)
            .build();
    given(memberRepository.findById(1L)).willReturn(Optional.of(member));
  }

  @Test
  @DisplayName("기존 PIN이 없으면 새 PIN을 바로 설정한다")
  void setupPaymentPinSuccess() {
    MemberPaymentPinUpdateRequest request =
        new MemberPaymentPinUpdateRequest("", "123456", "123456");

    memberService.updatePaymentPin(1L, request);

    assertThat(passwordEncoder.matches("123456", member.getPinHash())).isTrue();
    assertThat(member.getPinFailCount()).isZero();
    assertThat(member.isPinLocked()).isFalse();
  }

  @Test
  @DisplayName("기존 PIN이 있으면 현재 PIN 검증 후 변경한다")
  void changePaymentPinSuccess() {
    member.changePaymentPin(passwordEncoder.encode("123456"));
    MemberPaymentPinUpdateRequest request =
        new MemberPaymentPinUpdateRequest("123456", "654321", "654321");

    memberService.updatePaymentPin(1L, request);

    assertThat(passwordEncoder.matches("654321", member.getPinHash())).isTrue();
  }

  @Test
  @DisplayName("새 PIN과 확인 PIN이 다르면 예외가 발생한다")
  void changePaymentPinMismatchShouldThrowException() {
    MemberPaymentPinUpdateRequest request =
        new MemberPaymentPinUpdateRequest("", "123456", "111111");

    assertThatThrownBy(() -> memberService.updatePaymentPin(1L, request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PIN_CONFIRMATION_MISMATCH);
  }

  @Test
  @DisplayName("기존 PIN이 있을 때 현재 PIN이 틀리면 예외가 발생한다")
  void changePaymentPinWrongCurrentPinShouldThrowException() {
    member.changePaymentPin(passwordEncoder.encode("123456"));
    MemberPaymentPinUpdateRequest request =
        new MemberPaymentPinUpdateRequest("999999", "654321", "654321");

    assertThatThrownBy(() -> memberService.updatePaymentPin(1L, request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.CURRENT_PIN_MISMATCH);
  }
}
