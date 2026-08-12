package com.example.shinhandelivery.payment.entity;

import com.example.shinhandelivery.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 포인트 충전/사용 이력 엔티티. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "point_history")
public class PointHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private Member member;

  @Column(name = "wallet_id", nullable = false)
  private Long walletId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
  private PointWallet wallet;

  @Column(nullable = false)
  private long amount;

  @Column(name = "balance_after", nullable = false)
  private long balanceAfter;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PointHistoryType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", length = 30)
  private PaymentMethod paymentMethod;

  @Column(name = "idempotency_key", nullable = false, length = 100)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "reference_id")
  private Long referenceId;

  @Column(length = 100)
  private String description;

  /** 포인트 충전 이력을 생성하는 정적 팩토리 메서드. */
  public static PointHistory ofCharge(
      Long memberId,
      Long walletId,
      long amount,
      long balanceAfter,
      PaymentMethod paymentMethod,
      String idempotencyKey) {
    return PointHistory.builder()
        .memberId(memberId)
        .walletId(walletId)
        .amount(amount)
        .balanceAfter(balanceAfter)
        .type(PointHistoryType.CHARGE)
        .paymentMethod(paymentMethod)
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .build();
  }

  /** 포인트 차감/사용 이력을 생성하는 정적 팩토리 메서드. */
  public static PointHistory ofUse(
      Long memberId, Long walletId, long amount, long balanceAfter, String idempotencyKey) {
    return PointHistory.builder()
        .memberId(memberId)
        .walletId(walletId)
        .amount(amount)
        .balanceAfter(balanceAfter)
        .type(PointHistoryType.USE)
        .idempotencyKey(idempotencyKey)
        .createdAt(LocalDateTime.now())
        .build();
  }

  /** 배송 취소로 반환된 포인트 이력을 생성한다. */
  public static PointHistory ofRefund(
      Long memberId,
      Long walletId,
      long amount,
      long balanceAfter,
      String idempotencyKey,
      Long referenceId,
      String description) {
    return PointHistory.builder()
        .memberId(memberId)
        .walletId(walletId)
        .amount(amount)
        .balanceAfter(balanceAfter)
        .type(PointHistoryType.REFUND)
        .idempotencyKey(idempotencyKey)
        .referenceId(referenceId)
        .description(description)
        .createdAt(LocalDateTime.now())
        .build();
  }

  /** 고객 취소로 배정 배송원에게 지급한 이동 보상 이력을 생성한다. */
  public static PointHistory ofCourierCompensation(
      Long memberId,
      Long walletId,
      long amount,
      long balanceAfter,
      String idempotencyKey,
      Long referenceId,
      String description) {
    return PointHistory.builder()
        .memberId(memberId)
        .walletId(walletId)
        .amount(amount)
        .balanceAfter(balanceAfter)
        .type(PointHistoryType.COURIER_COMPENSATION)
        .idempotencyKey(idempotencyKey)
        .referenceId(referenceId)
        .description(description)
        .createdAt(LocalDateTime.now())
        .build();
  }
}
