package com.example.shinhangaecheokja.payment.repository;

import com.example.shinhangaecheokja.payment.entity.PointWallet;
import org.springframework.data.jpa.repository.JpaRepository;

/** PointWallet 엔티티에 대한 JPA 저장소. */
public interface PaymentRepository extends JpaRepository<PointWallet, Long> {}
