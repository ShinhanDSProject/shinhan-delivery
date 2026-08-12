package com.example.shinhandelivery.delivery.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPayLocationRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPayRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPickupRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryCancellationPreviewResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryPaymentResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryReorderResponse;
import com.example.shinhandelivery.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhandelivery.delivery.service.DeliveryCancellationService;
import com.example.shinhandelivery.delivery.service.DeliveryMatchingService;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.member.service.MemberService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DeliveryController.class)
class DeliveryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private DeliveryService deliveryService;
  @MockitoBean private DeliveryMatchingService deliveryMatchingService;
  @MockitoBean private DeliveryCancellationService deliveryCancellationService;
  @MockitoBean private MemberService memberService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("견적 요청을 받으면 예정된 요금을 반환한다")
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
  @DisplayName("취소 예상 조회는 상태별 수수료와 환불액을 반환한다")
  void previewCancellationReturnsSettlement() throws Exception {
    CustomUserDetails userDetails =
        new CustomUserDetails(1L, "user@test.com", "password", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(deliveryCancellationService.preview(1L, 10L))
        .thenReturn(
            new DeliveryCancellationPreviewResponse(
                10L, DeliveryStatus.MATCHED, 3000L, 1000L, 2000L, 1000L));

    mockMvc
        .perform(get("/api/v1/delivery-requests/10/cancellation-preview"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cancellationFee").value(1000))
        .andExpect(jsonPath("$.refundAmount").value(2000));
  }

  @Test
  @DisplayName("재배송 입력값 조회는 로그인 고객 ID로 서비스에 위임한다")
  void getReorderDraftReturnsReusableFields() throws Exception {
    CustomUserDetails userDetails =
        new CustomUserDetails(1L, "user@test.com", "password", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(deliveryService.getReorderDraft(1L, 10L))
        .thenReturn(
            new DeliveryReorderResponse(
                10L, "서울 강남구", 37.1, 127.1, "서울 서초구", 37.2, 127.2, 3.5, ItemSize.SMALL));

    mockMvc
        .perform(get("/api/v1/delivery-requests/10/reorder"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sourceDeliveryRequestId").value(10))
        .andExpect(jsonPath("$.pickupAddress").value("서울 강남구"))
        .andExpect(jsonPath("$.pickupLatitude").value(37.1))
        .andExpect(jsonPath("$.pickupLongitude").value(127.1))
        .andExpect(jsonPath("$.dropoffAddress").value("서울 서초구"))
        .andExpect(jsonPath("$.dropoffLatitude").value(37.2))
        .andExpect(jsonPath("$.dropoffLongitude").value(127.2))
        .andExpect(jsonPath("$.weight").value(3.5))
        .andExpect(jsonPath("$.itemSize").value("SMALL"))
        .andExpect(jsonPath("$.feePoint").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.status").doesNotHaveJsonPath());

    verify(deliveryService).getReorderDraft(1L, 10L);
  }

  @Test
  @DisplayName("다른 고객의 배송으로 재배송을 요청하면 403을 반환한다")
  void getReorderDraftByNonOwnerReturnsForbidden() throws Exception {
    CustomUserDetails userDetails =
        new CustomUserDetails(999L, "other@test.com", "password", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    when(deliveryService.getReorderDraft(999L, 10L))
        .thenThrow(new DeliveryAccessDeniedException(10L, 999L));

    mockMvc.perform(get("/api/v1/delivery-requests/10/reorder")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("기존 DELETE 취소 API는 로그인한 고객 ID로 취소하고 204를 반환한다")
  void deleteDeliveryRequestReturnsNoContent() throws Exception {
    CustomUserDetails userDetails =
        new CustomUserDetails(1L, "user@test.com", "password", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    mockMvc.perform(delete("/api/v1/delivery-requests/10")).andExpect(status().isNoContent());

    verify(deliveryCancellationService).cancel(1L, 10L);
  }

  @Test
  @DisplayName("인증 없이 기존 DELETE 취소 API를 호출하면 401을 반환한다")
  void deleteDeliveryRequestWithoutAuthenticationReturnsUnauthorized() throws Exception {
    mockMvc.perform(delete("/api/v1/delivery-requests/10")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("배송 결제 요청을 받으면 배송 요청 ID와 차감 결과를 반환한다")
  void payDeliverySuccess() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(1L, "pay@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryPayRequest request = new DeliveryPayRequest();
    request.setPickup(createLocation("서울 강남구", 37.0, 127.0));
    request.setDropoff(createLocation("서울 서초구", 38.0, 127.0));
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(deliveryService.payDelivery(eq(1L), eq("idem-pay"), any()))
        .thenReturn(
            new DeliveryPaymentResponse(55L, 78776L, 5000L, DeliveryStatus.REQUESTED, "MATCHING"));

    mockMvc
        .perform(
            post("/api/v1/delivery-requests/pay")
                .with(authentication(auth))
                .header("Idempotency-Key", "idem-pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deliveryRequestId").value(55L))
        .andExpect(jsonPath("$.remainingBalance").value(5000L))
        .andExpect(jsonPath("$.deliveryStatus").value("REQUESTED"));

    verify(memberService).requirePaymentPinConfigured(1L);
    ArgumentCaptor<DeliveryPayRequest> requestCaptor =
        ArgumentCaptor.forClass(DeliveryPayRequest.class);
    verify(deliveryService).payDelivery(eq(1L), eq("idem-pay"), requestCaptor.capture());
    DeliveryPayRequest captured = requestCaptor.getValue();
    assertThat(captured.getPickup().getAddress()).isEqualTo(request.getPickup().getAddress());
    assertThat(captured.getPickup().getLat()).isEqualTo(request.getPickup().getLat());
    assertThat(captured.getPickup().getLng()).isEqualTo(request.getPickup().getLng());
    assertThat(captured.getDropoff().getAddress()).isEqualTo(request.getDropoff().getAddress());
    assertThat(captured.getDropoff().getLat()).isEqualTo(request.getDropoff().getLat());
    assertThat(captured.getDropoff().getLng()).isEqualTo(request.getDropoff().getLng());
    assertThat(captured.getWeight()).isEqualTo(request.getWeight());
    assertThat(captured.getItemSize()).isEqualTo(request.getItemSize());
  }

  @Test
  @DisplayName("공백 Idempotency-Key면 400을 반환한다")
  void payDeliveryBlankIdempotencyKeyShouldReturn400() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(1L, "pay@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryPayRequest request = new DeliveryPayRequest();
    request.setPickup(createLocation("서울 강남구", 37.0, 127.0));
    request.setDropoff(createLocation("서울 서초구", 38.0, 127.0));
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    mockMvc
        .perform(
            post("/api/v1/delivery-requests/pay")
                .with(authentication(auth))
                .header("Idempotency-Key", "   ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("101자 Idempotency-Key면 400을 반환한다")
  void payDeliveryTooLongIdempotencyKeyShouldReturn400() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(1L, "pay@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryPayRequest request = new DeliveryPayRequest();
    request.setPickup(createLocation("서울 강남구", 37.0, 127.0));
    request.setDropoff(createLocation("서울 서초구", 38.0, 127.0));
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    mockMvc
        .perform(
            post("/api/v1/delivery-requests/pay")
                .with(authentication(auth))
                .header("Idempotency-Key", "a".repeat(101))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
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
  @DisplayName("견적 요청에 무게가 0 이하면 400을 반환한다")
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
    CustomUserDetails customUser =
        new CustomUserDetails(20L, "courier@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    DeliveryRequest completedEntity = new DeliveryRequest();
    completedEntity.setMemberId(1L);
    completedEntity.setStatus(DeliveryStatus.COMPLETED);
    completedEntity.setFeePoint(78776L);
    completedEntity.setItemSize(ItemSize.MEDIUM);
    when(deliveryService.completeDelivery(eq(20L), eq(1L), any())).thenReturn(completedEntity);

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  @DisplayName("증빙사진 URL이 없으면 완료 처리 요청은 400을 반환한다")
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
  @DisplayName("증빙사진 URL이 255자를 초과하면 완료 처리 요청은 400을 반환한다")
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
    CustomUserDetails customUser =
        new CustomUserDetails(20L, "courier@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    when(deliveryService.completeDelivery(eq(20L), eq(1L), any()))
        .thenThrow(
            new InvalidDeliveryTransitionException(
                DeliveryStatus.REQUESTED, DeliveryStatus.COMPLETED));

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("배정된 배송원이 아니면 완료 처리 요청은 403을 반환한다")
  void completeDeliveryNonAssignedCourierShouldReturn403() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(999L, "stranger@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    when(deliveryService.completeDelivery(eq(999L), eq(1L), any()))
        .thenThrow(new DeliveryAccessDeniedException(1L, 999L));

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/complete")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("픽업 완료 요청을 받으면 PICKED_UP 상태의 배송 요청을 반환한다")
  void confirmPickupProcessRequest() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(20L, "courier@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryPickupRequest request = new DeliveryPickupRequest();
    request.setPickupPhotoUrl("https://example.com/pickup.jpg");

    DeliveryRequest pickedUpEntity = new DeliveryRequest();
    pickedUpEntity.setMemberId(1L);
    pickedUpEntity.setStatus(DeliveryStatus.PICKED_UP);
    pickedUpEntity.setFeePoint(78776L);
    pickedUpEntity.setItemSize(ItemSize.MEDIUM);
    when(deliveryService.confirmPickup(eq(20L), eq(1L), any())).thenReturn(pickedUpEntity);

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/pickup")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PICKED_UP"));
  }

  @Test
  @DisplayName("물품 확인 사진 URL이 없으면 픽업 완료 요청은 400을 반환한다")
  void confirmPickupNoPhotoUrlShouldReturn400() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(20L, "courier@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryPickupRequest request = new DeliveryPickupRequest();

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/pickup")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("MATCHED가 아닌 배송을 픽업 처리하면 409를 반환한다")
  void confirmPickupNotMatchedStatusShouldReturn409() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(20L, "courier@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryPickupRequest request = new DeliveryPickupRequest();
    request.setPickupPhotoUrl("https://example.com/pickup.jpg");

    when(deliveryService.confirmPickup(eq(20L), eq(1L), any()))
        .thenThrow(
            new InvalidDeliveryTransitionException(
                DeliveryStatus.REQUESTED, DeliveryStatus.PICKED_UP));

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/pickup")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("배정된 배송원이 아니면 픽업 처리 요청은 403을 반환한다")
  void confirmPickupNonAssignedCourierShouldReturn403() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(999L, "stranger@example.com", "pass", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DeliveryPickupRequest request = new DeliveryPickupRequest();
    request.setPickupPhotoUrl("https://example.com/pickup.jpg");

    when(deliveryService.confirmPickup(eq(999L), eq(1L), any()))
        .thenThrow(new DeliveryAccessDeniedException(1L, 999L));

    mockMvc
        .perform(
            patch("/api/v1/delivery-requests/1/pickup")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("증빙사진 조회 요청을 받으면 사진 URL과 완료시각을 반환한다")
  void getProofPhotoProcessRequest() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(1L, "proof@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    LocalDateTime completedAt = LocalDateTime.of(2026, 7, 31, 10, 0);
    when(deliveryService.getProofPhoto(1L, 1L))
        .thenReturn(new ProofPhotoResponse(1L, "https://example.com/proof.jpg", completedAt));

    mockMvc
        .perform(get("/api/v1/delivery-requests/1/proof-photo").with(authentication(auth)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.proofPhotoUrl").value("https://example.com/proof.jpg"))
        .andExpect(jsonPath("$.completedAt").exists());

    verify(memberService).requirePaymentPinConfigured(1L);
  }

  @Test
  @DisplayName("완료되지 않은 배송의 증빙사진을 조회하면 404를 반환한다")
  void getProofPhotoNotCompletedStatusShouldReturn404() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(1L, "proof@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    when(deliveryService.getProofPhoto(1L, 1L)).thenThrow(new ProofPhotoNotFoundException(1L));

    mockMvc
        .perform(get("/api/v1/delivery-requests/1/proof-photo").with(authentication(auth)))
        .andExpect(status().isNotFound());
  }

  private DeliveryPayLocationRequest createLocation(String address, double lat, double lng) {
    DeliveryPayLocationRequest location = new DeliveryPayLocationRequest();
    location.setAddress(address);
    location.setLat(lat);
    location.setLng(lng);
    return location;
  }
}
