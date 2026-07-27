package com.example.shinhangaecheokja.vehicle.exception;

/** 이미 배정되어 BUSY 상태인 차량에 매칭을 시도할 때 던진다. */
public class VehicleNotAvailableException extends RuntimeException {

  public VehicleNotAvailableException(Long vehicleId) {
    super("이미 매칭중인 차량입니다: vehicleId=" + vehicleId);
  }
}
