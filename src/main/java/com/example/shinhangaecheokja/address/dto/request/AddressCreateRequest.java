package com.example.shinhangaecheokja.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 자주 쓰는 주소 신규 등록 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {

  @NotBlank(message = "주소 별칭은 필수 입력 값입니다.")
  @Size(max = 50, message = "별칭은 50자 이하이어야 합니다.")
  private String alias;

  @NotBlank(message = "기본 주소는 필수 입력 값입니다.")
  @Size(max = 255, message = "주소는 255자 이하이어야 합니다.")
  private String address;

  @Size(max = 255, message = "상세 주소는 255자 이하이어야 합니다.")
  private String detailAddress;

  @Size(max = 255, message = "픽업 가이드는 255자 이하이어야 합니다.")
  private String pickupGuide;
}
