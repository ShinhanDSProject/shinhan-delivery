package com.example.shinhandelivery.member.dto.request;

import com.example.shinhandelivery.member.entity.MemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 역할(CUSTOMER/COURIER) 변경 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MemberRoleUpdateRequest {

  @NotNull(message = "역할은 필수 선택 항목입니다.")
  private MemberRole role;
}
