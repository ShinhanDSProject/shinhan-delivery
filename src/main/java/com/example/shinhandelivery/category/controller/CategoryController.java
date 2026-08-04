package com.example.shinhandelivery.category.controller;

import com.example.shinhandelivery.category.dto.response.CategoryResponse;
import com.example.shinhandelivery.category.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Category 목록 조회 API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  /** 물품 카테고리 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<CategoryResponse>> getCategories() {
    List<CategoryResponse> responses =
        categoryService.list().stream().map(CategoryResponse::from).toList();
    return ResponseEntity.ok(responses);
  }
}
