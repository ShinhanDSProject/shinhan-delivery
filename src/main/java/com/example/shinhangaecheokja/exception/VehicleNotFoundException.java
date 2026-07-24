package com.example.shinhangaecheokja.exception;

public class VehicleNotFoundException extends RuntimeException {

  public VehicleNotFoundException(Long vehicleId) {
    super("존재하지 않는 운송수단입니다: " + vehicleId);
  }
}
