package com.example.shinhandelivery.category.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.shinhandelivery.category.entity.Category;
import com.example.shinhandelivery.category.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HomeWebControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CategoryService categoryService;

  @Test
  @DisplayName("홈 SSR 페이지 요청 시 home 뷰와 카테고리 모델 데이터를 반환한다")
  void homeReturnsViewAndModel() throws Exception {
    Category category = new Category();
    category.setId(1L);
    category.setName("식품/음료");
    given(categoryService.list()).willReturn(List.of(category));

    mockMvc
        .perform(get("/home"))
        .andExpect(status().isOk())
        .andExpect(view().name("home"))
        .andExpect(model().attributeExists("categories"));
  }
}
