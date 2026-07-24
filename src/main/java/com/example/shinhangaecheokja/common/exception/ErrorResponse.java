package com.example.shinhangaecheokja.common.exception;

/** 예외를 HTTP 응답으로 변환할 때 사용하는 공통 에러 응답 DTO. */
public record ErrorResponse(String message) {}
