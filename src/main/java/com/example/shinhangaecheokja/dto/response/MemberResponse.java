package com.example.shinhangaecheokja.dto.response;

import com.example.shinhangaecheokja.entity.Member;
import com.example.shinhangaecheokja.entity.MemberRole;

public record MemberResponse(Long id, String email, String name, String phoneNumber, MemberRole role) {

  public static MemberResponse from(Member entity) {
    return new MemberResponse(
        entity.getId(),
        entity.getEmail(),
        entity.getName(),
        entity.getPhoneNumber(),
        entity.getRole());
  }
}
