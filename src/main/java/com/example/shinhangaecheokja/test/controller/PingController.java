package com.example.shinhangaecheokja.test.controller;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import com.example.shinhangaecheokja.test.service.PingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 핑퐁 헬스체크 API 컨트롤러. */
@RestController
@RequestMapping("/api/test/ping")
@RequiredArgsConstructor
public class PingController {

  private final PingService pingService;

  /** 핑퐁 헬스체크 응답을 반환한다. */
  @GetMapping
  public ResponseEntity<PingResponse> ping() {
    return ResponseEntity.ok(pingService.getPing());
  }
}
