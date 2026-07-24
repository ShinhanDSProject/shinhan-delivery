package com.example.shinhangaecheokja.controller;

import com.example.shinhangaecheokja.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.dto.request.MemberUpdateRequest;
import com.example.shinhangaecheokja.dto.response.MemberResponse;
import com.example.shinhangaecheokja.service.MemberService;
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

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @PostMapping
  public ResponseEntity<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
    return ResponseEntity.ok(memberService.createMember(request));
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {
    return ResponseEntity.ok(memberService.getMember(memberId));
  }

  @GetMapping
  public ResponseEntity<List<MemberResponse>> getMembers() {
    return ResponseEntity.ok(memberService.getMembers());
  }

  @PutMapping("/{memberId}")
  public ResponseEntity<MemberResponse> updateMember(
      @PathVariable Long memberId, @RequestBody MemberUpdateRequest request) {
    return ResponseEntity.ok(memberService.updateMember(memberId, request));
  }

  @DeleteMapping("/{memberId}")
  public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
    memberService.deleteMember(memberId);
    return ResponseEntity.noContent().build();
  }
}
