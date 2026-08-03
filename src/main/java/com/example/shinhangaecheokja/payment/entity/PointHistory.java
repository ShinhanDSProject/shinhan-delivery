package com.example.shinhangaecheokja.payment.entity;

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

/** 포인트 충전·차감 결과를 기록하는 이력 엔티티. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "point_history")
public class PointHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "wallet_id", nullable = false)
  private Long walletId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PointHistoryType type;

  @Column(nullable = false)
  private long amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", length = 30)
  private PaymentMethod paymentMethod;

  @Column(name = "idempotency_key", length = 36)
  private String idempotencyKey;

  @Column(name = "balance_after", nullable = false)
  private long balanceAfter;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
