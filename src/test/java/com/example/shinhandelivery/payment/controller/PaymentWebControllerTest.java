package com.example.shinhandelivery.payment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentWebControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("포인트 지갑 뷰 요청 시 point-wallet 뷰를 반환한다")
  void pointWalletReturnsView() throws Exception {
    mockMvc
        .perform(get("/point-wallet"))
        .andExpect(status().isOk())
        .andExpect(view().name("point-wallet"));
  }

  @Test
  @DisplayName("인증된 사용자의 포인트 지갑 뷰 요청 시 포인트 지갑 모델을 전달한다")
  void pointWalletWithAuthenticatedUserReturnsModel() throws Exception {
    Member seededMember = getOrCreateTestMember();
    CustomUserDetails customUser = new CustomUserDetails(seededMember);

    mockMvc
        .perform(get("/point-wallet").with(SecurityMockMvcRequestPostProcessors.user(customUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("point-wallet"))
        .andExpect(model().attributeExists("wallet"));
  }

  private Member getOrCreateTestMember() {
    return memberRepository.findAll().stream()
        .findFirst()
        .orElseGet(
            () ->
                memberRepository.save(
                    Member.builder()
                        .email("testwalletuser@shinhan.com")
                        .password("password123")
                        .name("지갑유저")
                        .phoneNumber("010-9999-8888")
                        .role(MemberRole.CUSTOMER)
                        .build()));
  }
}
