package com.example.shinhangaecheokja.exception;

/** 이미 가입된 이메일로 회원 가입을 시도할 때 던진다. */
public class DuplicateMemberException extends RuntimeException {

  public DuplicateMemberException(String email) {
    super("이미 가입된 이메일입니다: " + email);
  }
}
