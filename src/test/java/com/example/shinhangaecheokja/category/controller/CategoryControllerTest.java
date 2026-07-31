package com.example.shinhangaecheokja.category.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shinhangaecheokja.category.entity.Category;
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
    Category c1 = new Category();
    c1.setId(1L);
    c1.setName("전자기기/가전");
    Category c2 = new Category();
    c2.setId(2L);
    c2.setName("식품/음료");
    Category c3 = new Category();
    c3.setId(3L);
    c3.setName("의류/패션잡화");

    when(categoryService.getCategories()).thenReturn(List.of(c1, c2, c3));

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
