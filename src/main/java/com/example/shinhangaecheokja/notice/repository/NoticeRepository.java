package com.example.shinhangaecheokja.notice.repository;

import com.example.shinhangaecheokja.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** 공지사항 엔티티 데이터 액세스를 처리하는 Repository 인터페이스입니다. */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

  /** 공지사항 목록을 상단 고정 및 작성일 최신순으로 페이징 조회합니다. */
  Page<Notice> findAllByOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);

  /** 카테고리별 공지사항 목록을 상단 고정 및 작성일 최신순으로 페이징 조회합니다. */
  Page<Notice> findByCategoryOrderByIsPinnedDescCreatedAtDesc(String category, Pageable pageable);
}
