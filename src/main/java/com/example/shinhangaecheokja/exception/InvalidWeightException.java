package com.example.shinhangaecheokja.exception;

public class InvalidWeightException extends RuntimeException {

  public InvalidWeightException(double weight) {
    super("유효하지 않은 무게입니다: " + weight);
  }
}
