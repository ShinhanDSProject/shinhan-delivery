package com.example.shinhangaecheokja.payment.dto.request;

import com.example.shinhangaecheokja.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 포인트 충전 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class PointChargeRequest {

  @Positive(message = "충전 금액은 0보다 커야 합니다.")
  private long amount;

  @NotNull(message = "결제 수단은 필수입니다.")
  private PaymentMethod paymentMethod;
}
