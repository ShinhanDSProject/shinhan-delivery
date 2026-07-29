package com.example.shinhangaecheokja.address.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.address.dto.request.AddressCreateRequest;
import com.example.shinhangaecheokja.address.dto.request.AddressUpdateRequest;
import com.example.shinhangaecheokja.address.dto.response.AddressResponse;
import com.example.shinhangaecheokja.address.service.AddressService;
import com.example.shinhangaecheokja.common.exception.GlobalExceptionHandler;
import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private AddressService addressService;
  @InjectMocks private AddressController addressController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(addressController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void 인증된_사용자의_주소_목록을_조회한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    AddressResponse addr = new AddressResponse(1L, 10L, "집", "서울시 강남구", "101호", "문 앞");
    when(addressService.getAddresses(eq(10L))).thenReturn(List.of(addr));

    mockMvc
        .perform(get("/api/v1/addresses").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].alias").value("집"))
        .andExpect(jsonPath("$[0].address").value("서울시 강남구"));
  }

  @Test
  void 신규_주소를_성공적으로_등록한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    AddressCreateRequest request = new AddressCreateRequest("회사", "서울시 서초구", "202호", "경비실");
    AddressResponse response = new AddressResponse(2L, 10L, "회사", "서울시 서초구", "202호", "경비실");

    when(addressService.createAddress(eq(10L), any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/addresses")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.alias").value("회사"));
  }

  @Test
  void 주소를_성공적으로_수정한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    AddressUpdateRequest request = new AddressUpdateRequest("우리집", "서울시 강남구", "303호", "직접 전달");
    AddressResponse response = new AddressResponse(1L, 10L, "우리집", "서울시 강남구", "303호", "직접 전달");

    when(addressService.updateAddress(eq(1L), eq(10L), any())).thenReturn(response);

    mockMvc
        .perform(
            patch("/api/v1/addresses/1")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.alias").value("우리집"));
  }

  @Test
  void 주소를_성공적으로_삭제한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    doNothing().when(addressService).deleteAddress(eq(1L), eq(10L));

    mockMvc
        .perform(delete("/api/v1/addresses/1").principal(auth))
        .andExpect(status().isNoContent());
  }

  @Test
  void 필수_입력값인_별칭이_누락되면_400을_반환한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    AddressCreateRequest request = new AddressCreateRequest("", "서울시 서초구", "202호", "경비실");

    mockMvc
        .perform(
            post("/api/v1/addresses")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 미인증_사용자가_주소_목록_조회시_401을_반환한다() throws Exception {
    mockMvc.perform(get("/api/v1/addresses")).andExpect(status().isUnauthorized());
  }
}
