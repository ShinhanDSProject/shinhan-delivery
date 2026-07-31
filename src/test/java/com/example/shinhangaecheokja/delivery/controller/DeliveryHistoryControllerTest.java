package com.example.shinhangaecheokja.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryListResponseDto;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 배송 내역 목록 조회는 {@code @PreAuthorize}로 로그인을 강제한다. {@code @WebMvcTest} 슬라이스로는 보안 필터체인/메서드 시큐리티가 실리지
 * 않아 검증이 안 되므로, {@code NotificationControllerTest}와 같은 방식으로 실제 {@code SecurityConfig}/{@code
 * JwtProvider}까지 뜨는 {@code @SpringBootTest}로 인증 흐름 자체를 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DeliveryHistoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtProvider jwtProvider;

  @MockitoBean private DeliveryService deliveryService;

  @Test
  void 인증_토큰이_없으면_403을_반환한다() throws Exception {
    mockMvc.perform(get("/api/v1/delivery-requests")).andExpect(status().isForbidden());
  }

  @Test
  void 인증된_회원은_본인_배송_내역을_조회한다() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    DeliveryListResponseDto delivery =
        new DeliveryListResponseDto(
            1L, DeliveryStatus.COMPLETED, "서울시 강남구", "서울시 서초구", LocalDateTime.now());
    when(deliveryService.getMyDeliveryRequests(eq(1L), isNull(), any()))
        .thenReturn(new PageImpl<>(List.of(delivery)));

    mockMvc
        .perform(get("/api/v1/delivery-requests").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].pickupAddress").value("서울시 강남구"));
  }

  @Test
  void status_파라미터를_전달하면_그_상태로_필터링해_조회한다() throws Exception {
    String token = jwtProvider.createAccessToken(1L, "user@test.com", "CUSTOMER");
    when(deliveryService.getMyDeliveryRequests(eq(1L), eq(DeliveryStatus.CANCELLED), any()))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc
        .perform(
            get("/api/v1/delivery-requests?status=CANCELLED")
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
  }
}
