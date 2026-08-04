package com.example.shinhandelivery.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 본인 프로필 정보 수정 요청 DTO. 이메일/비밀번호/역할은 변경 불가능하다. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileUpdateRequestDto {

  @NotBlank(message = "이름은 필수 입력 값입니다.")
  @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하이어야 합니다.")
  private String name;

  @NotBlank(message = "전화번호는 필수 입력 값입니다.")
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식(예: 010-1234-5678)이어야 합니다.")
  private String phoneNumber;
}
