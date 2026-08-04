package com.example.shinhangaecheokja.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 루트 경로("/")에 매핑된 정적 리소스가 없어 {@code NoResourceFoundException}이 나던 것을, 홈 화면으로 리다이렉트하도록 고정한다. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/home.html");
  }
}
