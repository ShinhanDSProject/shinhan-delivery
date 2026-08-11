package com.example.shinhandelivery.courier.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.common.security.CustomUserDetails;
import com.example.shinhandelivery.courier.dto.response.CourierHomePageResponse;
import com.example.shinhandelivery.courier.service.CourierHomeService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.RedirectView;

@ExtendWith(MockitoExtension.class)
class CourierWebControllerTest {

  private MockMvc mockMvc;

  @Mock private CourierHomeService courierHomeService;
  @InjectMocks private CourierWebController courierWebController;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(courierWebController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .setViewResolvers(
                (viewName, locale) ->
                    viewName.startsWith("redirect:")
                        ? new RedirectView(viewName.substring("redirect:".length()))
                        : (model, request, response) -> {})
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("미인증 사용자가 /courier-home 접근 시 /courier-login 으로 리다이렉트한다")
  void courierHomeUnauthenticatedRedirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/courier-home"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/courier-login"));
  }

  @Test
  @DisplayName("인증된 배송원이 /courier-home 접근 시 courier-home 뷰와 모델 데이터를 반환한다")
  void courierHomeReturnsViewAndModelForAuthenticatedCourier() throws Exception {
    Authentication auth = authenticationFor(1L);
    CourierHomePageResponse response =
        new CourierHomePageResponse(
            "박배송", "🛵 오토바이", "ONLINE", true, 37.5665, 126.9780, List.of(), 0L);

    when(courierHomeService.load(1L)).thenReturn(response);

    mockMvc
        .perform(get("/courier-home").principal(auth))
        .andExpect(status().isOk())
        .andExpect(view().name("courier-home"))
        .andExpect(model().attribute("memberName", "박배송"))
        .andExpect(model().attribute("transportMode", "🛵 오토바이"))
        .andExpect(model().attribute("workStatus", "ONLINE"))
        .andExpect(model().attribute("isOnline", true))
        .andExpect(model().attribute("availableCount", 0L));
  }

  private Authentication authenticationFor(Long memberId) {
    CustomUserDetails principal =
        new CustomUserDetails(memberId, "courier@example.com", "encoded-pw", "COURIER");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
    return auth;
  }
}
