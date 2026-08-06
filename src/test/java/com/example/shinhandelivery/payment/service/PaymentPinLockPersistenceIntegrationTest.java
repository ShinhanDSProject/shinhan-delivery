package com.example.shinhandelivery.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.payment.dto.request.PinVerifyRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PaymentPinLockPersistenceIntegrationTest {

  @Autowired private PaymentService paymentService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("세 번째 PIN 실패 후 예외가 발생해도 잠금 상태가 DB에 저장된다")
  void verifyPinLockPersistsAfterException() {
    Member saved =
        memberRepository.save(
            Member.builder()
                .email("pin-lock@example.com")
                .password(passwordEncoder.encode("Password1!"))
                .name("핀잠금")
                .phoneNumber("010-9999-9999")
                .role(MemberRole.CUSTOMER)
                .pinHash(passwordEncoder.encode("123456"))
                .pinFailCount(2)
                .pinLocked(false)
                .build());

    PinVerifyRequestDto request = new PinVerifyRequestDto();
    request.setPin("654321");

    assertThatThrownBy(() -> paymentService.verifyPin(saved.getId(), request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PIN_LOCKED);

    Member reloaded = memberRepository.findById(saved.getId()).orElseThrow();
    assertThat(reloaded.getPinFailCount()).isEqualTo(3);
    assertThat(reloaded.isPinLocked()).isTrue();
  }
}
