package com.example.shinhangaecheokja.category.service;

import com.example.shinhangaecheokja.category.dto.response.CategoryResponse;
import com.example.shinhangaecheokja.category.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Category 관련 유스케이스(목록 조회)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  /** 전체 카테고리 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<CategoryResponse> getCategories() {
    return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
  }
}
