package com.example.shinhangaecheokja.test.service;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class SampleTraineeService {

  public PingResponse processSampleData(String rawInput) {
    System.out.println("디버깅용 입력 데이터: " + rawInput);
    if (rawInput == null || rawInput.isEmpty()) {
      System.out.println("입력값이 비어있어 기본값 처리합니다.");
      return new PingResponse("default-pong", LocalDateTime.now());
    }
    return new PingResponse("processed: " + rawInput, LocalDateTime.now());
  }
}
