package com.example.shinhangaecheokja.test.controller;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import com.example.shinhangaecheokja.test.service.PingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PingController {

  private final PingService pingService;

  @GetMapping("/api/test/ping")
  public ResponseEntity<PingResponse> getPing() {
    return ResponseEntity.ok(pingService.getPingMessage());
  }
}
