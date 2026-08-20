package com.example.shinhandelivery.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.exception.GlobalExceptionHandler;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.member.constant.MemberValidationConstants;
import com.example.shinhandelivery.member.dto.request.LoginRequest;
import com.example.shinhandelivery.member.dto.request.MemberCreateRequest;
import com.example.shinhandelivery.member.dto.request.MemberFieldValidateRequest;
import com.example.shinhandelivery.member.dto.request.MemberPasswordUpdateRequest;
import com.example.shinhandelivery.member.dto.request.MemberProfileUpdateRequest;
import com.example.shinhandelivery.member.dto.request.RefreshTokenRequest;
import com.example.shinhandelivery.member.dto.response.MemberFieldValidateResponse;
import com.example.shinhandelivery.member.dto.response.TokenResponse;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.entity.MemberValidationField;
import com.example.shinhandelivery.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("회원 생성 요청을 받으면 생성된 회원을 반환한다")
  void createMemberSuccess() throws Exception {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("user@example.com");
    request.setPassword("password123!");
    request.setName("홍길동");
    request.setPhoneNumber("010-1234-5678");
    request.setRole(MemberRole.CUSTOMER);

    Member member =
        Member.builder()
            .id(1L)
            .email("user@example.com")
            .password("password123!")
            .name("홍길동")
            .phoneNumber("010-1234-5678")
            .role(MemberRole.CUSTOMER)
            .build();
    when(memberService.create(any())).thenReturn(member);

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
  @DisplayName("회원 필드 유효성 검증 API 호출 시 200 OK와 검증 결과를 반환한다")
  void validateFieldShouldReturnOk() throws Exception {
    MemberFieldValidateRequest request =
        new MemberFieldValidateRequest(MemberValidationField.EMAIL, "user@example.com");
    MemberFieldValidateResponse response =
        MemberFieldValidateResponse.ok(MemberValidationConstants.MSG_EMAIL_SUCCESS);

    when(memberService.validateField(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/members/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true))
        .andExpect(jsonPath("$.message").value(MemberValidationConstants.MSG_EMAIL_SUCCESS));
  }

  @Test
  @DisplayName("존재하지 않는 회원을 조회하면 404를 반환한다")
  void getMemberNotFoundShouldReturn404() throws Exception {
    when(memberService.getById(eq(999L)))
        .thenThrow(new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    mockMvc.perform(get("/api/v1/members/999")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유효하지 않은 이메일로 회원 생성 요청시 400을 반환한다")
  void createMemberInvalidEmailShouldReturn400() throws Exception {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("invalid-email-format");
    request.setPassword("password123!");
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
  @DisplayName("비밀번호가 8자 미만이면 회원 생성 요청시 400을 반환한다")
  void createMemberShortPasswordShouldReturn400() throws Exception {
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
  @DisplayName("필수 입력값이 누락되면 회원 생성 요청시 400을 반환한다")
  void createMemberMissingFieldShouldReturn400() throws Exception {
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
  @DisplayName("인증된 사용자의 내 프로필을 정상 조회한다")
  void getMyProfileSuccess() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    Member member =
        Member.builder()
            .id(10L)
            .email("my@example.com")
            .name("홍길동")
            .phoneNumber("010-1234-5678")
            .role(MemberRole.CUSTOMER)
            .build();

    when(memberService.getMyProfile(eq(10L))).thenReturn(member);

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
  @DisplayName("인증된 사용자의 내 프로필을 성공적으로 수정한다")
  void updateMyProfileSuccess() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    MemberProfileUpdateRequest request = new MemberProfileUpdateRequest("김철수", "010-9876-5432");

    Member updatedMember =
        Member.builder()
            .id(10L)
            .email("my@example.com")
            .name("김철수")
            .phoneNumber("010-9876-5432")
            .role(MemberRole.CUSTOMER)
            .build();

    when(memberService.updateMyProfile(eq(10L), any())).thenReturn(updatedMember);

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
  @DisplayName("인증된 사용자가 비밀번호를 변경하면 204를 반환한다")
  void updatePasswordSuccess() throws Exception {
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
  @DisplayName("새 비밀번호가 복잡도 규칙을 지키지 않으면 400을 반환한다")
  void updatePasswordSimplePasswordShouldReturn400() throws Exception {
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
  @DisplayName("유효하지 않은 전화번호로 프로필 수정시 400을 반환한다")
  void updateMyProfileInvalidPhoneShouldReturn400() throws Exception {
    CustomUserDetails customUser = new CustomUserDetails(10L, "my@example.com", "pass", "CUSTOMER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);

    MemberProfileUpdateRequest request = new MemberProfileUpdateRequest("김철수", "invalid-phone");

    mockMvc
        .perform(
            patch("/api/v1/members/me")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("인증되지 않은 사용자가 내 프로필 조회시 401을 반환한다")
  void getMyProfileUnauthenticatedShouldReturn401() throws Exception {
    mockMvc.perform(get("/api/v1/members/me")).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("로그인 요청 성공 시 JWT 토큰 응답과 함께 HttpOnly Set-Cookie 헤더를 반환한다")
  void loginSuccessSetsCookies() throws Exception {
    LoginRequest request = new LoginRequest("user@example.com", "password123");
    TokenResponse tokenResponse = TokenResponse.of("access.token.jwt", "refresh.token.jwt");

    when(memberService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

    mockMvc
        .perform(
            post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access.token.jwt"))
        .andExpect(jsonPath("$.refreshToken").value("refresh.token.jwt"))
        .andExpect(cookie().value("accessToken", "access.token.jwt"))
        .andExpect(cookie().httpOnly("accessToken", true))
        .andExpect(cookie().value("refreshToken", "refresh.token.jwt"))
        .andExpect(cookie().httpOnly("refreshToken", true));
  }

  @Test
  @DisplayName("리프레시 토큰 쿠키로 재발급 요청 시 새 토큰과 갱신된 Set-Cookie 헤더를 반환한다")
  void refreshSuccessWithCookie() throws Exception {
    TokenResponse tokenResponse = TokenResponse.of("new.access.token", "new.refresh.token");

    when(memberService.refresh(eq("valid.refresh.token"))).thenReturn(tokenResponse);

    mockMvc
        .perform(
            post("/api/v1/members/refresh")
                .cookie(new Cookie("refreshToken", "valid.refresh.token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new.access.token"))
        .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"))
        .andExpect(cookie().value("accessToken", "new.access.token"))
        .andExpect(cookie().httpOnly("accessToken", true))
        .andExpect(cookie().value("refreshToken", "new.refresh.token"))
        .andExpect(cookie().httpOnly("refreshToken", true));
  }

  @Test
  @DisplayName("리프레시 토큰 본문(Body)으로 재발급 요청 시 새 토큰과 갱신된 Set-Cookie 헤더를 반환한다")
  void refreshSuccessWithRequestBody() throws Exception {
    RefreshTokenRequest request = new RefreshTokenRequest("body.refresh.token");
    TokenResponse tokenResponse = TokenResponse.of("new.access.token", "new.refresh.token");

    when(memberService.refresh(eq("body.refresh.token"))).thenReturn(tokenResponse);

    mockMvc
        .perform(
            post("/api/v1/members/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new.access.token"))
        .andExpect(cookie().value("accessToken", "new.access.token"));
  }

  @Test
  @DisplayName("로그아웃 요청 시 204 No Content와 만료된 Set-Cookie(Max-Age=0)를 반환한다")
  void logoutSuccessClearsCookies() throws Exception {
    mockMvc
        .perform(post("/api/v1/members/logout"))
        .andExpect(status().isNoContent())
        .andExpect(cookie().maxAge("accessToken", 0))
        .andExpect(cookie().maxAge("refreshToken", 0));
  }
}
