package com.example.shinhandelivery.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.admin.dto.response.PendingCourierResponseDto;
import com.example.shinhandelivery.admin.service.AdminCourierService;
import com.example.shinhandelivery.member.entity.CourierApprovalStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminCourierController.class)
class AdminCourierControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AdminCourierService adminCourierService;

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("GET /api/v1/admin/couriers/pending 호출 시 승인 대기 기사 목록을 반환한다")
  void getPendingCouriersReturnsPage() throws Exception {
    PendingCourierResponseDto dto =
        new PendingCourierResponseDto(
            1L,
            "courier@example.com",
            "박배송",
            "010-1234-5678",
            "마포구",
            50.0,
            CourierApprovalStatus.PENDING,
            "http://example.com/license.png");

    when(adminCourierService.getPendingCouriers(any())).thenReturn(new PageImpl<>(List.of(dto)));

    mockMvc
        .perform(get("/api/v1/admin/couriers/pending"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("박배송"))
        .andExpect(jsonPath("$.content[0].courierApprovalStatus").value("PENDING"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("PATCH /api/v1/admin/couriers/{id}/approve 호출 시 승인 결과를 반환한다")
  void approveCourierReturnsApprovedDto() throws Exception {
    PendingCourierResponseDto dto =
        new PendingCourierResponseDto(
            1L,
            "courier@example.com",
            "박배송",
            "010-1234-5678",
            "마포구",
            50.0,
            CourierApprovalStatus.APPROVED,
            "http://example.com/license.png");

    when(adminCourierService.approveCourier(1L)).thenReturn(dto);

    mockMvc
        .perform(patch("/api/v1/admin/couriers/1/approve"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courierApprovalStatus").value("APPROVED"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("PATCH /api/v1/admin/couriers/{id}/reject 호출 시 거절 결과를 반환한다")
  void rejectCourierReturnsRejectedDto() throws Exception {
    PendingCourierResponseDto dto =
        new PendingCourierResponseDto(
            1L,
            "courier@example.com",
            "박배송",
            "010-1234-5678",
            "마포구",
            50.0,
            CourierApprovalStatus.REJECTED,
            "http://example.com/license.png");

    when(adminCourierService.rejectCourier(1L)).thenReturn(dto);

    mockMvc
        .perform(patch("/api/v1/admin/couriers/1/reject"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courierApprovalStatus").value("REJECTED"));
  }
}
