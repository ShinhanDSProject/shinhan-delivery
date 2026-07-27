package com.example.shinhangaecheokja.vehicle.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

public class VehicleNotFoundException extends BusinessException {

  public VehicleNotFoundException() {
    super(ErrorCode.VEHICLE_NOT_FOUND);
  }

  public VehicleNotFoundException(Long id) {
    super(ErrorCode.VEHICLE_NOT_FOUND, "존재하지 않는 차량입니다. (ID: " + id + ")");
  }

  public VehicleNotFoundException(String message) {
    super(ErrorCode.VEHICLE_NOT_FOUND, message);
  }
}
