package com.example.shinhandelivery.vehicle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private VehicleService vehicleService;

  @Test
  @DisplayName("차량 생성 요청을 받으면 생성된 차량을 반환한다")
  void registerVehicleSuccess() throws Exception {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setMemberId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);

    Vehicle vehicle =
        Vehicle.builder()
            .id(1L)
            .memberId(1L)
            .type(VehicleType.CAR)
            .maxWeight(500)
            .maxDistance(100)
            .location(Location.of(37.5, 127.0))
            .status(VehicleStatus.AVAILABLE)
            .build();
    when(vehicleService.create(any())).thenReturn(vehicle);

    mockMvc
        .perform(
            post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.memberId").value(1L))
        .andExpect(jsonPath("$.type").value("CAR"));
  }

  @Test
  @DisplayName("경도가 범위를 벗어나면 400을 반환한다")
  void registerVehicleLongitudeOutOfRangeShouldReturn400() throws Exception {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setMemberId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);
    request.setLatitude(37.5);
    request.setLongitude(200);

    mockMvc
        .perform(
            post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("소유자 id가 없으면 400을 반환한다")
  void registerVehicleMissingOwnerIdShouldReturn400() throws Exception {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);

    mockMvc
        .perform(
            post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("존재하지 않는 차량을 조회하면 404를 반환한다")
  void getVehicleNotFoundShouldReturn404() throws Exception {
    when(vehicleService.getById(eq(999L)))
        .thenThrow(new EntityNotFoundException(ErrorCode.VEHICLE_NOT_FOUND));

    mockMvc.perform(get("/api/v1/vehicles/999")).andExpect(status().isNotFound());
  }
}
