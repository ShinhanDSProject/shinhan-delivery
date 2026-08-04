package com.example.shinhangaecheokja.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 비밀번호 재설정 토큰/인증코드 관리 엔티티. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(name = "verification_code", nullable = false, length = 6)
  private String verificationCode;

  @Column(name = "reset_token", length = 36)
  private String resetToken;

  @Builder.Default
  @Column(name = "attempt_count", nullable = false)
  private int attemptCount = 0;

  @Builder.Default
  @Column(name = "is_verified", nullable = false)
  private boolean isVerified = false;

  @Builder.Default
  @Column(name = "is_used", nullable = false)
  private boolean isUsed = false;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  public void incrementAttemptCount() {
    this.attemptCount++;
    this.updatedAt = LocalDateTime.now();
  }

  public void markVerified(String generatedResetToken, LocalDateTime resetTokenExpiresAt) {
    this.isVerified = true;
    this.resetToken = generatedResetToken;
    this.expiresAt = resetTokenExpiresAt;
    this.updatedAt = LocalDateTime.now();
  }

  public void markUsed() {
    this.isUsed = true;
    this.updatedAt = LocalDateTime.now();
  }
}
