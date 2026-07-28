package com.example.shinhangaecheokja.test.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 핑퐁 API 응답 DTO. */
@Getter
@AllArgsConstructor
public class PingResponse {

  private final String message;
  private final LocalDateTime timestamp;
}
