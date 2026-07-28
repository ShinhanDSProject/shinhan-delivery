package com.example.shinhangaecheokja.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청과 차량을 연결하는 매칭 엔티티. delivery_request_id/vehicle_id는 각각 FK 값이다. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "matching")
public class Matching {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "delivery_request_id", nullable = false, unique = true)
  private Long deliveryRequestId;

  @Column(name = "vehicle_id", nullable = false)
  private Long vehicleId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MatchingStatus status;

  @Column(name = "matched_at", nullable = false)
  private LocalDateTime matchedAt;
}
