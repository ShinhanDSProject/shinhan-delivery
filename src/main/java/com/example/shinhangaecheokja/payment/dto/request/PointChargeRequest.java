package com.example.shinhangaecheokja.payment.dto.request;

import com.example.shinhangaecheokja.payment.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 포인트 충전 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class PointChargeRequest {

  @Min(value = 1, message = "충전 금액은 1 이상이어야 합니다.")
  private long amount;

  @NotNull(message = "결제 수단은 필수입니다.")
  private PaymentMethod paymentMethod;
}
