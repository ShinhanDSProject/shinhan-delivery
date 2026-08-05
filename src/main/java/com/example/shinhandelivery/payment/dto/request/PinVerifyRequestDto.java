package com.example.shinhandelivery.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/** 결제 PIN 검증 요청 DTO. */
@Getter
@Setter
public class PinVerifyRequestDto {

  @NotBlank(message = "pin은 필수입니다.")
  @Pattern(regexp = "\\d{6}", message = "pin은 6자리 숫자여야 합니다.")
  private String pin;
}
