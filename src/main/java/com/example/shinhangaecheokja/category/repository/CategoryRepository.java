package com.example.shinhangaecheokja.category.repository;

import com.example.shinhangaecheokja.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/** Category 엔티티에 대한 JPA 저장소. */
public interface CategoryRepository extends JpaRepository<Category, Long> {}
