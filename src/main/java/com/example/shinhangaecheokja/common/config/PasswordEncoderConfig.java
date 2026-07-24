package com.example.shinhangaecheokja.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** 비밀번호 암호화에 사용할 PasswordEncoder 빈을 등록한다. */
@Configuration
public class PasswordEncoderConfig {

  /** BCrypt 기반 PasswordEncoder를 제공한다. */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
