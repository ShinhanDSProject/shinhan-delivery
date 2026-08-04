package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetCodeRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetConfirmRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetVerifyRequestDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetCodeResponseDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetVerifyResponseDto;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.entity.PasswordResetToken;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.member.repository.PasswordResetTokenRepository;
import java.time.LocalDateTime;
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
class PasswordResetServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @InjectMocks private PasswordResetService passwordResetService;

  private Member member;

  @BeforeEach
  void setUp() {
    member =
        Member.builder()
            .id(1L)
            .email("test@example.com")
            .password(passwordEncoder.encode("OldPassword123!"))
            .name("테스터")
            .phoneNumber("010-1234-5678")
            .role(MemberRole.CUSTOMER)
            .build();
  }

  @Test
  @DisplayName("정상 가입된 이메일로 인증코드 발송 요청 시 6자리 코드 토큰을 생성하고 저장한다")
  void requestCodeSuccess() {
    given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

    PasswordResetCodeRequestDto request = new PasswordResetCodeRequestDto("test@example.com");
    PasswordResetCodeResponseDto response = passwordResetService.requestCode(request);

    assertThat(response.getExpiresInSeconds()).isEqualTo(180);
    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
  }

  @Test
  @DisplayName("가입되지 않은 이메일로 발송 요청 시 MEMBER_NOT_FOUND 예외가 발생한다")
  void requestCodeNotFound() {
    given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

    PasswordResetCodeRequestDto request = new PasswordResetCodeRequestDto("unknown@example.com");

    assertThatThrownBy(() -> passwordResetService.requestCode(request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("올바른 6자리 인증코드를 입력하면 일회성 resetToken을 발급한다")
  void verifyCodeSuccess() {
    PasswordResetToken token =
        PasswordResetToken.builder()
            .id(1L)
            .email("test@example.com")
            .verificationCode("123456")
            .expiresAt(LocalDateTime.now().plusMinutes(3))
            .attemptCount(0)
            .build();

    given(passwordResetTokenRepository.findTopByEmailOrderByCreatedAtDesc("test@example.com"))
        .willReturn(Optional.of(token));

    PasswordResetVerifyRequestDto request =
        new PasswordResetVerifyRequestDto("test@example.com", "123456");
    PasswordResetVerifyResponseDto response = passwordResetService.verifyCode(request);

    assertThat(response.getResetToken()).isNotBlank();
    assertThat(token.isVerified()).isTrue();
  }

  @Test
  @DisplayName("만료된 인증코드를 입력하면 RESET_CODE_EXPIRED 예외가 발생한다")
  void verifyCodeExpired() {
    PasswordResetToken token =
        PasswordResetToken.builder()
            .id(1L)
            .email("test@example.com")
            .verificationCode("123456")
            .expiresAt(LocalDateTime.now().minusMinutes(1))
            .build();

    given(passwordResetTokenRepository.findTopByEmailOrderByCreatedAtDesc("test@example.com"))
        .willReturn(Optional.of(token));

    PasswordResetVerifyRequestDto request =
        new PasswordResetVerifyRequestDto("test@example.com", "123456");

    assertThatThrownBy(() -> passwordResetService.verifyCode(request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.RESET_CODE_EXPIRED);
  }

  @Test
  @DisplayName("유효한 resetToken으로 새 비밀번호를 변경하면 암호화되어 적용된다")
  void confirmResetSuccess() {
    PasswordResetToken token =
        PasswordResetToken.builder()
            .id(1L)
            .email("test@example.com")
            .verificationCode("123456")
            .resetToken("token-uuid-1234")
            .isVerified(true)
            .isUsed(false)
            .expiresAt(LocalDateTime.now().plusMinutes(10))
            .build();

    given(passwordResetTokenRepository.findByResetToken("token-uuid-1234"))
        .willReturn(Optional.of(token));
    given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

    PasswordResetConfirmRequestDto request =
        new PasswordResetConfirmRequestDto("token-uuid-1234", "NewPassword123!", "NewPassword123!");

    passwordResetService.confirmReset(request);

    assertThat(passwordEncoder.matches("NewPassword123!", member.getPassword())).isTrue();
    assertThat(token.isUsed()).isTrue();
  }

  @Test
  @DisplayName("새 비밀번호 확인이 일치하지 않으면 PASSWORD_CONFIRMATION_MISMATCH 예외가 발생한다")
  void confirmResetPasswordMismatch() {
    PasswordResetConfirmRequestDto request =
        new PasswordResetConfirmRequestDto(
            "token-uuid-1234", "NewPassword123!", "DifferentPassword123!");

    assertThatThrownBy(() -> passwordResetService.confirmReset(request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
  }
}
