package com.example.shinhangaecheokja.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원의 포인트 잔액을 보관하는 지갑 엔티티. member_id는 Member를 가리키는 FK 값이다. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "point_wallet")
public class PointWallet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false, unique = true)
  private Long memberId;

  @Column(nullable = false)
  private long balance;

  /** 잔액 0원인 신규 포인트 지갑 엔티티를 생성하는 정적 팩토리 메서드. */
  public static PointWallet createEmpty(Long memberId) {
    return PointWallet.builder().memberId(memberId).balance(0L).build();
  }
}
