package com.example.shinhangaecheokja.category.dto.response;

import com.example.shinhangaecheokja.category.entity.Category;

/** 물품 카테고리 응답 DTO. */
public record CategoryResponse(Long id, String name) {

  /** Category 엔티티를 응답 DTO로 변환한다. */
  public static CategoryResponse from(Category entity) {
    return new CategoryResponse(entity.getId(), entity.getName());
  }
}
