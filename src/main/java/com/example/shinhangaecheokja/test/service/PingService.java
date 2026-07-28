package com.example.shinhangaecheokja.test.service;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 핑퐁 헬스체크 비즈니스 로직 서비스. */
@Service
public class PingService {

  /** 핑퐁 응답 DTO를 생성하여 반환한다. */
  @Transactional(readOnly = true)
  public PingResponse getPing() {
    return new PingResponse("pong", LocalDateTime.now());
  }
}
