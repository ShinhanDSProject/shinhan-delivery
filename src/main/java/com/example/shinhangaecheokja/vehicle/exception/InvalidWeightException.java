package com.example.shinhangaecheokja.vehicle.exception;

/** 최대 무게(maxWeight)가 0 이하로 유효하지 않을 때 던진다. */
public class InvalidWeightException extends RuntimeException {

  public InvalidWeightException(double weight) {
    super("유효하지 않은 무게입니다: " + weight);
  }
}
