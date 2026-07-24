package com.example.shinhangaecheokja.repository;

import com.example.shinhangaecheokja.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

/** Vehicle 엔티티에 대한 JPA 저장소. */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  /** 주어진 무게·거리를 모두 감당할 수 있는 차량이 하나라도 있는지 확인한다. */
  boolean existsByMaxWeightGreaterThanEqualAndMaxDistanceGreaterThanEqual(
      double maxWeight, double maxDistance);
}
