package com.example.shinhangaecheokja.delivery.repository;

import com.example.shinhangaecheokja.delivery.entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;

/** Matching 엔티티에 대한 JPA 저장소. */
public interface MatchingRepository extends JpaRepository<Matching, Long> {

  /** 해당 배송 요청이 이미 매칭되었는지 확인한다. */
  boolean existsByDeliveryRequestId(Long deliveryRequestId);
}
