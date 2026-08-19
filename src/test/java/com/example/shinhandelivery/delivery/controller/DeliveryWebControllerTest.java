package com.example.shinhandelivery.delivery.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.common.security.WebAuthHelper;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DeliveryWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DeliveryService deliveryService;

  @MockitoBean private WebAuthHelper webAuthHelper;

  @Test
  @DisplayName("배송 내역 뷰 요청 시 delivery-history 뷰를 반환한다")
  void deliveryHistoryReturnsView() throws Exception {
    mockMvc
        .perform(get("/delivery-history"))
        .andExpect(status().isOk())
        .andExpect(view().name("delivery-history"));
  }

  @Test
  @DisplayName("배송 내역이 존재하면 생성일을 포함한 목록 화면을 렌더링한다")
  void deliveryHistoryRendersCreatedAt() throws Exception {
    DeliveryRequest delivery =
        DeliveryRequest.builder()
            .id(1L)
            .memberId(10L)
            .pickupAddress("서울역")
            .dropoffAddress("강남역")
            .weight(1.0)
            .distance(10.0)
            .status(DeliveryStatus.COMPLETED)
            .feePoint(5000L)
            .createdAt(LocalDateTime.of(2026, 8, 19, 9, 30))
            .build();
    when(webAuthHelper.getCurrentMemberId()).thenReturn(Optional.of(10L));
    when(deliveryService.getMyDeliveryRequests(eq(10L), isNull(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(delivery)));

    mockMvc
        .perform(get("/delivery-history"))
        .andExpect(status().isOk())
        .andExpect(view().name("delivery-history"))
        .andExpect(content().string(containsString("2026.08.19")))
        .andExpect(content().string(containsString("서울역 → 강남역")))
        .andExpect(content().string(containsString("5,000 P")));
  }

  @Test
  @DisplayName("배송 취소 내역 화면에는 고객 하단 내비게이션을 표시한다")
  void deliveryCancelListShowsCustomerBottomNavigation() throws Exception {
    mockMvc
        .perform(get("/delivery-cancel-list"))
        .andExpect(status().isOk())
        .andExpect(view().name("delivery-cancel-list"))
        .andExpect(content().string(containsString("class=\"customer-bottom-nav\"")));
  }
}
