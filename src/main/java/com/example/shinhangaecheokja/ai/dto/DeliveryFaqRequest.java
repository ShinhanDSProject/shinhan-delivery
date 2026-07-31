package com.example.shinhangaecheokja.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFaqRequest {

  @NotBlank(message = "질문 내용을 입력해 주세요")
  private String question;
}
