package com.example.shinhangaecheokja.member.controller;

import com.example.shinhangaecheokja.member.dto.request.PasswordResetCodeRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetConfirmRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetVerifyRequestDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetCodeResponseDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetVerifyResponseDto;
import com.example.shinhangaecheokja.member.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 비밀번호 재설정 REST Controller. */
@RestController
@RequestMapping("/api/v1/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

  private final PasswordResetService passwordResetService;

  /** 비밀번호 재설정 이메일 인증코드 발송 요청. */
  @PostMapping("/request-code")
  public ResponseEntity<PasswordResetCodeResponseDto> requestCode(
      @Valid @RequestBody PasswordResetCodeRequestDto request) {
    return ResponseEntity.ok(passwordResetService.requestCode(request));
  }

  /** 비밀번호 재설정 이메일 인증코드 검증 및 resetToken 발급. */
  @PostMapping("/verify-code")
  public ResponseEntity<PasswordResetVerifyResponseDto> verifyCode(
      @Valid @RequestBody PasswordResetVerifyRequestDto request) {
    return ResponseEntity.ok(passwordResetService.verifyCode(request));
  }

  /** resetToken 기반 신규 비밀번호 변경 확정. */
  @PostMapping("/confirm")
  public ResponseEntity<Void> confirmReset(
      @Valid @RequestBody PasswordResetConfirmRequestDto request) {
    passwordResetService.confirmReset(request);
    return ResponseEntity.noContent().build();
  }
}
