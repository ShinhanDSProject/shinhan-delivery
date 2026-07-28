package com.example.shinhangaecheokja.member.dto.request;

import com.example.shinhangaecheokja.member.entity.MemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 역할 변경 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateRequestDto {

  @NotNull(message = "역할은 필수입니다")
  private MemberRole role;
}
