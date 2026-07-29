package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import java.math.BigDecimal;
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
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(deliveryService.requestDelivery(any()))
        .thenReturn(
            new DeliveryResponse(
                1L,
                1L,
                "서울시 강남구",
                "서울시 서초구",
                10,
                111.19,
                DeliveryStatus.REQUESTED,
                78776L,
                37.0,
                127.0,
                38.0,
                127.0,
                ItemSize.MEDIUM));

    mockMvc
        .perform(
            post("/api/v1/delivery-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerId").value(1L))
        .andExpect(jsonPath("$.status").value("REQUESTED"));
  }

  @Test
  void 위도가_범위를_벗어나면_400을_반환한다() throws Exception {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setWeight(10);
    request.setPickupLatitude(200);
    request.setPickupLongitude(127.0);

    mockMvc
        .perform(
            post("/api/v1/delivery-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 픽업_주소가_없으면_400을_반환한다() throws Exception {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);

    mockMvc
        .perform(
            post("/api/v1/delivery-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 견적_요청을_받으면_산정된_요금을_반환한다() throws Exception {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setPickupLatitude(37.5665);
    request.setPickupLongitude(126.9780);
    request.setDestinationLatitude(35.1796);
    request.setDestinationLongitude(129.0756);
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(deliveryService.estimateFee(any()))
        .thenReturn(
            new DeliveryEstimateResponse(
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(162556),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(50267),
                BigDecimal.valueOf(217823)));

    mockMvc
        .perform(
            post("/api/v1/delivery-requests/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalFee").value(217823));
  }

  @Test
  void 견적_요청에_좌표가_없으면_400을_반환한다() throws Exception {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setDestinationLatitude(35.1796);
    request.setDestinationLongitude(129.0756);
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    mockMvc
        .perform(
            post("/api/v1/delivery-requests/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 견적_요청의_무게가_0이하면_400을_반환한다() throws Exception {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setPickupLatitude(37.5665);
    request.setPickupLongitude(126.9780);
    request.setDestinationLatitude(35.1796);
    request.setDestinationLongitude(129.0756);
    request.setWeight(0.0);
    request.setItemSize(ItemSize.MEDIUM);

    mockMvc
        .perform(
            post("/api/v1/delivery-requests/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 견적_요청에_물품_크기가_없으면_400을_반환한다() throws Exception {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setPickupLatitude(37.5665);
    request.setPickupLongitude(126.9780);
    request.setDestinationLatitude(35.1796);
    request.setDestinationLongitude(129.0756);
    request.setWeight(10.0);

    mockMvc
        .perform(
            post("/api/v1/delivery-requests/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 존재하지_않는_배송_요청을_조회하면_404를_반환한다() throws Exception {
    when(deliveryService.getDeliveryRequest(eq(999L)))
        .thenThrow(
            new com.example.shinhangaecheokja.common.exception.EntityNotFoundException(
                com.example.shinhangaecheokja.common.exception.ErrorCode.DELIVERY_NOT_FOUND));

    mockMvc.perform(get("/api/v1/delivery-requests/999")).andExpect(status().isNotFound());
  }
}
