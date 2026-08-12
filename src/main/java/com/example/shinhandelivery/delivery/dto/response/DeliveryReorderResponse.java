package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.ItemSize;

/** 과거 배송에서 신규 배송 초안에 다시 사용할 수 있는 입력값만 반환한다. */
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
    return new DeliveryReorderResponse(
        deliveryRequest.getId(),
        deliveryRequest.getPickupAddress(),
        deliveryRequest.getPickupLatitude(),
        deliveryRequest.getPickupLongitude(),
        deliveryRequest.getDropoffAddress(),
        deliveryRequest.getDropoffLatitude(),
        deliveryRequest.getDropoffLongitude(),
        deliveryRequest.getWeight(),
        deliveryRequest.getItemSize());
  }
}
