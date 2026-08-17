package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import lombok.Builder;

/** 과거 배송에서 신규 배송 초안에 다시 사용할 수 있는 입력값만 반환한다. */
@Builder
public record DeliveryReorderResponse(
    Long sourceDeliveryRequestId,
    String pickupAddress,
    double pickupLatitude,
    double pickupLongitude,
    String dropoffAddress,
    double dropoffLatitude,
    double dropoffLongitude,
    double weight,
    ItemSize itemSize) {

  /** 결제·상태·배송원·정산 정보를 제외하고 재배송 입력값만 변환한다. */
  public static DeliveryReorderResponse from(DeliveryRequest deliveryRequest) {
    return DeliveryReorderResponse.builder()
        .sourceDeliveryRequestId(deliveryRequest.getId())
        .pickupAddress(deliveryRequest.getPickupAddress())
        .pickupLatitude(deliveryRequest.getPickupLatitude())
        .pickupLongitude(deliveryRequest.getPickupLongitude())
        .dropoffAddress(deliveryRequest.getDropoffAddress())
        .dropoffLatitude(deliveryRequest.getDropoffLatitude())
        .dropoffLongitude(deliveryRequest.getDropoffLongitude())
        .weight(deliveryRequest.getWeight())
        .itemSize(deliveryRequest.getItemSize())
        .build();
  }
}
