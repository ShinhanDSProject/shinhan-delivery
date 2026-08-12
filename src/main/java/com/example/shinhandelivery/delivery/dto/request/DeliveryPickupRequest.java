package com.example.shinhandelivery.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 픽업 완료 처리 요청 DTO. 물품 확인 사진은 사전에 {@code POST /api/v1/uploads/image}로 업로드한 URL을 전달받는다. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryPickupRequest {

  @NotBlank(message = "물품 확인 사진 URL은 필수입니다.")
  @Size(max = 255, message = "물품 확인 사진 URL은 255자를 초과할 수 없습니다.")
  private String pickupPhotoUrl;
}
