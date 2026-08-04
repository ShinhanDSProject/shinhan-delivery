package com.example.shinhandelivery.delivery.repository;

import com.example.shinhandelivery.delivery.entity.Matching;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Matching 엔티티에 대한 JPA 저장소. */
public interface MatchingRepository extends JpaRepository<Matching, Long> {

  /** 해당 배송 요청에 연결된 매칭이 있는지 확인한다(배송 요청 삭제 시 FK 제약 위반을 막기 위해 사용). */
  boolean existsByDeliveryRequestId(Long deliveryRequestId);

  /** 배송 요청 id로 매칭을 조회한다(deliveryRequestId는 unique). */
  Optional<Matching> findByDeliveryRequestId(Long deliveryRequestId);
}
