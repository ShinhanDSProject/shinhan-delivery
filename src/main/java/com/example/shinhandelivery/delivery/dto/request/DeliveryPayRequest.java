package com.example.shinhandelivery.delivery.dto.request;

import com.example.shinhandelivery.delivery.entity.DeliveryInstructionType;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

  private DeliveryInstructionType deliveryInstructionType;

  @Size(max = 100, message = "공동현관 출입번호는 100자 이하여야 합니다.")
  private String entranceCode;

  @Size(max = 100, message = "동/호수 상세는 100자 이하여야 합니다.")
  private String unitDetail;

  @Size(max = 500, message = "전달 요청사항은 500자 이하여야 합니다.")
  private String deliveryNote;

  @Size(max = 255, message = "참고 사진 URL은 255자 이하여야 합니다.")
  private String deliveryReferencePhotoUrl;
}
