package com.example.shinhangaecheokja.member.controller;

import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberUpdateRequest;
import com.example.shinhangaecheokja.member.dto.response.MemberResponse;
import com.example.shinhangaecheokja.member.service.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Member CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  /** 회원을 생성(가입)한다. */
  @PostMapping
  public ResponseEntity<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
    return ResponseEntity.ok(memberService.createMember(request));
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

  /** 회원을 삭제한다. */
  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
    memberService.deleteMember(memberId);
    return ResponseEntity.noContent().build();
  }
}
