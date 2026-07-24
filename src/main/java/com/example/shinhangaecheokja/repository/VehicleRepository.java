package com.example.shinhangaecheokja.repository;

import com.example.shinhangaecheokja.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

/** Vehicle 엔티티에 대한 JPA 저장소. */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {}
