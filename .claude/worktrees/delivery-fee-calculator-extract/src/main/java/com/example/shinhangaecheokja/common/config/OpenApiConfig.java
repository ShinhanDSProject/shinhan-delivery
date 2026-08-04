package com.example.shinhandelivery.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Shinhan Delivery 배송 서비스 API 명세서")
                .description(
                    "Shinhan Delivery 프로젝트의 회원가입, 차량 관리, 배송 요청, 매칭 서비스 및 결제 지갑 연동 API 자동화 문서입니다. 개발을 공부하는 교육생분들의 테스트 환경을 돕기 위해 Swagger UI가 활성화되어 있습니다.")
                .version("1.0.0"));
  }
}
