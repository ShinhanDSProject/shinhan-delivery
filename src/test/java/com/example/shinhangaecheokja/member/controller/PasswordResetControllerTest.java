package com.example.shinhangaecheokja.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.exception.GlobalExceptionHandler;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetCodeRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetConfirmRequestDto;
import com.example.shinhangaecheokja.member.dto.request.PasswordResetVerifyRequestDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetCodeResponseDto;
import com.example.shinhangaecheokja.member.dto.response.PasswordResetVerifyResponseDto;
import com.example.shinhangaecheokja.member.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private PasswordResetService passwordResetService;
  @InjectMocks private PasswordResetController passwordResetController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(passwordResetController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("POST /api/v1/auth/password-reset/request-code 200 OK 응답")
  void requestCodeSuccess() throws Exception {
    given(passwordResetService.requestCode(any()))
        .willReturn(PasswordResetCodeResponseDto.of("비밀번호 재설정 인증번호가 이메일로 발송되었습니다.", 180));

    PasswordResetCodeRequestDto request = new PasswordResetCodeRequestDto("test@example.com");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset/request-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.expiresInSeconds").value(180));
  }

  @Test
  @DisplayName("POST /api/v1/auth/password-reset/verify-code 200 OK 응답")
  void verifyCodeSuccess() throws Exception {
    given(passwordResetService.verifyCode(any()))
        .willReturn(PasswordResetVerifyResponseDto.of("sample-reset-token", 600));

    PasswordResetVerifyRequestDto request =
        new PasswordResetVerifyRequestDto("test@example.com", "123456");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resetToken").value("sample-reset-token"));
  }

  @Test
  @DisplayName("POST /api/v1/auth/password-reset/confirm 204 No Content 응답")
  void confirmResetSuccess() throws Exception {
    PasswordResetConfirmRequestDto request =
        new PasswordResetConfirmRequestDto(
            "sample-reset-token", "NewPassword123!", "NewPassword123!");

    mockMvc
        .perform(
            post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }
}
