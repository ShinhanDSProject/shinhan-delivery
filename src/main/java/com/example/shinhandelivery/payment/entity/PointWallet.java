package com.example.shinhandelivery.payment.entity;

import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.payment.exception.InsufficientPointException;
import com.example.shinhandelivery.payment.exception.InvalidPointAmountException;
import com.example.shinhandelivery.payment.exception.PointBalanceOverflowException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private Member member;

  @Column(nullable = false)
  private long balance;

  @Version private Long version;

  /** 잔액 0인 새 포인트 지갑 엔티티를 생성하는 정적 팩토리 메서드. */
  public static PointWallet createEmpty(Long memberId) {
    return PointWallet.builder().memberId(memberId).balance(0L).build();
  }

  /**
   * 지갑에 포인트를 충전하는 도메인 비즈니스 메서드. amount가 0 이하이면 InvalidPointAmountException, 잔액이 long 범위를 넘어서면
   * PointBalanceOverflowException.
   */
  public PointWallet charge(long amount) {
    validateAmount(amount);
    try {
      this.balance = Math.addExact(this.balance, amount);
    } catch (ArithmeticException e) {
      throw new PointBalanceOverflowException(this.id, this.balance, amount);
    }
    return this;
  }

  /**
   * 지갑에서 포인트를 차감하는 도메인 비즈니스 메서드. amount가 0 이하이면 InvalidPointAmountException, 잔액이 부족하면
   * InsufficientPointException.
   */
  public PointWallet use(long amount) {
    validateAmount(amount);
    if (this.balance < amount) {
      throw new InsufficientPointException(this.id, amount);
    }
    this.balance -= amount;
    return this;
  }

  private void validateAmount(long amount) {
    if (amount <= 0) {
      throw new InvalidPointAmountException(amount);
    }
  }
}
