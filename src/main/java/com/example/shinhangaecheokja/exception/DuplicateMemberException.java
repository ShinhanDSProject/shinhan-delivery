package com.example.shinhangaecheokja.exception;

public class DuplicateMemberException extends RuntimeException {

  public DuplicateMemberException(String email) {
    super("이미 가입된 이메일입니다: " + email);
  }
}
