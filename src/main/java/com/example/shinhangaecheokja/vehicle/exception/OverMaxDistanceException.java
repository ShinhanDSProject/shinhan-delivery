package com.example.shinhangaecheokja.exception;

/** 최대 이동거리(maxDistance)가 0 이하로 유효하지 않을 때 던진다. */
public class OverMaxDistanceException extends RuntimeException {

  public OverMaxDistanceException(double maxDistance) {
    super("유효하지 않은 최대 이동거리입니다: " + maxDistance);
  }
}
