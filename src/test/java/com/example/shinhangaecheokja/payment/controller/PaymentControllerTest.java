package com.example.shinhangaecheokja.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointWalletResponse;
import com.example.shinhangaecheokja.payment.exception.PointWalletNotFoundException;
import com.example.shinhangaecheokja.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PaymentService paymentService;

  @Test
  void 지갑_생성_요청을_받으면_생성된_지갑을_반환한다() throws Exception {
    PointWalletCreateRequest request = new PointWalletCreateRequest();
    request.setMemberId(1L);

    when(paymentService.createWallet(any())).thenReturn(new PointWalletResponse(1L, 1L, 0L));

    mockMvc
        .perform(
            post("/api/point-wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberId").value(1L))
        .andExpect(jsonPath("$.balance").value(0));
  }

  @Test
  void 존재하지_않는_지갑을_조회하면_404를_반환한다() throws Exception {
    when(paymentService.getWallet(eq(999L))).thenThrow(new PointWalletNotFoundException(999L));

    mockMvc.perform(get("/api/point-wallets/999")).andExpect(status().isNotFound());
  }
}
