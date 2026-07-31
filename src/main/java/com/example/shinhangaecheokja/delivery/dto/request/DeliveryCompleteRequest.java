package com.example.shinhangaecheokja.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 완료 처리 요청 DTO. 증거 사진은 사전에 {@code POST /api/v1/uploads/image}로 업로드한 URL을 전달받는다. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryCompleteRequest {

  @NotBlank(message = "증거 사진 URL은 필수입니다.")
  private String proofPhotoUrl;
}
