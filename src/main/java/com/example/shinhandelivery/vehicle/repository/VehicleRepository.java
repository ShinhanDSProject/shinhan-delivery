package com.example.shinhandelivery.vehicle.repository;

import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Vehicle 엔티티에 대한 JPA 저장소. */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  /** 차량 배정(매칭) 시 동시성 제어를 위해 비관적 쓰기 락으로 차량을 조회한다. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select v from Vehicle v where v.id = :id")
  Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);

  /** 주어진 상태이면서 무게·거리를 모두 그 값 이상으로 감당 가능한 차량 목록을 조회한다(신규 배송요청의 오퍼 후보용). */
  List<Vehicle> findByStatusAndMaxWeightGreaterThanEqualAndMaxDistanceGreaterThanEqual(
      VehicleStatus status, double weight, double distance);

  List<Vehicle> findAllByMemberId(Long memberId);

  List<Vehicle> findAllByMemberIdOrderByIdDesc(Long memberId);

  Optional<Vehicle> findByMemberIdAndIsActiveTrue(Long memberId);

  /**
   * 활성화된(isActive=true) 차량의 id만 프로젝션으로 조회한다. Vehicle 엔티티를 영속성 컨텍스트에 올리지 않아, 같은 트랜잭션에서 뒤이어 {@code
   * findByIdForUpdate}로 비관적 락을 걸 때 1차 캐시된 옛 값을 돌려받는 문제를 피할 수 있다.
   */
  @Query("select v.id from Vehicle v where v.memberId = :memberId and v.isActive = true")
  Optional<Long> findActiveVehicleIdByMemberId(@Param("memberId") Long memberId);

  /** 회원(배송원) 정보까지 한 번의 DB 쿼리로 조인 조회하는 Fetch Join 메서드. */
  @Query("select v from Vehicle v join fetch v.member where v.memberId = :memberId")
  List<Vehicle> findAllByMemberIdWithMember(@Param("memberId") Long memberId);
}
