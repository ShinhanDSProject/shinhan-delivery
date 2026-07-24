package com.example.shinhangaecheokja.service;

import com.example.shinhangaecheokja.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.dto.request.MemberUpdateRequest;
import com.example.shinhangaecheokja.dto.response.MemberResponse;
import com.example.shinhangaecheokja.entity.Member;
import com.example.shinhangaecheokja.exception.DuplicateMemberException;
import com.example.shinhangaecheokja.exception.MemberNotFoundException;
import com.example.shinhangaecheokja.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

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

  @Transactional(readOnly = true)
  public MemberResponse getMember(Long memberId) {
    return MemberResponse.from(findMemberOrThrow(memberId));
  }

  @Transactional(readOnly = true)
  public List<MemberResponse> getMembers() {
    return memberRepository.findAll().stream().map(MemberResponse::from).toList();
  }

  @Transactional
  public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
    Member member = findMemberOrThrow(memberId);
    member.setName(request.getName());
    member.setPhoneNumber(request.getPhoneNumber());
    return MemberResponse.from(member);
  }

  @Transactional
  public void deleteMember(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    memberRepository.delete(member);
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository.findById(memberId).orElseThrow(() -> new MemberNotFoundException(memberId));
  }
}
