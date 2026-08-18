package com.example.shinhandelivery.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PointWalletProvisioningServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @InjectMocks private PointWalletProvisioningService pointWalletProvisioningService;

  @Test
  @DisplayName("이미 지갑이 있으면 기존 지갑을 그대로 반환한다")
  void ensureWalletReturnsExistingWallet() {
    PointWallet existingWallet = PointWallet.builder().id(1L).memberId(10L).balance(3000L).build();
    when(paymentRepository.findByMemberId(10L)).thenReturn(Optional.of(existingWallet));

    PointWallet result = pointWalletProvisioningService.ensureWallet(10L);

    assertThat(result).isSameAs(existingWallet);
  }

  @Test
  @DisplayName("지갑 생성 중 중복 생성 예외가 나면 재조회한 기존 지갑으로 복구한다")
  void ensureWalletRecoversAfterDuplicateCreationRace() {
    PointWallet existingWallet = PointWallet.builder().id(2L).memberId(20L).balance(0L).build();
    when(paymentRepository.findByMemberId(20L))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(existingWallet));
    when(paymentRepository.save(any(PointWallet.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate wallet"));

    PointWallet result = pointWalletProvisioningService.ensureWallet(20L);

    assertThat(result).isSameAs(existingWallet);
    verify(paymentRepository).save(any(PointWallet.class));
  }
}
