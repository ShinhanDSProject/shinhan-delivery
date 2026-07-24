package com.example.shinhangaecheokja.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.entity.VehicleType;
import com.example.shinhangaecheokja.exception.VehicleNotFoundException;
import com.example.shinhangaecheokja.service.VehicleService;
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
  void 차량_생성_요청을_받으면_생성된_차량을_반환한다() throws Exception {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setOwnerId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);

    when(vehicleService.registerVehicle(any()))
        .thenReturn(new VehicleResponse(1L, 1L, VehicleType.CAR, 500, 100));

    mockMvc
        .perform(
            post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ownerId").value(1L))
        .andExpect(jsonPath("$.type").value("CAR"));
  }

  @Test
  void 존재하지_않는_차량을_조회하면_404를_반환한다() throws Exception {
    when(vehicleService.getVehicle(eq(999L))).thenThrow(new VehicleNotFoundException(999L));

    mockMvc.perform(get("/api/vehicles/999")).andExpect(status().isNotFound());
  }
}
