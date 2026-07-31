package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhangaecheokja.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        .thenThrow(new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));

    mockMvc.perform(get("/api/v1/delivery-requests/999")).andExpect(status().isNotFound());
  }

  @Test
  void 완료_처리_요청을_받으면_완료된_배송_요청을_반환한다() throws Exception {
    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    when(deliveryService.completeDelivery(eq(1L), any()))
        .thenReturn(
            new DeliveryResponse(
                1L,
                1L,
                "서울시 강남구",
                "서울시 서초구",
                10,
                111.19,
                DeliveryStatus.COMPLETED,
                78776L,
                37.0,
                127.0,
                38.0,
                127.0,
                ItemSize.MEDIUM));

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void 증거사진_URL이_없으면_완료_처리_요청에_400을_반환한다() throws Exception {
    DeliveryCompleteRequest request = new DeliveryCompleteRequest();

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 증거사진_URL이_255자를_초과하면_완료_처리_요청에_400을_반환한다() throws Exception {
    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("a".repeat(256));

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void MATCHED가_아닌_배송을_완료_처리하면_409를_반환한다() throws Exception {
    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    when(deliveryService.completeDelivery(eq(1L), any()))
        .thenThrow(
            new InvalidDeliveryTransitionException(
                DeliveryStatus.REQUESTED, DeliveryStatus.COMPLETED));

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  void 픽업_완료_요청을_받으면_PICKED_UP_상태의_배송_요청을_반환한다() throws Exception {
    when(deliveryService.confirmPickup(1L))
        .thenReturn(
            new DeliveryResponse(
                1L,
                1L,
                "서울시 강남구",
                "서울시 서초구",
                10,
                111.19,
                DeliveryStatus.PICKED_UP,
                78776L,
                37.0,
                127.0,
                38.0,
                127.0,
                ItemSize.MEDIUM));

    mockMvc
        .perform(patch("/api/v1/delivery-requests/1/pickup"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PICKED_UP"));
  }

  @Test
  void MATCHED가_아닌_배송을_픽업_처리하면_409를_반환한다() throws Exception {
    when(deliveryService.confirmPickup(1L))
        .thenThrow(
            new InvalidDeliveryTransitionException(
                DeliveryStatus.REQUESTED, DeliveryStatus.PICKED_UP));

    mockMvc.perform(patch("/api/v1/delivery-requests/1/pickup")).andExpect(status().isConflict());
  }

  @Test
  void 증거사진_조회_요청을_받으면_사진_URL과_완료시각을_반환한다() throws Exception {
    LocalDateTime completedAt = LocalDateTime.of(2026, 7, 31, 10, 0);
    when(deliveryService.getProofPhoto(1L))
        .thenReturn(new ProofPhotoResponse(1L, "https://example.com/proof.jpg", completedAt));

    mockMvc
        .perform(get("/api/v1/delivery-requests/1/proof-photo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.proofPhotoUrl").value("https://example.com/proof.jpg"))
        .andExpect(jsonPath("$.completedAt").exists());
  }

  @Test
  void 완료되지_않은_배송의_증거사진을_조회하면_404를_반환한다() throws Exception {
    when(deliveryService.getProofPhoto(1L)).thenThrow(new ProofPhotoNotFoundException(1L));

    mockMvc
        .perform(get("/api/v1/delivery-requests/1/proof-photo"))
        .andExpect(status().isNotFound());
  }
}
