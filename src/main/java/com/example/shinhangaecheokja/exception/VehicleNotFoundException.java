package com.example.shinhangaecheokja.exception;

/** 주어진 id에 해당하는 Vehicle이 존재하지 않을 때 던진다. */
public class VehicleNotFoundException extends RuntimeException {

  public VehicleNotFoundException(Long vehicleId) {
    super("존재하지 않는 운송수단입니다: " + vehicleId);
  }
}
