package com.example.shinhangaecheokja.member.dto.response;

import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;

/** 회원 본인 프로필 조회 응답 DTO. 비밀번호는 노출하지 않는다. */
public record MemberProfileResponseDto(
    Long id, String email, String name, String phoneNumber, MemberRole role) {

  /** Member 엔티티를 MemberProfileResponseDto로 변환한다. */
  public static MemberProfileResponseDto from(Member entity) {
    return new MemberProfileResponseDto(
        entity.getId(),
        entity.getEmail(),
        entity.getName(),
        entity.getPhoneNumber(),
        entity.getRole());
  }
}
