package com.example.shinhangaecheokja.delivery.exception;

/** 수동 매칭 시 지정한 차량이 배송 요청의 무게/거리 조건을 감당하지 못할 때 던진다. */
public class VehicleCapacityMismatchException extends RuntimeException {

  public VehicleCapacityMismatchException(Long vehicleId, double weight, double distance) {
    super(
        "차량이 배송 조건을 감당할 수 없습니다: vehicleId="
            + vehicleId
            + ", weight="
            + weight
            + ", distance="
            + distance);
  }
}
