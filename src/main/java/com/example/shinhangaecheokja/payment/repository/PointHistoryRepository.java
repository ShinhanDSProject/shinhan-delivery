package com.example.shinhangaecheokja.payment.repository;

import com.example.shinhangaecheokja.payment.entity.PointHistory;
import com.example.shinhangaecheokja.payment.entity.PointHistoryType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** PointHistory 엔티티에 대한 JPA 저장소. */
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

  Optional<PointHistory> findByWalletIdAndIdempotencyKey(Long walletId, String idempotencyKey);

  Optional<PointHistory> findTopByWalletIdAndTypeOrderByCreatedAtDesc(
      Long walletId, PointHistoryType type);

  long countByWalletIdAndType(Long walletId, PointHistoryType type);
}
