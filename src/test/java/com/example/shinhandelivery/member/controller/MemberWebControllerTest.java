package com.example.shinhandelivery.member.controller;

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
class MemberWebControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("마이페이지 뷰 요청 시 my-page 뷰를 반환한다")
  void myPageReturnsView() throws Exception {
    mockMvc.perform(get("/my-page")).andExpect(status().isOk()).andExpect(view().name("my-page"));
  }

  @Test
  @DisplayName("인증된 사용자의 마이페이지 뷰 요청 시 회원 프로필 모델을 전달한다")
  void myPageWithAuthenticatedUserReturnsModel() throws Exception {
    Member seededMember = getOrCreateTestMember();
    CustomUserDetails customUser = new CustomUserDetails(seededMember);

    mockMvc
        .perform(get("/my-page").with(SecurityMockMvcRequestPostProcessors.user(customUser)))
        .andExpect(status().isOk())
        .andExpect(view().name("my-page"))
        .andExpect(model().attributeExists("member"));
  }

  private Member getOrCreateTestMember() {
    return memberRepository.findAll().stream()
        .findFirst()
        .orElseGet(
            () ->
                memberRepository.save(
                    Member.builder()
                        .email("testuser@shinhan.com")
                        .password("password123")
                        .name("테스트유저")
                        .phoneNumber("010-1234-5678")
                        .role(MemberRole.CUSTOMER)
                        .build()));
  }

  @Test
  @DisplayName("로그인 뷰 요청 시 login 뷰를 반환한다")
  void loginReturnsView() throws Exception {
    mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
  }
}
