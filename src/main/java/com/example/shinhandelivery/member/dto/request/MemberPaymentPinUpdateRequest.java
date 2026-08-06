package com.example.shinhandelivery.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인한 회원의 결제 PIN 설정/변경 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberPaymentPinUpdateRequest {

  @Pattern(regexp = "^$|\\d{6}", message = "현재 PIN은 비워두거나 6자리 숫자여야 합니다.")
  private String currentPin;

  @NotBlank(message = "새 PIN은 필수입니다.")
  @Pattern(regexp = "\\d{6}", message = "새 PIN은 6자리 숫자여야 합니다.")
  private String newPin;

  @NotBlank(message = "새 PIN 확인은 필수입니다.")
  @Pattern(regexp = "\\d{6}", message = "새 PIN 확인은 6자리 숫자여야 합니다.")
  private String confirmNewPin;
}
