package com.example.shinhangaecheokja.exception;

public class OverMaxDistanceException extends RuntimeException {

  public OverMaxDistanceException(double maxDistance) {
    super("유효하지 않은 최대 이동거리입니다: " + maxDistance);
  }
}
