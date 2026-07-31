package com.example.shinhangaecheokja.ai.controller;

import com.example.shinhangaecheokja.ai.dto.DeliveryFaqRequest;
import com.example.shinhangaecheokja.ai.dto.DeliveryFaqResponse;
import com.example.shinhangaecheokja.ai.service.DeliveryFaqAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LangChain4j AI RAG API", description = "LangChain4j 기반 배송 가이드 & Q&A AI 서비스")
@RestController
@RequestMapping("/api/ai/faq")
@RequiredArgsConstructor
public class DeliveryFaqAiController {

  private final DeliveryFaqAiService deliveryFaqAiService;

  @Operation(
      summary = "LangChain4j RAG 배송 FAQ 질의 API",
      description = "사내 배송 규정 문서를 RAG VectorStore로 조회하여 AI가 최적의 답변을 생성합니다.")
  @PostMapping
  public ResponseEntity<DeliveryFaqResponse> askDeliveryFaq(
      @Valid @RequestBody DeliveryFaqRequest request) {
    DeliveryFaqResponse response = deliveryFaqAiService.askFaq(request);
    return ResponseEntity.ok(response);
  }
}
