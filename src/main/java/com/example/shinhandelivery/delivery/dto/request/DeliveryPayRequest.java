package com.example.shinhandelivery.delivery.dto.request;

import com.example.shinhandelivery.delivery.entity.ItemSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 결제와 배송 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryPayRequest {

  private Long categoryId;

  private Long totalFee;

  @Valid
  @NotNull(message = "출발지는 필수입니다.")
  private DeliveryPayLocationRequest pickup;

  @Valid
  @NotNull(message = "도착지는 필수입니다.")
  private DeliveryPayLocationRequest dropoff;

  @DecimalMin(value = "0.0", inclusive = false, message = "물품 무게는 0보다 커야 합니다.")
  private double weight;

  @NotNull(message = "물품 크기는 필수입니다.")
  private ItemSize itemSize;
}
