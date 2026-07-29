package com.example.shinhangaecheokja.address.entity;

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

/** 자주 쓰는 주소 엔티티. 특정 회원의 별칭, 주소, 상세주소, 픽업가이드를 관리한다. */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "address")
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Column(nullable = false, length = 50)
  private String alias;

  @Column(nullable = false, length = 255)
  private String address;

  @Column(name = "detail_address", length = 255)
  private String detailAddress;

  @Column(name = "pickup_guide", length = 255)
  private String pickupGuide;

  /** 주소 정보를 수정하는 도메인 비즈니스 메서드. */
  public void update(String alias, String address, String detailAddress, String pickupGuide) {
    this.alias = alias;
    this.address = address;
    this.detailAddress = detailAddress;
    this.pickupGuide = pickupGuide;
  }
}
