package com.example.shinhangaecheokja.member.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberUpdateRequest;
import com.example.shinhangaecheokja.member.dto.response.MemberResponse;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.exception.DuplicateMemberException;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Member 관련 유스케이스(가입/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  /** 이메일 중복을 검증하고 비밀번호를 암호화해 회원을 생성한다. */
  @Transactional
  public MemberResponse createMember(MemberCreateRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateMemberException(request.getEmail());
    }

    Member member = new Member();
    member.setEmail(request.getEmail());
    member.setPassword(passwordEncoder.encode(request.getPassword()));
    member.setName(request.getName());
    member.setPhoneNumber(request.getPhoneNumber());
    member.setRole(request.getRole());

    return MemberResponse.from(memberRepository.save(member));
  }

  /** id로 회원 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public MemberResponse getMember(Long memberId) {
    return MemberResponse.from(findMemberOrThrow(memberId));
  }

  /** 전체 회원 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<MemberResponse> getMembers() {
    return memberRepository.findAll().stream().map(MemberResponse::from).toList();
  }

  /** 회원의 이름·연락처를 수정한다. 이메일/비밀번호/역할은 변경하지 않는다. */
  @Transactional
  public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
    Member member = findMemberOrThrow(memberId);
    member.setName(request.getName());
    member.setPhoneNumber(request.getPhoneNumber());
    return MemberResponse.from(member);
  }

  /** id로 회원을 조회해 삭제한다. 없으면 EntityNotFoundException. */
  @Transactional
  public void deleteMember(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    memberRepository.delete(member);
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
  }
}
