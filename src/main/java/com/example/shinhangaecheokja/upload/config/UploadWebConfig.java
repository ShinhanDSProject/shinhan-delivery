package com.example.shinhangaecheokja.upload.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** {@code app.upload.dir}에 저장된 파일을 {@code app.upload.base-url} 경로로 정적 서빙하는 설정. */
@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

  @Value("${app.upload.dir}")
  private String uploadDir;

  @Value("${app.upload.base-url}")
  private String baseUrl;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String absolutePath = Path.of(uploadDir).toAbsolutePath().normalize().toString();
    String location = absolutePath.endsWith("/") ? absolutePath : absolutePath + "/";
    registry.addResourceHandler(baseUrl + "/**").addResourceLocations("file:" + location);
  }
}
