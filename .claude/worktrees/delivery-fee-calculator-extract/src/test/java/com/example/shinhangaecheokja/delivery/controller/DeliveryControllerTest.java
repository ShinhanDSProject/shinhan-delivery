package com.example.shinhandelivery.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("배송 요청을 받으면 생성된 배송 요청을 반환한다")
  void requestDeliverySuccess() throws Exception {
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

    DeliveryRequest mockDeliveryRequest = new DeliveryRequest();
    mockDeliveryRequest.setCustomerId(1L);
    mockDeliveryRequest.setPickupAddress("서울시 강남구");
    mockDeliveryRequest.setDropoffAddress("서울시 서초구");
    mockDeliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    mockDeliveryRequest.setFeePoint(78776L);
    mockDeliveryRequest.setItemSize(ItemSize.MEDIUM);
    when(deliveryService.requestDelivery(any())).thenReturn(mockDeliveryRequest);

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
  @DisplayName("위도가 범위를 벗어나면 400을 반환한다")
  void createDeliveryLatitudeOutOfRangeShouldReturn400() throws Exception {
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
  @DisplayName("픽업 주소가 없으면 400을 반환한다")
  void createDeliveryMissingPickupAddressShouldReturn400() throws Exception {
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
  @DisplayName("견적 요청을 받으면 산정된 요금을 반환한다")
  void estimateFeeSuccess() throws Exception {
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
  @DisplayName("견적 요청에 좌표가 없으면 400을 반환한다")
  void estimateFeeMissingCoordinatesShouldReturn400() throws Exception {
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
  @DisplayName("견적 요청의 무게가 0이하면 400을 반환한다")
  void estimateFeeInvalidWeightShouldReturn400() throws Exception {
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
  @DisplayName("견적 요청에 물품 크기가 없으면 400을 반환한다")
  void estimateFeeMissingItemSizeShouldReturn400() throws Exception {
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
  @DisplayName("완료 처리 요청을 받으면 완료된 배송 요청을 반환한다")
  void completeDeliveryProcessRequest() throws Exception {
    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    DeliveryRequest completedEntity = new DeliveryRequest();
    completedEntity.setCustomerId(1L);
    completedEntity.setStatus(DeliveryStatus.COMPLETED);
    completedEntity.setFeePoint(78776L);
    completedEntity.setItemSize(ItemSize.MEDIUM);
    when(deliveryService.completeDelivery(eq(1L), any())).thenReturn(completedEntity);

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  @DisplayName("증거사진 URL이 없으면 완료 처리 요청에 400을 반환한다")
  void completeDeliveryNoPhotoUrlShouldReturn400() throws Exception {
    DeliveryCompleteRequest request = new DeliveryCompleteRequest();

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("증거사진 URL이 255자를 초과하면 완료 처리 요청에 400을 반환한다")
  void completeDeliveryPhotoUrlTooLongShouldReturn400() throws Exception {
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
  @DisplayName("MATCHED가 아닌 배송을 완료 처리하면 409를 반환한다")
  void completeDeliveryNotMatchedStatusShouldReturn409() throws Exception {
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
  @DisplayName("픽업 완료 요청을 받으면 PICKED_UP 상태의 배송 요청을 반환한다")
  void confirmPickupProcessRequest() throws Exception {
    DeliveryRequest pickedUpEntity = new DeliveryRequest();
    pickedUpEntity.setCustomerId(1L);
    pickedUpEntity.setStatus(DeliveryStatus.PICKED_UP);
    pickedUpEntity.setFeePoint(78776L);
    pickedUpEntity.setItemSize(ItemSize.MEDIUM);
    when(deliveryService.confirmPickup(1L)).thenReturn(pickedUpEntity);

    mockMvc
        .perform(patch("/api/v1/delivery-requests/1/pickup"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PICKED_UP"));
  }

  @Test
  @DisplayName("MATCHED가 아닌 배송을 픽업 처리하면 409를 반환한다")
  void confirmPickupNotMatchedStatusShouldReturn409() throws Exception {
    when(deliveryService.confirmPickup(1L))
        .thenThrow(
            new InvalidDeliveryTransitionException(
                DeliveryStatus.REQUESTED, DeliveryStatus.PICKED_UP));

    mockMvc.perform(patch("/api/v1/delivery-requests/1/pickup")).andExpect(status().isConflict());
  }

  @Test
  @DisplayName("증거사진 조회 요청을 받으면 사진 URL과 완료시각을 반환한다")
  void getProofPhotoProcessRequest() throws Exception {
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
  @DisplayName("완료되지 않은 배송의 증거사진을 조회하면 404를 반환한다")
  void getProofPhotoNotCompletedStatusShouldReturn404() throws Exception {
    when(deliveryService.getProofPhoto(1L)).thenThrow(new ProofPhotoNotFoundException(1L));

    mockMvc
        .perform(get("/api/v1/delivery-requests/1/proof-photo"))
        .andExpect(status().isNotFound());
  }
}
