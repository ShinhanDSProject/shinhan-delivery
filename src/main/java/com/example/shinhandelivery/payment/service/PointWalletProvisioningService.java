package com.example.shinhandelivery.payment.service;

import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회원 기준 포인트 지갑 존재를 보장하는 프로비저닝 서비스. */
@Service
@RequiredArgsConstructor
public class PointWalletProvisioningService {

  private final PaymentRepository paymentRepository;

  @Transactional
  public PointWallet ensureWallet(Long memberId) {
    return paymentRepository.findByMemberId(memberId).orElseGet(() -> createIfMissing(memberId));
  }

  private PointWallet createIfMissing(Long memberId) {
    try {
      return paymentRepository.save(PointWallet.createEmpty(memberId));
    } catch (DataIntegrityViolationException e) {
      return paymentRepository.findByMemberId(memberId).orElseThrow(() -> e);
    }
  }
}
