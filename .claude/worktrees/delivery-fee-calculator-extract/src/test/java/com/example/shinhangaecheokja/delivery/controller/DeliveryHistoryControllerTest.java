package com.example.shinhandelivery.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.JwtProvider;
import com.example.shinhandelivery.delivery.dto.response.DeliveryDetailResponseDto;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * GET /api/v1/delivery-requests는 {@code @PreAuthorize}를 쓰므로, {@code @WebMvcTest} 슬라이스로는 보안 필터체인/메서드
 * 시큐리티가 실리지 않아 검증이 안 된다. {@code NotificationControllerTest}와 동일한 이유로 실제 {@code
 * SecurityConfig}/{@code JwtProvider}까지 뜨는 {@code @SpringBootTest}로 인증 흐름 자체를 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DeliveryHistoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtProvider jwtProvider;

  @MockitoBean private DeliveryService deliveryService;

  @Test
  @DisplayName("인증 토큰이 없으면 403을 반환한다")
  void getDeliveryRequestsUnauthenticatedShouldReturn403() throws Exception {
    mockMvc.perform(get("/api/v1/delivery-requests")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("status 파라미터를 전달하면 그 상태로 필터링해 조회한다")
  void getDeliveryRequestsWithStatusFiltersByStatus() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setId(1L);
    deliveryRequest.setStatus(DeliveryStatus.CANCELLED);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");

    when(deliveryService.getMyDeliveryRequests(eq(1L), eq(DeliveryStatus.CANCELLED), any()))
        .thenReturn(new PageImpl<>(List.of(deliveryRequest)));

    mockMvc
        .perform(
            get("/api/v1/delivery-requests?status=CANCELLED")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].status").value("CANCELLED"));
  }

  @Test
  @DisplayName("status 파라미터가 없으면 회원 본인의 전체 배송 내역을 조회한다")
  void getDeliveryRequestsWithoutStatusReturnsAll() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setId(1L);
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");

    when(deliveryService.getMyDeliveryRequests(eq(1L), isNull(), any()))
        .thenReturn(new PageImpl<>(List.of(deliveryRequest)));

    mockMvc
        .perform(get("/api/v1/delivery-requests").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
  }

  @Test
  @DisplayName("인증 토큰이 없으면 상세 조회도 403을 반환한다")
  void getDeliveryRequestDetailUnauthenticatedShouldReturn403() throws Exception {
    mockMvc.perform(get("/api/v1/delivery-requests/1")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("고객 본인이 조회하면 배송원 이름을 포함한 상세 응답을 반환한다")
  void getDeliveryRequestDetailOwnerCanView() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    DeliveryDetailResponseDto response =
        new DeliveryDetailResponseDto(
            1L,
            1L,
            "서울시 강남구",
            "서울시 서초구",
            10.0,
            5.0,
            DeliveryStatus.MATCHED,
            78776L,
            37.0,
            127.0,
            38.0,
            127.0,
            ItemSize.MEDIUM,
            "박배송",
            null);
    when(deliveryService.getDeliveryRequestDetail(1L, 1L)).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/delivery-requests/1").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courierName").value("박배송"));
  }

  @Test
  @DisplayName("고객 본인도 배정된 배송원도 아니면 403을 반환한다")
  void getDeliveryRequestDetailForbiddenShouldReturn403() throws Exception {
    String token = jwtProvider.createAccessToken(999L, "stranger@test.com", "CUSTOMER");
    when(deliveryService.getDeliveryRequestDetail(999L, 1L))
        .thenThrow(new DeliveryAccessDeniedException(1L, 999L));

    mockMvc
        .perform(get("/api/v1/delivery-requests/1").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("존재하지 않는 배송 요청을 조회하면 404를 반환한다")
  void getDeliveryRequestDetailNotFoundShouldReturn404() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    when(deliveryService.getDeliveryRequestDetail(1L, 999L))
        .thenThrow(new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));

    mockMvc
        .perform(get("/api/v1/delivery-requests/999").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }
}
