package com.example.shinhandelivery.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 필드 실시간 검증 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberFieldValidateRequest {

  @NotBlank(message = "검증 대상 필드명은 필수입니다.")
  private String field;

  private String value;
}
