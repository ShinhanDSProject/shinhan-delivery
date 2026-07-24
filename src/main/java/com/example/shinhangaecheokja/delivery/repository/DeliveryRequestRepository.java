package com.example.shinhangaecheokja.delivery.repository;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import org.springframework.data.jpa.repository.JpaRepository;

/** DeliveryRequest 엔티티에 대한 JPA 저장소. */
public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Long> {}
