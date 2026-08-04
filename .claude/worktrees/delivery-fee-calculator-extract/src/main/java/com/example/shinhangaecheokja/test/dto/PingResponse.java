package com.example.shinhandelivery.test.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PingResponse {

  private String message;
  private LocalDateTime timestamp;
}
