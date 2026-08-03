package com.example.shinhangaecheokja.vehicle.repository;

import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Vehicle 엔티티에 대한 JPA 저장소. */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  /** 특정 배송원(owner)의 차량 목록을 조회한다. */
  List<Vehicle> findAllByOwnerId(Long ownerId);

  /** 차량 배정(매칭) 시 동시성 제어를 위해 비관적 쓰기 락으로 차량을 조회한다. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select v from Vehicle v where v.id = :id")
  Optional<Vehicle> findByIdForUpdate(@Param("id") Long id);
}
