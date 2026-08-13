package com.example.shinhandelivery.delivery.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 픽업이 완료되지 않았거나 물품 확인 사진이 등록되지 않은 배송 요청의 사진을 조회할 때 던진다. */
public class PickupPhotoNotFoundException extends BusinessException {

  public PickupPhotoNotFoundException(Long deliveryRequestId) {
    super(ErrorCode.PICKUP_PHOTO_NOT_FOUND, "픽업 확인 사진을 찾을 수 없습니다: " + deliveryRequestId);
  }
}
