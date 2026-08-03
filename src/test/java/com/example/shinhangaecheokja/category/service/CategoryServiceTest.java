package com.example.shinhangaecheokja.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.category.entity.Category;
import com.example.shinhangaecheokja.category.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @InjectMocks private CategoryService categoryService;

  @Test
  @DisplayName("저장된 카테고리 목록을 전부 반환한다")
  void getCategoriesSuccess() {
    List<Category> categories =
        List.of(newCategory(1L, "전자기기/가전"), newCategory(2L, "식품/음료"), newCategory(3L, "의류/패션잡화"));
    when(categoryRepository.findAllByOrderByIdAsc()).thenReturn(categories);

    List<Category> responses = categoryService.list();

    assertThat(responses).hasSize(3);
    assertThat(responses)
        .extracting(Category::getName)
        .containsExactly("전자기기/가전", "식품/음료", "의류/패션잡화");
  }

  @Test
  @DisplayName("저장된 카테고리가 없으면 빈 목록을 반환한다")
  void getCategoriesEmptyShouldReturnEmptyList() {
    when(categoryRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

    List<Category> responses = categoryService.list();

    assertThat(responses).isEmpty();
  }

  private Category newCategory(Long id, String name) {
    Category category = new Category();
    category.setId(id);
    category.setName(name);
    return category;
  }
}
