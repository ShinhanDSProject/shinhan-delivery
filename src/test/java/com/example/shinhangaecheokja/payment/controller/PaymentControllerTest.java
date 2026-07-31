package com.example.shinhangaecheokja.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.payment.dto.request.PointWalletCreateRequest;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
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

    PointWallet wallet = PointWallet.builder().id(1L).memberId(1L).balance(0L).build();
    when(paymentService.createWallet(any())).thenReturn(wallet);

    mockMvc
        .perform(
            post("/api/v1/point-wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.memberId").value(1L))
        .andExpect(jsonPath("$.balance").value(0));
  }

  @Test
  void 존재하지_않는_지갑을_조회하면_404를_반환한다() throws Exception {
    when(paymentService.getWallet(eq(999L)))
        .thenThrow(new EntityNotFoundException(ErrorCode.POINT_WALLET_NOT_FOUND));

    mockMvc.perform(get("/api/v1/point-wallets/999")).andExpect(status().isNotFound());
  }
}
