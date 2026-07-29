package com.example.shinhangaecheokja.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.category.dto.response.CategoryResponse;
import com.example.shinhangaecheokja.category.entity.Category;
import com.example.shinhangaecheokja.category.repository.CategoryRepository;
import java.util.List;
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
  void 저장된_카테고리_목록을_전부_반환한다() {
    List<Category> categories =
        List.of(newCategory(1L, "전자기기/가전"), newCategory(2L, "식품/음료"), newCategory(3L, "의류/패션잡화"));
    when(categoryRepository.findAll()).thenReturn(categories);

    List<CategoryResponse> responses = categoryService.getCategories();

    assertThat(responses).hasSize(3);
    assertThat(responses)
        .extracting(CategoryResponse::name)
        .containsExactly("전자기기/가전", "식품/음료", "의류/패션잡화");
  }

  @Test
  void 저장된_카테고리가_없으면_빈_목록을_반환한다() {
    when(categoryRepository.findAll()).thenReturn(List.of());

    List<CategoryResponse> responses = categoryService.getCategories();

    assertThat(responses).isEmpty();
  }

  private Category newCategory(Long id, String name) {
    Category category = new Category();
    category.setId(id);
    category.setName(name);
    return category;
  }
}
