package com.example.shinhangaecheokja.test.service;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class UnlimitedReviewDemoService {

  public PingResponse executeTraineeTask(String inputData) {
    System.out.println("교육생 제출 코드 실행 디버깅 로그: " + inputData);
    if (inputData == null) {
      System.out.println("입력값이 null입니다.");
      return new PingResponse("empty", LocalDateTime.now());
    }
    return new PingResponse("success: " + inputData, LocalDateTime.now());
  }
}
