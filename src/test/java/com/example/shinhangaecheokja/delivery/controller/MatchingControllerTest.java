package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.exception.MatchingNotFoundException;
import com.example.shinhangaecheokja.delivery.service.MatchingService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MatchingController.class)
class MatchingControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MatchingService matchingService;

  @Test
  void 매칭_생성_요청을_받으면_생성된_매칭을_반환한다() throws Exception {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(matchingService.createMatching(any()))
        .thenReturn(new MatchingResponse(1L, 1L, 2L, MatchingStatus.MATCHED, LocalDateTime.now()));

    mockMvc
        .perform(
            post("/api/matchings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deliveryRequestId").value(1L))
        .andExpect(jsonPath("$.status").value("MATCHED"));
  }

  @Test
  void 존재하지_않는_매칭을_조회하면_404를_반환한다() throws Exception {
    when(matchingService.getMatching(eq(999L))).thenThrow(new MatchingNotFoundException(999L));

    mockMvc.perform(get("/api/matchings/999")).andExpect(status().isNotFound());
  }
}
