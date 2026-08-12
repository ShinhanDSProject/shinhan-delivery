package com.example.shinhandelivery.delivery.repository;

import com.example.shinhandelivery.delivery.entity.Matching;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Matching 엔티티에 대한 JPA 저장소. */
public interface MatchingRepository extends JpaRepository<Matching, Long> {

  /** 해당 배송 요청에 연결된 매칭이 있는지 확인한다(배송 요청 삭제 시 FK 제약 위반을 막기 위해 사용). */
  boolean existsByDeliveryRequestId(Long deliveryRequestId);

  /** 배송 요청 id로 매칭을 조회한다(deliveryRequestId는 unique). */
  Optional<Matching> findByDeliveryRequestId(Long deliveryRequestId);

  /** 고객 취소와 매칭 상태 변경 경합을 막기 위해 배송 요청 기준으로 매칭 행을 잠근다. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Matching m where m.deliveryRequestId = :deliveryRequestId")
  Optional<Matching> findByDeliveryRequestIdForUpdate(
      @Param("deliveryRequestId") Long deliveryRequestId);
}
