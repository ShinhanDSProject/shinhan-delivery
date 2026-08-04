package com.example.shinhangaecheokja.member.repository;

import com.example.shinhangaecheokja.member.entity.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** 비밀번호 재설정 토큰 Repository. */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);

  Optional<PasswordResetToken> findByResetToken(String resetToken);

  void deleteAllByEmail(String email);
}
