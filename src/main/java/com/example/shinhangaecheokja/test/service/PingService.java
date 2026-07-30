package com.example.shinhangaecheokja.test.service;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PingService {

  public PingResponse getPingMessage() {
    return new PingResponse("pong", LocalDateTime.now());
  }
}
