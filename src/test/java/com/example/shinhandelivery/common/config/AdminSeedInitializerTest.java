package com.example.shinhandelivery.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminSeedInitializerTest {

  @Mock private MemberRepository memberRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("설정한 이메일이 없으면 비밀번호를 암호화해 관리자 계정을 생성한다")
  void createAdminWhenEmailDoesNotExist() {
    when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("secure-password")).thenReturn("encoded-password");
    AdminSeedInitializer initializer = createInitializer("admin@example.com", "secure-password");

    initializer.run();

    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
    verify(memberRepository).save(captor.capture());
    assertThat(captor.getValue().getEmail()).isEqualTo("admin@example.com");
    assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
    assertThat(captor.getValue().getRole()).isEqualTo(MemberRole.ADMIN);
  }

  @Test
  @DisplayName("이미 관리자 계정이 존재하면 다시 저장하지 않는다")
  void skipWhenAdminAlreadyExists() {
    Member admin =
        Member.createAdmin("admin@example.com", "encoded-password", "관리자", "010-0000-0000");
    when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
    AdminSeedInitializer initializer = createInitializer("admin@example.com", "secure-password");

    initializer.run();

    verify(memberRepository, never()).save(any(Member.class));
    verify(passwordEncoder, never()).encode(any());
  }

  @Test
  @DisplayName("동일한 이메일의 일반 회원이 존재하면 관리자 권한으로 승격하지 않는다")
  void rejectWhenNonAdminEmailAlreadyExists() {
    Member customer =
        Member.builder()
            .email("admin@example.com")
            .password("encoded-password")
            .name("일반 회원")
            .phoneNumber("010-1111-1111")
            .role(MemberRole.CUSTOMER)
            .build();
    when(memberRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(customer));
    AdminSeedInitializer initializer = createInitializer("admin@example.com", "secure-password");

    assertThatThrownBy(initializer::run)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("일반 회원");

    verify(memberRepository, never()).save(any(Member.class));
  }

  @Test
  @DisplayName("관리자 시드 비밀번호가 비어 있으면 애플리케이션 시작을 중단한다")
  void rejectBlankPassword() {
    AdminSeedInitializer initializer = createInitializer("admin@example.com", " ");

    assertThatThrownBy(initializer::run)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ADMIN_PASSWORD");

    verify(memberRepository, never()).findByEmail(any());
  }

  private AdminSeedInitializer createInitializer(String email, String password) {
    return new AdminSeedInitializer(
        memberRepository, passwordEncoder, email, password, "로컬 관리자", "010-0000-0000");
  }
}
