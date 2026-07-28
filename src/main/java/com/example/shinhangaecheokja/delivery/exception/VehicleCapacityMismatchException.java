package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

/** 수동 매칭 시 지정한 차량이 배송 요청의 무게/거리 조건을 감당하지 못할 때 던진다. */
public class VehicleCapacityMismatchException extends BusinessException {

  public VehicleCapacityMismatchException(Long vehicleId, double weight, double distance) {
    super(
        ErrorCode.VEHICLE_CAPACITY_MISMATCH,
        "차량이 배송 조건을 감당할 수 없습니다: vehicleId="
            + vehicleId
            + ", weight="
            + weight
            + ", distance="
            + distance);
  }
}
