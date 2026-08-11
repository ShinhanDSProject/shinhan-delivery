package com.example.shinhandelivery.address.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.address.entity.Address;
import com.example.shinhandelivery.address.service.AddressService;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AddressWebControllerTest {

  private static final Long MEMBER_ID = 10L;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AddressService addressService;

  @Test
  @DisplayName("주소 관리 화면은 로그인 회원의 주소 목록을 SSR로 렌더링한다")
  void addressManagementRendersAddresses() throws Exception {
    given(addressService.list(MEMBER_ID)).willReturn(List.of(savedAddress()));

    mockMvc
        .perform(get("/address-management").with(user(principal())))
        .andExpect(status().isOk())
        .andExpect(view().name("address-management"))
        .andExpect(model().attributeExists("addresses"))
        .andExpect(content().string(containsString("우리 집")))
        .andExpect(content().string(containsString("서울특별시 중구 세종대로 110")))
        .andExpect(content().string(containsString("101동 1001호")));
  }

  @Test
  @DisplayName("주소 입력 화면은 최근 주소와 선택용 데이터를 SSR로 렌더링한다")
  void addressInputRendersRecentAddresses() throws Exception {
    given(addressService.list(MEMBER_ID)).willReturn(List.of(savedAddress()));

    mockMvc
        .perform(get("/address-input").with(user(principal())))
        .andExpect(status().isOk())
        .andExpect(view().name("address-input"))
        .andExpect(model().attributeExists("addresses"))
        .andExpect(content().string(containsString("data-recent-address")))
        .andExpect(content().string(containsString("우리 집")))
        .andExpect(content().string(containsString("서울특별시 중구 세종대로 110")));
  }

  @Test
  @DisplayName("저장된 주소가 없으면 주소 관리 화면에 빈 상태를 렌더링한다")
  void addressManagementRendersEmptyState() throws Exception {
    given(addressService.list(MEMBER_ID)).willReturn(List.of());

    mockMvc
        .perform(get("/address-management").with(user(principal())))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("등록된 배송 주소가 없습니다.")));
  }

  @Test
  @DisplayName("주소 SSR 출력은 사용자 입력 HTML을 이스케이프한다")
  void addressManagementEscapesUserInput() throws Exception {
    Address unsafeAddress =
        Address.builder()
            .id(2L)
            .memberId(MEMBER_ID)
            .alias("<script>alert('xss')</script>")
            .address("서울")
            .build();
    given(addressService.list(MEMBER_ID)).willReturn(List.of(unsafeAddress));

    mockMvc
        .perform(get("/address-management").with(user(principal())))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("&lt;script&gt;")));
  }

  @Test
  @DisplayName("인증되지 않은 주소 화면 요청은 로그인 화면으로 이동한다")
  void unauthenticatedRequestRedirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/address-management"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));

    verify(addressService, never()).list(org.mockito.ArgumentMatchers.anyLong());
  }

  private CustomUserDetails principal() {
    return new CustomUserDetails(MEMBER_ID, "user@example.com", "encoded-password", "CUSTOMER");
  }

  private Address savedAddress() {
    return Address.builder()
        .id(1L)
        .memberId(MEMBER_ID)
        .alias("우리 집")
        .address("서울특별시 중구 세종대로 110")
        .detailAddress("101동 1001호")
        .pickupGuide("문 앞에 놓아주세요")
        .build();
  }
}
