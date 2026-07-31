package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.service.MatchingService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("매칭 생성 요청을 받으면 생성된 매칭을 반환한다")
  void createMatchingSuccess() throws Exception {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    Matching matching = Matching.from(request);
    matching.setId(1L);

    when(matchingService.createMatching(any())).thenReturn(matching);

    mockMvc
        .perform(
            post("/api/v1/matchings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.deliveryRequestId").value(1L))
        .andExpect(jsonPath("$.status").value("MATCHED"));
  }

  @Test
  @DisplayName("차량의 열린 콜 목록을 조회한다")
  void getOpenCallsSuccess() throws Exception {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setId(1L);
    deliveryRequest.setCustomerId(1L);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");
    deliveryRequest.setWeight(10);
    deliveryRequest.setDistance(5);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setFeePoint(600L);

    when(matchingService.getOpenCalls(2L)).thenReturn(List.of(deliveryRequest));

    mockMvc
        .perform(get("/api/v1/matchings/calls").param("vehicleId", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].status").value("REQUESTED"));
  }

  @Test
  @DisplayName("존재하지 않는 매칭을 조회하면 404를 반환한다")
  void getMatchingNotFoundShouldReturn404() throws Exception {
    when(matchingService.getMatching(eq(999L)))
        .thenThrow(new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));

    mockMvc.perform(get("/api/v1/matchings/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("매칭 생성시 deliveryRequestId가 없으면 400을 반환한다")
  void createMatchingMissingDeliveryRequestIdShouldReturn400() throws Exception {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setVehicleId(2L);

    mockMvc
        .perform(
            post("/api/v1/matchings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("매칭 상태 변경시 status가 없으면 400을 반환한다")
  void updateMatchingMissingStatusShouldReturn400() throws Exception {
    MatchingUpdateRequest request = new MatchingUpdateRequest();

    mockMvc
        .perform(
            put("/api/v1/matchings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
