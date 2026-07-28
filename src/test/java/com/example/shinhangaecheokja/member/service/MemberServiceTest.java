package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.response.MemberResponse;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.exception.DuplicateMemberException;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private MemberService memberService;

  @Test
  void 이메일이_중복되지_않으면_회원을_생성한다() {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("user@example.com");
    request.setPassword("password123");
    request.setName("홍길동");
    request.setPhoneNumber("010-1234-5678");
    request.setRole(MemberRole.CUSTOMER);

    when(memberRepository.existsByEmail("user@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
    when(memberRepository.save(any(Member.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    MemberResponse response = memberService.createMember(request);

    assertThat(response.email()).isEqualTo("user@example.com");
    assertThat(response.role()).isEqualTo(MemberRole.CUSTOMER);
  }

  @Test
  void 이메일이_중복되면_DuplicateMemberException을_던진다() {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("dup@example.com");
    when(memberRepository.existsByEmail("dup@example.com")).thenReturn(true);

    assertThatThrownBy(() -> memberService.createMember(request))
        .isInstanceOf(DuplicateMemberException.class);
  }

  @Test
  void 존재하지_않는_회원을_조회하면_EntityNotFoundException을_던진다() {
    when(memberRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> memberService.getMember(1L))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 존재하는_회원을_조회하면_MemberResponse를_반환한다() {
    Member member = new Member();
    member.setEmail("user@example.com");
    member.setName("홍길동");
    member.setPhoneNumber("010-1234-5678");
    member.setRole(MemberRole.COURIER);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    MemberResponse response = memberService.getMember(1L);

    assertThat(response.email()).isEqualTo("user@example.com");
  }

  @Test
  void 역할_변경_시_성공적으로_역할이_업데이트된다() {
    Member member = new Member();
    member.setEmail("user@example.com");
    member.setName("홍길동");
    member.setPhoneNumber("010-1234-5678");
    member.setRole(MemberRole.CUSTOMER);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    com.example.shinhangaecheokja.member.dto.request.RoleUpdateRequestDto request =
        new com.example.shinhangaecheokja.member.dto.request.RoleUpdateRequestDto(
            MemberRole.COURIER);

    MemberResponse response = memberService.updateRole(1L, request);

    assertThat(response.role()).isEqualTo(MemberRole.COURIER);
    assertThat(member.getRole()).isEqualTo(MemberRole.COURIER);
  }

  @Test
  void 존재하지_않는_회원의_역할을_변경하려_하면_EntityNotFoundException을_던진다() {
    when(memberRepository.findById(999L)).thenReturn(Optional.empty());

    com.example.shinhangaecheokja.member.dto.request.RoleUpdateRequestDto request =
        new com.example.shinhangaecheokja.member.dto.request.RoleUpdateRequestDto(
            MemberRole.COURIER);

    assertThatThrownBy(() -> memberService.updateRole(999L, request))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }
}
