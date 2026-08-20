package com.example.shinhandelivery.member.dto.request;

import com.example.shinhandelivery.member.entity.MemberValidationField;
import jakarta.validation.constraints.NotNull;
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

  @NotNull(message = "검증 대상 필드명은 필수입니다.")
  private MemberValidationField field;

  private String value;
}
