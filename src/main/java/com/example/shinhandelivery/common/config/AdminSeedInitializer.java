package com.example.shinhandelivery.common.config;

import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 환경변수로 명시적으로 활성화한 로컬 환경에서만 관리자 계정을 멱등 생성합니다. */
@Component
@ConditionalOnProperty(name = "app.admin-seed.enabled", havingValue = "true")
public class AdminSeedInitializer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminSeedInitializer.class);

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final String email;
  private final String password;
  private final String name;
  private final String phoneNumber;

  public AdminSeedInitializer(
      MemberRepository memberRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.admin-seed.email}") String email,
      @Value("${app.admin-seed.password}") String password,
      @Value("${app.admin-seed.name}") String name,
      @Value("${app.admin-seed.phone-number}") String phoneNumber) {
    this.memberRepository = memberRepository;
    this.passwordEncoder = passwordEncoder;
    this.email = email;
    this.password = password;
    this.name = name;
    this.phoneNumber = phoneNumber;
  }

  /** 설정된 관리자 계정이 없을 때만 암호화하여 생성합니다. */
  @Override
  @Transactional
  public void run(String... args) {
    validateConfiguration();
    memberRepository
        .findByEmail(email.trim())
        .ifPresentOrElse(this::validateExistingAdmin, this::createAdmin);
  }

  private void validateConfiguration() {
    if (email == null || email.isBlank() || password == null || password.length() < 12) {
      throw new IllegalStateException(
          "관리자 시드를 활성화하려면 ADMIN_EMAIL과 12자 이상의 ADMIN_PASSWORD를 설정해야 합니다.");
    }
  }

  private void validateExistingAdmin(Member member) {
    if (member.getRole() != MemberRole.ADMIN) {
      throw new IllegalStateException("동일한 이메일의 일반 회원이 있어 관리자 계정을 생성할 수 없습니다.");
    }
    log.info("Local admin account already exists. Skipping admin seeding.");
  }

  private void createAdmin() {
    memberRepository.save(
        Member.createAdmin(email.trim(), passwordEncoder.encode(password), name, phoneNumber));
    log.info("Local admin account created successfully.");
  }
}
