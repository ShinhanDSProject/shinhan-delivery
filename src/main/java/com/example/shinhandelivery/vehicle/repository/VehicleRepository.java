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
}
