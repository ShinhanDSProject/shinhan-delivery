package com.example.shinhandelivery.address.dto.response;

import com.example.shinhandelivery.address.entity.Address;
import lombok.Builder;

/** 자주 쓰는 주소 응답 DTO (record 불변 객체). */
@Builder
public record AddressResponse(
    Long id,
    Long memberId,
    String alias,
    String address,
    String detailAddress,
    String pickupGuide) {

  /** Address 엔티티를 AddressResponse DTO로 변환한다. */
  public static AddressResponse from(Address entity) {
    return AddressResponse.builder()
        .id(entity.getId())
        .memberId(entity.getMemberId())
        .alias(entity.getAlias())
        .address(entity.getAddress())
        .detailAddress(entity.getDetailAddress())
        .pickupGuide(entity.getPickupGuide())
        .build();
  }
}
