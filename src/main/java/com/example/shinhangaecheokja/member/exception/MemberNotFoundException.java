package com.example.shinhangaecheokja.member.exception;

/** 주어진 id에 해당하는 Member가 존재하지 않을 때 던진다. */
public class MemberNotFoundException extends RuntimeException {

  public MemberNotFoundException(Long memberId) {
    super("존재하지 않는 회원입니다: " + memberId);
  }
}
