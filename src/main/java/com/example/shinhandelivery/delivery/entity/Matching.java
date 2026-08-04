package com.example.shinhandelivery.delivery.entity;

import com.example.shinhandelivery.delivery.dto.request.MatchingCreateRequest;
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

  /** MatchingCreateRequest DTO 기반으로 MATCHED 상태의 Matching 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Matching from(MatchingCreateRequest request) {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(request.getDeliveryRequestId());
    matching.setVehicleId(request.getVehicleId());
    matching.setStatus(MatchingStatus.MATCHED);
    matching.setMatchedAt(LocalDateTime.now());
    return matching;
  }

  /** 매칭 상태를 변경하는 도메인 비즈니스 메서드. */
  public Matching changeStatus(MatchingStatus newStatus) {
    this.status = newStatus;
    return this;
  }
}
