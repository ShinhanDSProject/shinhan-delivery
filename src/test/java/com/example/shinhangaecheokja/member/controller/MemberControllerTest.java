package com.example.shinhangaecheokja.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.common.exception.GlobalExceptionHandler;
import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberPasswordUpdateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberProfileUpdateRequestDto;
import com.example.shinhangaecheokja.member.dto.response.MemberProfileResponseDto;
import com.example.shinhangaecheokja.member.dto.response.MemberResponse;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.service.MemberService;
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
class MemberControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private MemberService memberService;
  @InjectMocks private MemberController memberController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(memberController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void 회원_생성_요청을_받으면_생성된_회원을_반환한다() throws Exception {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("user@example.com");
    request.setPassword("password123");
    request.setName("홍길동");
    request.setPhoneNumber("010-1234-5678");
    request.setRole(MemberRole.CUSTOMER);

    when(memberService.createMember(any()))
        .thenReturn(
            new MemberResponse(
                1L, "user@example.com", "홍길동", "010-1234-5678", MemberRole.CUSTOMER));

    mockMvc
        .perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("user@example.com"))
        .andExpect(jsonPath("$.role").value("CUSTOMER"));
  }

  @Test
  void 존재하지_않는_회원을_조회하면_404를_반환한다() throws Exception {
    when(memberService.getMember(eq(999L)))
        .thenThrow(new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    mockMvc.perform(get("/api/v1/members/999")).andExpect(status().isNotFound());
  }

  @Test
  void 유효하지_않은_이메일로_회원_생성_요청시_400을_반환한다() throws Exception {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("invalid-email-format");
    request.setPassword("password123");
    request.setName("홍길동");
    request.setPhoneNumber("010-1234-5678");

    mockMvc
        .perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 비밀번호가_8자_미만이면_회원_생성_요청시_400을_반환한다() throws Exception {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("user@example.com");
    request.setPassword("short");
    request.setName("홍길동");
    request.setPhoneNumber("010-1234-5678");

    mockMvc
        .perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 필수_입력값이_누락되면_회원_생성_요청시_400을_반환한다() throws Exception {
    MemberCreateRequest request = new MemberCreateRequest();
    // email, password, name, phoneNumber 누락

    mockMvc
        .perform(
            post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 인증된_사용자의_내_프로필을_정상_조회한다() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    MemberProfileResponseDto response =
        new MemberProfileResponseDto(
            10L, "my@example.com", "홍길동", "010-1234-5678", MemberRole.CUSTOMER);

    when(memberService.getMyProfile(eq(10L))).thenReturn(response);

    mockMvc
        .perform(get("/api/v1/members/me").principal(auth))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.email").value("my@example.com"))
        .andExpect(jsonPath("$.name").value("홍길동"))
        .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678"))
        .andExpect(jsonPath("$.role").value("CUSTOMER"));
  }

  @Test
  void 인증된_사용자의_내_프로필을_성공적으로_수정한다() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    MemberProfileUpdateRequestDto request =
        new MemberProfileUpdateRequestDto("김철수", "010-9876-5432");
    MemberProfileResponseDto response =
        new MemberProfileResponseDto(
            10L, "my@example.com", "김철수", "010-9876-5432", MemberRole.CUSTOMER);

    when(memberService.updateMyProfile(eq(10L), any())).thenReturn(response);

    mockMvc
        .perform(
            patch("/api/v1/members/me")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("김철수"))
        .andExpect(jsonPath("$.phoneNumber").value("010-9876-5432"));
  }

  @Test
  void 인증된_사용자가_비밀번호를_변경하면_204를_반환한다() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    MemberPasswordUpdateRequest request =
        new MemberPasswordUpdateRequest("OldPassword1!", "NewPassword2@", "NewPassword2@");

    mockMvc
        .perform(
            patch("/api/v1/members/password")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(memberService).updatePassword(eq(10L), any(MemberPasswordUpdateRequest.class));
  }

  @Test
  void 새_비밀번호가_복잡도_규칙을_지키지_않으면_400을_반환한다() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    MemberPasswordUpdateRequest request =
        new MemberPasswordUpdateRequest("OldPassword1!", "passwordonly", "passwordonly");

    mockMvc
        .perform(
            patch("/api/v1/members/password")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 유효하지_않은_전화번호로_프로필_수정시_400을_반환한다() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    MemberProfileUpdateRequestDto request =
        new MemberProfileUpdateRequestDto("김철수", "invalid-phone");

    mockMvc
        .perform(
            patch("/api/v1/members/me")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void 인증되지_않은_사용자가_내_프로필_조회시_401을_반환한다() throws Exception {
    mockMvc.perform(get("/api/v1/members/me")).andExpect(status().isUnauthorized());
  }
}
