package com.example.shinhangaecheokja.member.dto.response;

import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;

/** 회원 응답 DTO. 비밀번호는 노출하지 않는다. */
public record MemberResponse(
    Long id, String email, String name, String phoneNumber, MemberRole role) {

  /** Member 엔티티를 응답 DTO로 변환한다. */
  public static MemberResponse from(Member entity) {
    return new MemberResponse(
        entity.getId(),
        entity.getEmail(),
        entity.getName(),
        entity.getPhoneNumber(),
        entity.getRole());
  }
}
