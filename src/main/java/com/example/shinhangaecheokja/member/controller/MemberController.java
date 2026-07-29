package com.example.shinhangaecheokja.member.controller;

import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.member.dto.request.LoginRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberProfileUpdateRequestDto;
import com.example.shinhangaecheokja.member.dto.request.MemberRoleUpdateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberUpdateRequest;
import com.example.shinhangaecheokja.member.dto.response.MemberProfileResponseDto;
import com.example.shinhangaecheokja.member.dto.response.MemberResponse;
import com.example.shinhangaecheokja.member.dto.response.TokenResponse;
import com.example.shinhangaecheokja.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Member CRUD 및 역할 변경 API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  /** 회원을 생성(가입)한다. */
  @PostMapping
  public ResponseEntity<MemberResponse> createMember(
      @Valid @RequestBody MemberCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(memberService.createMember(request));
  }

  /** 회원 로그인(JWT 토큰 발급)을 처리한다. */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(memberService.login(request));
  }

  /** 인증된 본인의 프로필 정보를 조회한다. */
  @GetMapping({"/me", "/api/members/me"})
  public ResponseEntity<MemberProfileResponseDto> getMyProfile(
      org.springframework.security.core.Authentication authentication) {
    Long memberId = extractMemberId(authentication);
    return ResponseEntity.ok(memberService.getMyProfile(memberId));
  }

  /** 인증된 본인의 프로필 정보(이름, 연락처)를 수정한다. */
  @PatchMapping({"/me", "/api/members/me"})
  public ResponseEntity<MemberProfileResponseDto> updateMyProfile(
      org.springframework.security.core.Authentication authentication,
      @Valid @RequestBody MemberProfileUpdateRequestDto request) {
    Long memberId = extractMemberId(authentication);
    return ResponseEntity.ok(memberService.updateMyProfile(memberId, request));
  }

  private Long extractMemberId(org.springframework.security.core.Authentication authentication) {
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails
        && userDetails.getId() != null) {
      return userDetails.getId();
    }
    throw new com.example.shinhangaecheokja.common.exception.BusinessException(
        com.example.shinhangaecheokja.common.exception.ErrorCode.UNAUTHORIZED);
  }

  /** 회원 단건을 조회한다. */
  @GetMapping("/{memberId}")
  public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
    return ResponseEntity.ok(memberService.getMember(memberId));
  }

  /** 회원 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<MemberResponse>> getMembers() {
    return ResponseEntity.ok(memberService.getMembers());
  }

  /** 회원 정보를 수정한다. */
  @PutMapping("/{memberId}")
  public ResponseEntity<MemberResponse> updateMember(
      @PathVariable Long memberId, @RequestBody MemberUpdateRequest request) {
    return ResponseEntity.ok(memberService.updateMember(memberId, request));
  }

  /** 회원의 역할(CUSTOMER/COURIER)을 변경한다. */
  @PatchMapping("/{memberId}/role")
  public ResponseEntity<MemberResponse> updateRole(
      @PathVariable Long memberId, @Valid @RequestBody MemberRoleUpdateRequest request) {
    return ResponseEntity.ok(memberService.updateRole(memberId, request.getRole()));
  }

  /** 회원을 삭제한다. */
  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
    memberService.deleteMember(memberId);
    return ResponseEntity.noContent().build();
  }
}
