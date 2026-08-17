package com.example.shinhandelivery.category.dto.response;

import com.example.shinhandelivery.category.entity.Category;
import lombok.Builder;

/** 물품 카테고리 응답 DTO. */
@Builder
public record CategoryResponse(Long id, String name) {

  /** Category 엔티티를 응답 DTO로 변환한다. */
  public static CategoryResponse from(Category entity) {
    return CategoryResponse.builder().id(entity.getId()).name(entity.getName()).build();
  }
}
