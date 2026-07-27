package com.example.shinhangaecheokja.vehicle.repository;

import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Vehicle 엔티티에 대한 JPA 저장소. */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

  /** 주어진 상태이면서 무게·거리를 모두 감당할 수 있는 차량 목록을 조회한다. */
  List<Vehicle> findByStatusAndMaxWeightGreaterThanEqualAndMaxDistanceGreaterThanEqual(
      VehicleStatus status, double maxWeight, double maxDistance);
}
