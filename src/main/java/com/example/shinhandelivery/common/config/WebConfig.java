package com.example.shinhandelivery.common.config;

import com.example.shinhandelivery.common.resolver.CurrentUserIdArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 루트 경로("/")에 매핑된 정적 리소스가 없어 {@code NoResourceFoundException}이 나던 것을, 홈 화면으로 리다이렉트하도록 고정한다. */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(currentUserIdArgumentResolver);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/home");
    registry.addRedirectViewController("/index.html", "/home");
    registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html");
    registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html");
  }
}
