package com.example.shinhandelivery.payment.repository;

import com.example.shinhandelivery.payment.entity.PointWallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** PointWallet 엔티티에 대한 JPA 저장소. */
public interface PaymentRepository extends JpaRepository<PointWallet, Long> {

  /** 포인트 충전/차감 시 동시성 제어를 위해 비관적 쓰기 락으로 포인트 지갑을 조회한다. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select w from PointWallet w where w.id = :id")
  Optional<PointWallet> findByIdForUpdate(@Param("id") Long id);

  Optional<PointWallet> findByMemberId(Long memberId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select w from PointWallet w where w.memberId = :memberId")
  Optional<PointWallet> findByMemberIdForUpdate(@Param("memberId") Long memberId);
}
