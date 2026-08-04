package com.example.shinhandelivery.address.dto.response;

import com.example.shinhandelivery.address.entity.Address;

/** 자주 쓰는 주소 응답 DTO (record 불변 객체). */
public record AddressResponse(
    Long id,
    Long memberId,
    String alias,
    String address,
    String detailAddress,
    String pickupGuide) {

  /** Address 엔티티를 AddressResponse DTO로 변환한다. */
  public static AddressResponse from(Address entity) {
    return new AddressResponse(
        entity.getId(),
        entity.getMemberId(),
        entity.getAlias(),
        entity.getAddress(),
        entity.getDetailAddress(),
        entity.getPickupGuide());
  }
}
