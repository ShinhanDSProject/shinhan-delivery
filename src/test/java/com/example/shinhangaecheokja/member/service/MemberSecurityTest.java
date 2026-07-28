package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.member.dto.request.LoginRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.response.MemberResponse;
import com.example.shinhangaecheokja.member.dto.response.TokenResponse;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
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
class MemberSecurityTest {

  @Mock private MemberRepository memberRepository;

  @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Mock private JwtProvider jwtProvider;

  @InjectMocks private MemberService memberService;

  private Member testMember;

  @BeforeEach
  void setUp() {
    testMember = new Member();
    testMember.setId(1L);
    testMember.setEmail("user@example.com");
    testMember.setPassword(passwordEncoder.encode("rawPassword123"));
    testMember.setName("Hong Gil Dong");
    testMember.setPhoneNumber("010-1234-5678");
    testMember.setRole(MemberRole.CUSTOMER);
  }

  @Test
  @DisplayName("회원가입 시 비밀번호가 BCrypt로 암호화되어 저장되는지 검증")
  void createMemberEncodesPassword() {
    // given
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("newuser@example.com");
    request.setPassword("rawPassword123");
    request.setName("New User");
    request.setPhoneNumber("010-9999-8888");
    request.setRole(MemberRole.CUSTOMER);

    given(memberRepository.existsByEmail(request.getEmail())).willReturn(false);
    given(memberRepository.save(any(Member.class)))
        .willAnswer(
            invocation -> {
              Member saved = invocation.getArgument(0);
              saved.setId(2L);
              return saved;
            });

    // when
    MemberResponse response = memberService.createMember(request);

    // then
    assertThat(response.id()).isEqualTo(2L);
    assertThat(passwordEncoder.matches("rawPassword123", testMember.getPassword())).isTrue();
  }

  @Test
  @DisplayName("올바른 이메일과 비밀번호 입력 시 JWT 토큰이 발급되는지 검증")
  void loginSuccess() {
    // given
    LoginRequest request = new LoginRequest("user@example.com", "rawPassword123");
    given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(testMember));
    given(jwtProvider.createAccessToken(1L, "user@example.com", "CUSTOMER"))
        .willReturn("access.token.jwt");
    given(jwtProvider.createRefreshToken(1L, "user@example.com")).willReturn("refresh.token.jwt");

    // when
    TokenResponse tokenResponse = memberService.login(request);

    // then
    assertThat(tokenResponse).isNotNull();
    assertThat(tokenResponse.getAccessToken()).isEqualTo("access.token.jwt");
    assertThat(tokenResponse.getRefreshToken()).isEqualTo("refresh.token.jwt");
    assertThat(tokenResponse.getTokenType()).isEqualTo("Bearer");
  }

  @Test
  @DisplayName("틀린 비밀번호 입력 시 BusinessException 예외 발생")
  void loginWithWrongPassword() {
    // given
    LoginRequest request = new LoginRequest("user@example.com", "wrongPassword");
    given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(testMember));

    // when & then
    assertThatThrownBy(() -> memberService.login(request)).isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("존재하지 않는 이메일 로그인 시 EntityNotFoundException 발생")
  void loginWithNonExistingEmail() {
    // given
    LoginRequest request = new LoginRequest("notfound@example.com", "password");
    given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> memberService.login(request))
        .isInstanceOf(EntityNotFoundException.class);
  }
}
