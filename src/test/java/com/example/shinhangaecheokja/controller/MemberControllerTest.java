package com.example.shinhangaecheokja.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.dto.response.MemberResponse;
import com.example.shinhangaecheokja.entity.MemberRole;
import com.example.shinhangaecheokja.exception.MemberNotFoundException;
import com.example.shinhangaecheokja.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MemberService memberService;

  @Test
  void 회원_생성_요청을_받으면_생성된_회원을_반환한다() throws Exception {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("user@example.com");
    request.setPassword("password123");
    request.setName("홍길동");
    request.setPhoneNumber("010-1234-5678");
    request.setRole(MemberRole.CUSTOMER);

    when(memberService.createMember(any())).thenReturn(
        new MemberResponse(1L, "user@example.com", "홍길동", "010-1234-5678", MemberRole.CUSTOMER));

    mockMvc
        .perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("user@example.com"))
        .andExpect(jsonPath("$.role").value("CUSTOMER"));
  }

  @Test
  void 존재하지_않는_회원을_조회하면_404를_반환한다() throws Exception {
    when(memberService.getMember(eq(999L))).thenThrow(new MemberNotFoundException(999L));

    mockMvc.perform(get("/api/members/999")).andExpect(status().isNotFound());
  }
}
