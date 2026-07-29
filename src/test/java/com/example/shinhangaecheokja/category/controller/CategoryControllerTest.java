package com.example.shinhangaecheokja.category.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.category.dto.response.CategoryResponse;
import com.example.shinhangaecheokja.category.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CategoryService categoryService;

  @Test
  void 카테고리_목록을_조회하면_배열로_직접_반환한다() throws Exception {
    when(categoryService.getCategories())
        .thenReturn(
            List.of(
                new CategoryResponse(1L, "전자기기/가전"),
                new CategoryResponse(2L, "식품/음료"),
                new CategoryResponse(3L, "의류/패션잡화")));

    mockMvc
        .perform(get("/api/v1/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].name").value("전자기기/가전"));
  }

  @Test
  void 카테고리가_없으면_빈_배열을_반환한다() throws Exception {
    when(categoryService.getCategories()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/v1/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
