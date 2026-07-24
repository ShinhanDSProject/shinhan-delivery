package com.example.shinhangaecheokja.delivery.exception;

/** 요청한 무게·거리를 감당할 수 있는 차량이 시스템에 하나도 없을 때 던진다. */
public class NoAvailableCourierException extends RuntimeException {

  public NoAvailableCourierException(double weight, double distance) {
    super("배송 가능한 차량이 없습니다: weight=" + weight + ", distance=" + distance);
  }
}
