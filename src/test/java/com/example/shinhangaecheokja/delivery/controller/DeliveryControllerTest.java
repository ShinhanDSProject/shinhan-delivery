package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DeliveryController.class)
class DeliveryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private DeliveryService deliveryService;

  @Test
  void 배송_요청을_받으면_생성된_배송_요청을_반환한다() throws Exception {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setDistance(5);

    when(deliveryService.requestDelivery(any()))
        .thenReturn(
            new DeliveryResponse(
                1L, 1L, "서울시 강남구", "서울시 서초구", 10, 5, DeliveryStatus.REQUESTED, 600L));

    mockMvc
        .perform(
            post("/api/delivery-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerId").value(1L))
        .andExpect(jsonPath("$.status").value("REQUESTED"));
  }

  @Test
  void 존재하지_않는_배송_요청을_조회하면_404를_반환한다() throws Exception {
    when(deliveryService.getDeliveryRequest(eq(999L)))
        .thenThrow(new DeliveryRequestNotFoundException(999L));

    mockMvc.perform(get("/api/delivery-requests/999")).andExpect(status().isNotFound());
  }
}
