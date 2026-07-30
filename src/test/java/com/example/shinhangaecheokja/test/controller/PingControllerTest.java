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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PingService pingService;

  @Test
  @DisplayName("GET /api/test/ping 요청 시 200 OK와 message: pong 응답을 반환한다")
  void getPing_success() throws Exception {
    given(pingService.getPingMessage()).willReturn(new PingResponse("pong", LocalDateTime.now()));

    mockMvc
        .perform(get("/api/test/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("pong"));
  }
}
