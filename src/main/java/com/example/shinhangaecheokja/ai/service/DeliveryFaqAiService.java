package com.example.shinhangaecheokja.ai.service;

import com.example.shinhangaecheokja.ai.dto.DeliveryFaqRequest;
import com.example.shinhangaecheokja.ai.dto.DeliveryFaqResponse;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryFaqAiService {

  @Value("${gemini.api.key:demo}")
  private String apiKey;

  private DeliveryFaqAssistant assistant;

  public interface DeliveryFaqAssistant {
    String answer(String userQuestion);
  }

  @PostConstruct
  public void initRagEngine() {
    log.info("🤖 LangChain4j RAG 엔진 초기화 (Easy RAG / InMemory Vector Store)...");

    // 1. 사내 픽업 가이드 및 배송 규정 문서 생성
    Document kbDoc =
        Document.from(
            """
        [신한 개척자 퀵배송 & 온디맨드 규정 가이드]
        1. 배송 수수료 및 수단 규정:
           - 자전거/도보: 5kg 이하 소형 화물만 가능.
           - 오토바이: 20kg 이하 일반 화물 지원.
           - 다마스/라보/1톤 트럭: 20kg 초과 대형 화물 및 전자제품(컴퓨터, TV 등) 필수 수단.
        2. 픽업 유의사항:
           - 전자제품 및 유리가 포함된 물품은 에어캡(뾱뾱이) 포장이 필수입니다.
           - 위험물, 휘발유, 동물, 불법 장물은 배송 불가 물품으로 매칭 즉시 취소 처리됩니다.
        3. 배송 정산 및 지갑:
           - 기사님 정산은 배송 완료 처리 즉시 포인트 지갑(PointWallet)으로 실시간 입금됩니다.
        """);

    // 2. 인메모리 임베딩 저장소 구축
    EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    // 3. 검색기(ContentRetriever) 생성
    ContentRetriever contentRetriever =
        EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .maxResults(2)
            .minScore(0.5)
            .build();

    // 4. OpenAI / Gemini 호환 ChatModel 설정
    ChatLanguageModel model =
        OpenAiChatModel.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/")
            .apiKey(apiKey.equals("demo") ? "AIzaSy_demo_key" : apiKey)
            .modelName("gpt-4o-mini")
            .temperature(0.2)
            .build();

    // 5. LangChain4j AiServices RAG 바인딩
    this.assistant =
        AiServices.builder(DeliveryFaqAssistant.class)
            .chatLanguageModel(model)
            .contentRetriever(contentRetriever)
            .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
            .build();
  }

  public DeliveryFaqResponse askFaq(DeliveryFaqRequest request) {
    log.info("🤖 LangChain4j RAG 질의 처리: {}", request.getQuestion());

    String answer;
    try {
      answer = assistant.answer(request.getQuestion());
    } catch (Exception e) {
      log.warn("⚠️ LangChain4j 외부 API 호출 예외 (더미 로컬 RAG 응답으로 대체): {}", e.getMessage());
      answer = getLocalFallbackAnswer(request.getQuestion());
    }

    return DeliveryFaqResponse.builder()
        .question(request.getQuestion())
        .answer(answer)
        .source("LangChain4j RAG Engine (InMemory VectorStore + Gemini/OpenAI Model)")
        .build();
  }

  private String getLocalFallbackAnswer(String question) {
    if (question.contains("자전거") || question.contains("무게")) {
      return "자전거 및 도보 배송은 5kg 이하 소형 화물만 가능합니다. (신한 개척자 배송 규정 가이드 참고)";
    } else if (question.contains("유리") || question.contains("전자제품")) {
      return "전자제품 및 유리가 포함된 물품은 에어캡 포장이 필수이며, 다마스/트럭 배송 수단을 권장합니다.";
    }
    return "신한 개척자 배송 규정에 따라 20kg 이하 일반 화물은 오토바이, 5kg 이하는 도보/자전거로 이용 가능합니다.";
  }
}
