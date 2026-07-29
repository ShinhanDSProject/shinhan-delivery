package com.example.shinhangaecheokja.category.repository;

import com.example.shinhangaecheokja.category.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Category 엔티티에 대한 JPA 저장소. */
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /** id 오름차순으로 전체 카테고리를 조회한다(목록 노출 순서 고정). */
  List<Category> findAllByOrderByIdAsc();
}
