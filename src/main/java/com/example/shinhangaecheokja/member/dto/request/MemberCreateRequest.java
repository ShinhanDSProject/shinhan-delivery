package com.example.shinhangaecheokja.member.dto.request;

import com.example.shinhangaecheokja.member.entity.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 가입 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MemberCreateRequest {

  private String email;
  private String password;
  private String name;
  private String phoneNumber;
  private MemberRole role;
}
