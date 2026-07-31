package com.example.shinhangaecheokja.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFaqResponse {

  private String question;
  private String answer;
  private String source;
}
