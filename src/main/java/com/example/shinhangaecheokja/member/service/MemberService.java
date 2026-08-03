package com.example.shinhangaecheokja.member.service;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.member.dto.request.LoginRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberPasswordUpdateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberProfileUpdateRequestDto;
import com.example.shinhangaecheokja.member.dto.request.MemberUpdateRequest;
import com.example.shinhangaecheokja.member.dto.response.TokenResponse;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.exception.DuplicateMemberException;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Member 관련 유스케이스(가입/조회/수정/역할변경/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;

  /** 이메일 중복을 검증하고 비밀번호를 암호화해 회원을 생성한다 (Entity 리턴). */
  @Transactional
  public Member create(MemberCreateRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateMemberException(request.getEmail());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    return memberRepository.save(Member.from(request, encodedPassword));
  }

  /** 이메일과 비밀번호를 검증하여 JWT Access/Refresh 토큰을 발급한다. */
  @Transactional(readOnly = true)
  public TokenResponse login(LoginRequest request) {
    Member member =
        memberRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken =
        jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole().name());
    String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getEmail());

    return TokenResponse.of(accessToken, refreshToken);
  }

  /** id로 회원 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public Member getById(Long memberId) {
    return findMemberOrThrow(memberId);
  }

  /** 전체 회원 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<Member> list() {
    return memberRepository.findAll();
  }

  /** 로그인한 본인의 프로필 정보를 조회한다. */
  @Transactional(readOnly = true)
  public Member getMyProfile(Long memberId) {
    return findMemberOrThrow(memberId);
  }

  /** 로그인한 본인의 프로필 정보(이름, 연락처)를 수정한다. */
  @Transactional
  public Member updateMyProfile(Long memberId, MemberProfileUpdateRequestDto request) {
    return findMemberOrThrow(memberId).updateProfileBy(request);
  }

  /** 현재 비밀번호를 확인한 뒤 새 비밀번호를 BCrypt로 암호화해 저장한다. */
  @Transactional
  public void updatePassword(Long memberId, MemberPasswordUpdateRequest request) {
    Member member = findMemberOrThrow(memberId);

    if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
    }
    if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
    }
    if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_REUSE_NOT_ALLOWED);
    }

    member.changePassword(passwordEncoder.encode(request.getNewPassword()));
  }

  /** 회원의 이름·연락처를 수정한다 (Member Entity 리턴). 이메일/비밀번호/역할은 변경하지 않는다. */
  @Transactional
  public Member update(Long memberId, MemberUpdateRequest request) {
    return findMemberOrThrow(memberId).updateBy(request);
  }

  /** 회원의 역할(CUSTOMER / COURIER)을 변경한다 (Member Entity 리턴). */
  @Transactional
  public Member updateRole(Long memberId, MemberRole role) {
    return findMemberOrThrow(memberId).changeRole(role);
  }

  /** id로 회원을 조회해 삭제한다. 없으면 EntityNotFoundException. */
  @Transactional
  public void delete(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    memberRepository.delete(member);
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
  }
}
