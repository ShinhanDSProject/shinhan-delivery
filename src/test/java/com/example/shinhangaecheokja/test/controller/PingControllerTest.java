package com.example.shinhangaecheokja.test.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.test.dto.PingResponse;
import com.example.shinhangaecheokja.test.service.PingService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** PingController 단위/슬라이스 테스트. */
@WebMvcTest(PingController.class)
class PingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PingService pingService;

  @Test
  @DisplayName("GET /api/test/ping 호출 시 HTTP Status 200과 message 'pong'을 반환한다")
  void ping_ReturnsPongResponse() throws Exception {
    // given
    PingResponse response = new PingResponse("pong", LocalDateTime.now());
    given(pingService.getPing()).willReturn(response);

    // when & then
    mockMvc
        .perform(get("/api/test/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("pong"));
  }
}
