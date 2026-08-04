package com.example.shinhangaecheokja.member.service;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetCodeRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetConfirmRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetVerifyRequestDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetCodeResponseDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetVerifyResponseDto;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.PasswordResetToken;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.member.repository.PasswordResetTokenRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 비밀번호 재설정 비즈니스 서비스. */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

  private static final long CODE_EXPIRES_IN_SECONDS = 180; // 3분
  private static final long RESET_TOKEN_EXPIRES_IN_SECONDS = 600; // 10분
  private static final int MAX_ATTEMPTS = 5;

  private final MemberRepository memberRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecureRandom random = new SecureRandom();

  /** 이메일 존재 여부 확인 후 6자리 인증번호를 생성하여 발송한다. */
  @Transactional
  public PasswordResetCodeResponseDto requestCode(PasswordResetCodeRequestDto request) {
    String email = request.getEmail();
    Member member =
        memberRepository
            .findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    passwordResetTokenRepository.deleteAllByEmail(email);

    String verificationCode = String.format("%06d", random.nextInt(1000000));
    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(CODE_EXPIRES_IN_SECONDS);

    PasswordResetToken token =
        PasswordResetToken.builder()
            .email(member.getEmail())
            .verificationCode(verificationCode)
            .expiresAt(expiresAt)
            .build();

    passwordResetTokenRepository.save(token);

    log.info("[PasswordReset] 이메일 {} 로 6자리 인증번호 [{}] 발송 완료", email, verificationCode);

    return PasswordResetCodeResponseDto.of("비밀번호 재설정 인증번호가 이메일로 발송되었습니다.", CODE_EXPIRES_IN_SECONDS);
  }

  /** 6자리 인증번호를 검증하고 일회성 resetToken을 발급한다. */
  @Transactional
  public PasswordResetVerifyResponseDto verifyCode(PasswordResetVerifyRequestDto request) {
    String email = request.getEmail();
    PasswordResetToken token =
        passwordResetTokenRepository
            .findTopByEmailOrderByCreatedAtDesc(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_CODE));

    if (token.isExpired()) {
      throw new BusinessException(ErrorCode.RESET_CODE_EXPIRED);
    }

    if (token.getAttemptCount() >= MAX_ATTEMPTS) {
      throw new BusinessException(ErrorCode.INVALID_RESET_CODE);
    }

    if (!token.getVerificationCode().equals(request.getCode())) {
      token.incrementAttemptCount();
      throw new BusinessException(ErrorCode.INVALID_RESET_CODE);
    }

    String resetToken = UUID.randomUUID().toString();
    LocalDateTime resetTokenExpiresAt =
        LocalDateTime.now().plusSeconds(RESET_TOKEN_EXPIRES_IN_SECONDS);
    token.markVerified(resetToken, resetTokenExpiresAt);

    return PasswordResetVerifyResponseDto.of(resetToken, RESET_TOKEN_EXPIRES_IN_SECONDS);
  }

  /** resetToken 검증 후 신규 비밀번호로 최종 변경한다. */
  @Transactional
  public void confirmReset(PasswordResetConfirmRequestDto request) {
    if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
      throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
    }

    PasswordResetToken token =
        passwordResetTokenRepository
            .findByResetToken(request.getResetToken())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_RESET_TOKEN));

    if (token.isExpired() || !token.isVerified() || token.isUsed()) {
      throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
    }

    Member member =
        memberRepository
            .findByEmail(token.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

    if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_REUSE_NOT_ALLOWED);
    }

    member.changePassword(passwordEncoder.encode(request.getNewPassword()));
    token.markUsed();

    log.info("[PasswordReset] 회원 {} 의 비밀번호가 성공적으로 재설정되었습니다.", member.getEmail());
  }
}
