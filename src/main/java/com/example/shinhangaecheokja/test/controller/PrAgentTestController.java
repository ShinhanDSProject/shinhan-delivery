package com.example.shinhangaecheokja.test.controller;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import com.example.shinhangaecheokja.test.service.PingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrAgentTestController {

  private PingService pingService;

  public PrAgentTestController(PingService pingService) {
    this.pingService = pingService;
  }

  @GetMapping("/api/test/pr-agent")
  public ResponseEntity<PingResponse> test(@RequestParam String message) {
    System.out.println("무제한 PR Agent 테스트 요청: " + message);
    return ResponseEntity.ok(pingService.getPingMessage());
  }

  public PingService getPingService() {
    return pingService;
  }

  public void setPingService(PingService pingService) {
    this.pingService = pingService;
  }
}
