package com.example.shinhandelivery.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = "jwt.member.secret=test-jwt-secret-key-for-application-context-very-long-secret")
@AutoConfigureMockMvc
class SwaggerUiTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Swagger UI 접속 테스트 - /swagger-ui")
  void swaggerUiShortTest() throws Exception {
    mockMvc.perform(get("/swagger-ui")).andDo(print()).andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("Swagger UI 접속 테스트 - /swagger-ui/")
  void swaggerUiSlashTest() throws Exception {
    mockMvc.perform(get("/swagger-ui/")).andDo(print()).andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("Swagger UI 접속 테스트 - /swagger-ui/index.html")
  void swaggerUiIndexTest() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html")).andDo(print()).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Swagger UI 접속 테스트 - /swagger-ui.html")
  void swaggerUiHtmlTest() throws Exception {
    mockMvc.perform(get("/swagger-ui.html")).andDo(print()).andExpect(status().is3xxRedirection());
  }

  @Test
  @DisplayName("OpenAPI spec 접속 테스트 - /v3/api-docs")
  void apiDocsTest() throws Exception {
    mockMvc.perform(get("/v3/api-docs")).andDo(print()).andExpect(status().isOk());
  }

  @Test
  @DisplayName("OpenAPI config 접속 테스트 - /v3/api-docs/swagger-config")
  void apiDocsConfigTest() throws Exception {
    mockMvc.perform(get("/v3/api-docs/swagger-config")).andDo(print()).andExpect(status().isOk());
  }
}
