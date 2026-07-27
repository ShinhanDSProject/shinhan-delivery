package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.exception.MatchingNotFoundException;
import com.example.shinhangaecheokja.delivery.service.MatchingService;
import java.time.LocalDateTime;
import java.util.List;
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
  void 차량의_열린_콜_목록을_조회한다() throws Exception {
    when(matchingService.getOpenCalls(2L))
        .thenReturn(
            List.of(
                new DeliveryResponse(
                    1L, 1L, "서울시 강남구", "서울시 서초구", 10, 5, DeliveryStatus.REQUESTED, 600L, 37.5,
                    127.0)));

    mockMvc
        .perform(get("/api/matchings/calls").param("vehicleId", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].status").value("REQUESTED"));
  }

  @Test
  void 존재하지_않는_매칭을_조회하면_404를_반환한다() throws Exception {
    when(matchingService.getMatching(eq(999L))).thenThrow(new MatchingNotFoundException(999L));

    mockMvc.perform(get("/api/matchings/999")).andExpect(status().isNotFound());
  }

  @Test
  void 매칭_생성시_deliveryRequestId가_없으면_400을_반환한다() throws Exception {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setVehicleId(2L);

    mockMvc
        .perform(
            post("/api/matchings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 매칭_상태_변경시_status가_없으면_400을_반환한다() throws Exception {
    MatchingUpdateRequest request = new MatchingUpdateRequest();

    mockMvc
        .perform(
            put("/api/matchings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
