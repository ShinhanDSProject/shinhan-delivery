package com.example.shinhandelivery.vehicle.exception;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;

/** 이미 배정되어 BUSY 상태인 차량에 매칭을 시도할 때 던진다. */
public class VehicleNotAvailableException extends BusinessException {

  public VehicleNotAvailableException(Long vehicleId) {
    super(ErrorCode.VEHICLE_NOT_AVAILABLE, "이미 매칭중인 차량입니다: vehicleId=" + vehicleId);
  }
}
