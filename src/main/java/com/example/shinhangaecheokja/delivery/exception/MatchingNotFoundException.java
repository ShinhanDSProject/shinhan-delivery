package com.example.shinhangaecheokja.delivery.exception;

/** 주어진 id에 해당하는 Matching이 존재하지 않을 때 던진다. */
public class MatchingNotFoundException extends RuntimeException {

  public MatchingNotFoundException(Long matchingId) {
    super("존재하지 않는 매칭입니다: " + matchingId);
  }
}
