package com.example.shinhangaecheokja.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.payment.dto.request.PointUseRequest;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.exception.InsufficientPointException;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * PointWallet 잔액 차감의 동시성 제어(비관적 락)를 실제 DB 트랜잭션으로 검증하는 테스트입니다.
 *
 * <p>Mockito 단위 테스트는 실제 DB 락 경합을 재현할 수 없으므로, 이 테스트는 {@code @SpringBootTest}로 실제 스프링 빈과 트랜잭션을 사용해 동일
 * 지갑에 대한 100개의 동시 차감 요청을 발생시킨다.
 */
@SpringBootTest
@ActiveProfiles("test")
class PaymentConcurrencyTest {

  private static final int THREAD_COUNT = 100;
  private static final long INITIAL_BALANCE = 5_000L;
  private static final long USE_AMOUNT = 100L;

  @Autowired private PaymentService paymentService;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private MemberRepository memberRepository;

  private Long memberId;
  private Long walletId;

  @AfterEach
  void cleanUp() {
    if (walletId != null) {
      paymentRepository.deleteById(walletId);
    }
    if (memberId != null) {
      memberRepository.deleteById(memberId);
    }
  }

  @Test
  @DisplayName("동시 차감 요청은 비관적 락으로 직렬화되어 잔액 정합성을 보장한다")
  void concurrentDeductPointShouldEnsureBalanceConsistency() throws InterruptedException {
    Member member = new Member();
    member.setEmail("concurrency-test-" + System.nanoTime() + "@example.com");
    member.setPassword("password");
    member.setName("동시성 테스트 회원");
    member.setPhoneNumber("010-0000-0000");
    member.setRole(MemberRole.CUSTOMER);
    memberId = memberRepository.save(member).getId();

    PointWallet wallet = new PointWallet();
    wallet.setMemberId(memberId);
    wallet.setBalance(INITIAL_BALANCE);
    walletId = paymentRepository.save(wallet).getId();

    int expectedSuccessCount = (int) (INITIAL_BALANCE / USE_AMOUNT);

    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger insufficientCount = new AtomicInteger();
    List<Throwable> unexpectedFailures = new CopyOnWriteArrayList<>();

    for (int i = 0; i < THREAD_COUNT; i++) {
      executorService.submit(
          () -> {
            readyLatch.countDown();
            try {
              startLatch.await();
              PointUseRequest request = new PointUseRequest();
              request.setAmount(USE_AMOUNT);
              paymentService.usePoint(walletId, request);
              successCount.incrementAndGet();
            } catch (InsufficientPointException e) {
              insufficientCount.incrementAndGet();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } catch (Throwable t) {
              // 락 대기 타임아웃, 커넥션 풀 고갈 등 예상치 못한 실패는 카운터 어디에도 잡히지 않으므로 따로 모아
              // 검증 단계에서 원인을 그대로 드러낸다.
              unexpectedFailures.add(t);
            } finally {
              doneLatch.countDown();
            }
          });
    }

    boolean completedInTime;
    try {
      readyLatch.await();
      startLatch.countDown();
      completedInTime = doneLatch.await(30, TimeUnit.SECONDS);
    } finally {
      // 위 대기 도중 이 스레드 자체가 인터럽트되어 예외가 던져지더라도, 워커 스레드들이 살아있는 채로
      // @AfterEach의 DB 정리가 실행되지 않도록 종료 처리는 항상 수행한다.
      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
      }
    }

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(completedInTime).as("모든 스레드가 제한시간 내에 종료됨").isTrue();
    softly.assertThat(unexpectedFailures).as("예상치 못한 예외 없음").isEmpty();
    softly.assertAll();

    long finalBalance = paymentRepository.findById(walletId).orElseThrow().getBalance();

    assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
    assertThat(insufficientCount.get()).isEqualTo(THREAD_COUNT - expectedSuccessCount);
    assertThat(finalBalance).isEqualTo(INITIAL_BALANCE - (long) expectedSuccessCount * USE_AMOUNT);
    assertThat(finalBalance).isZero();
  }
}
