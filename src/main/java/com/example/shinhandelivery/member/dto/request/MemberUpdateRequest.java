package com.example.shinhandelivery.member.dto.request;

import com.example.shinhandelivery.member.constant.MemberValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 정보 수정 요청 DTO. 이메일/비밀번호/역할은 변경 대상이 아니다. */
@Getter
@Setter
@NoArgsConstructor
public class MemberUpdateRequest {

  @NotBlank(message = "이름은 필수 입력 값입니다.")
  @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하이어야 합니다.")
  private String name;

  @NotBlank(message = MemberValidationConstants.MSG_PHONE_EMPTY)
  @Pattern(
      regexp = MemberValidationConstants.PHONE_REGEX,
      message = MemberValidationConstants.MSG_PHONE_INVALID_FORMAT)
  private String phoneNumber;
}
