package com.example.shinhangaecheokja.address.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.address.dto.request.AddressCreateRequest;
import com.example.shinhangaecheokja.address.dto.request.AddressUpdateRequest;
import com.example.shinhangaecheokja.address.entity.Address;
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

    Address addr =
        Address.builder()
            .id(1L)
            .memberId(10L)
            .alias("집")
            .address("서울시 강남구")
            .detailAddress("101호")
            .pickupGuide("문 앞")
            .build();
    when(addressService.getAddresses(eq(10L))).thenReturn(List.of(addr));

    mockMvc
        .perform(get("/api/v1/addresses").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].alias").value("집"))
        .andExpect(jsonPath("$[0].address").value("서울시 강남구"));
  }

  @Test
  void 주소_생성_요청을_처리한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    AddressCreateRequest request = new AddressCreateRequest();
    request.setAlias("회사");
    request.setAddress("서울시 서초구");

    Address created = Address.builder().id(2L).memberId(10L).alias("회사").address("서울시 서초구").build();
    when(addressService.createAddress(eq(10L), any())).thenReturn(created);

    mockMvc
        .perform(
            post("/api/v1/addresses")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.alias").value("회사"));
  }

  @Test
  void 주소_수정_요청을_처리한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    AddressUpdateRequest request = new AddressUpdateRequest();
    request.setAlias("우리집");
    request.setAddress("서울시 송파구");

    Address updated =
        Address.builder().id(1L).memberId(10L).alias("우리집").address("서울시 송파구").build();
    when(addressService.updateAddress(eq(1L), eq(10L), any())).thenReturn(updated);

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
  void 주소_삭제_요청을_처리한다() throws Exception {
    CustomUserDetails customUser =
        new CustomUserDetails(10L, "user@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    mockMvc
        .perform(delete("/api/v1/addresses/1").principal(auth))
        .andExpect(status().isNoContent());

    verify(addressService).deleteAddress(1L, 10L);
  }
}
