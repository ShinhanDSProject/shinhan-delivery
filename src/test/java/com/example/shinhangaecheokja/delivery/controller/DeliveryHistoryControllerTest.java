package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
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
}
