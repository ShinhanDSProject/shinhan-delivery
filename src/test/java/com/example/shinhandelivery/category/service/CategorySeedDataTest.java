package com.example.shinhandelivery.category.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.category.entity.Category;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** V8 마이그레이션이 실제로 카테고리 12종을 정확히 시딩했는지 실제 DB로 검증한다(Mockito로는 잡을 수 없는 마이그레이션 오타/누락 방지). */
@SpringBootTest
class CategorySeedDataTest {

  @Autowired private CategoryService categoryService;

  @Test
  @DisplayName("마이그레이션이 카테고리 12종을 시딩한다")
  void migrationShouldSeed12Categories() {
    List<Category> categories = categoryService.list();

    assertThat(categories).hasSize(12);
    assertThat(categories)
        .extracting(Category::getName)
        .containsExactlyInAnyOrder(
            "전자기기/가전", "식품/음료", "의류/패션잡화", "서류/문서", "생활용품/잡화", "가구/인테리어", "화장품/뷰티", "도서/음반",
            "스포츠/레저", "반려동물 용품", "꽃/식물", "기타");
  }
}
