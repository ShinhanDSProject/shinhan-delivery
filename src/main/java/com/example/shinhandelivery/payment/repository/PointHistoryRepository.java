package com.example.shinhandelivery.payment.repository;

import com.example.shinhandelivery.payment.entity.PointHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 포인트 충전/사용 이력 저장소. */
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

  Optional<PointHistory> findByMemberIdAndIdempotencyKey(Long memberId, String idempotencyKey);
}
