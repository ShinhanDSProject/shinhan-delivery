package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;

/** 허용되지 않는 매칭 상태 전이를 시도할 때 던진다. */
public class InvalidMatchingTransitionException extends RuntimeException {

  public InvalidMatchingTransitionException(MatchingStatus from, MatchingStatus to) {
    super("허용되지 않는 매칭 상태 전이입니다: " + from + " -> " + to);
  }
}
