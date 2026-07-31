package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberProfileUpdateRequestDto;
import com.example.shinhangaecheokja.member.dto.response.MemberProfileResponseDto;
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

    Member response = memberService.createMember(request);

    assertThat(response.getEmail()).isEqualTo("user@example.com");
    assertThat(response.getRole()).isEqualTo(MemberRole.CUSTOMER);
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
        .isInstanceOf(EntityNotFoundException.class);
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
  void 로그인한_본인의_프로필을_조회한다() {
    Member member = new Member();
    member.setEmail("my@example.com");
    member.setName("김철수");
    member.setPhoneNumber("010-9876-5432");
    member.setRole(MemberRole.CUSTOMER);
    when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

    MemberProfileResponseDto response = memberService.getMyProfile(2L);

    assertThat(response.email()).isEqualTo("my@example.com");
    assertThat(response.name()).isEqualTo("김철수");
    assertThat(response.phoneNumber()).isEqualTo("010-9876-5432");
  }

  @Test
  void 로그인한_본인의_프로필_이름과_연락처를_수정한다() {
    Member member = new Member();
    member.setEmail("my@example.com");
    member.setName("김철수");
    member.setPhoneNumber("010-9876-5432");
    member.setRole(MemberRole.CUSTOMER);
    when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

    MemberProfileUpdateRequestDto request =
        new MemberProfileUpdateRequestDto("김영희", "010-1111-2222");

    MemberProfileResponseDto response = memberService.updateMyProfile(2L, request);

    assertThat(response.name()).isEqualTo("김영희");
    assertThat(response.phoneNumber()).isEqualTo("010-1111-2222");
  }
}
