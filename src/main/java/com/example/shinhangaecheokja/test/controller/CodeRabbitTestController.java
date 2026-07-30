package com.example.shinhangaecheokja.test.controller;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import com.example.shinhangaecheokja.test.service.PingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeRabbitTestController {

  private PingService pingService;

  public CodeRabbitTestController(PingService pingService) {
    this.pingService = pingService;
  }

  @GetMapping("/api/test/coderabbit")
  public ResponseEntity<PingResponse> test(@RequestParam String message) {
    System.out.println("요청 메시지: " + message);
    return ResponseEntity.ok(pingService.getPingMessage());
  }

  public PingService getPingService() {
    return pingService;
  }

  public void setPingService(PingService pingService) {
    this.pingService = pingService;
  }
}
