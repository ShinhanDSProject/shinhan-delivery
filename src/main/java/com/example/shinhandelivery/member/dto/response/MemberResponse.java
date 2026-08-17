package com.example.shinhandelivery.member.dto.response;

import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import lombok.Builder;

/** 회원 응답 DTO. 비밀번호는 노출하지 않는다. */
@Builder
public record MemberResponse(
    Long id, String email, String name, String phoneNumber, MemberRole role) {

  /** Member 엔티티를 응답 DTO로 변환한다. */
  public static MemberResponse from(Member entity) {
    return MemberResponse.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .name(entity.getName())
        .phoneNumber(entity.getPhoneNumber())
        .role(entity.getRole())
        .build();
  }
}
