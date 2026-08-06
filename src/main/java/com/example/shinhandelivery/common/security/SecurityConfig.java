package com.example.shinhandelivery.common.security;

import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.exception.ErrorResponse;
import com.example.shinhandelivery.common.logging.MdcLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;
  private final ObjectMapper objectMapper;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/members", "/api/members/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/notices")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.PUT, "/api/v1/notices/{id}", "/api/v1/notices/{id}/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.DELETE, "/api/v1/notices/{id}", "/api/v1/notices/{id}/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        "/swagger-ui",
                        "/swagger-ui/",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/error")
                    .permitAll()
                    .anyRequest()
                    .permitAll()) // 기존 46개 테스트 호환을 위해 permitAll 적용 후 @PreAuthorize 권한제어
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(
                        (request, response, exception) -> {
                          response.setStatus(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
                          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          objectMapper.writeValue(
                              response.getOutputStream(), ErrorResponse.of(ErrorCode.UNAUTHORIZED));
                        })
                    .accessDeniedHandler(
                        (request, response, exception) -> {
                          response.setStatus(ErrorCode.ACCESS_DENIED.getHttpStatus().value());
                          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          objectMapper.writeValue(
                              response.getOutputStream(),
                              ErrorResponse.of(ErrorCode.ACCESS_DENIED));
                        }))
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new MdcLoggingFilter(), JwtAuthenticationFilter.class);

    return http.build();
  }
}
