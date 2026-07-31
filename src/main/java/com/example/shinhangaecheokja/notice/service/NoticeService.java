package com.example.shinhangaecheokja.notice.service;

import com.example.shinhangaecheokja.notice.entity.Notice;
import com.example.shinhangaecheokja.notice.exception.NoticeNotFoundException;
import com.example.shinhangaecheokja.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 공지사항 목록 및 상세 조회 비즈니스 로직을 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

  private final NoticeRepository noticeRepository;

  /** 공지사항 목록을 페이징하여 조회합니다. 카테고리가 지정된 경우 해당 카테고리만 필터링합니다. */
  public Page<Notice> getNotices(String category, Pageable pageable) {
    if (category != null && !category.isBlank()) {
      return noticeRepository.findByCategoryOrderByIsPinnedDescCreatedAtDesc(category, pageable);
    }
    return noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc(pageable);
  }

  /** 공지사항 단건 상세 정보를 조회합니다. */
  public Notice getNoticeDetail(Long noticeId) {
    return noticeRepository.findById(noticeId).orElseThrow(NoticeNotFoundException::new);
  }
}
