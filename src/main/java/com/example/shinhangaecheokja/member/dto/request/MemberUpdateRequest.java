package com.example.shinhangaecheokja.member.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 정보 수정 요청 DTO. 이메일/비밀번호/역할은 변경 대상이 아니다. */
@Getter
@Setter
@NoArgsConstructor
public class MemberUpdateRequest {

  private String name;
  private String phoneNumber;
}
