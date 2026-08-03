package com.example.shinhangaecheokja.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.payment.dto.request.PointChargeRequest;
import com.example.shinhangaecheokja.payment.dto.response.PointBalanceResponse;
import com.example.shinhangaecheokja.payment.entity.PaymentMethod;
import com.example.shinhangaecheokja.payment.entity.PointHistoryType;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
import com.example.shinhangaecheokja.payment.repository.PointHistoryRepository;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PointChargeConcurrencyTest {

  private static final int THREAD_COUNT = 100;
  private static final long CHARGE_AMOUNT = 100L;

  @Autowired private PaymentService paymentService;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private PointHistoryRepository pointHistoryRepository;
  @Autowired private MemberRepository memberRepository;

  private Long memberId;
  private Long walletId;

  @BeforeEach
  void setUp() {
    Member member = new Member();
    member.setEmail("charge-concurrency-" + System.nanoTime() + "@example.com");
    member.setPassword("password");
    member.setName("충전 동시성 테스트 회원");
    member.setPhoneNumber("010-1111-1111");
    member.setRole(MemberRole.CUSTOMER);
    memberId = memberRepository.save(member).getId();

    PointWallet wallet = new PointWallet();
    wallet.setMemberId(memberId);
    wallet.setBalance(0L);
    walletId = paymentRepository.save(wallet).getId();
  }

  @AfterEach
  void cleanUp() {
    pointHistoryRepository.deleteAll();
    paymentRepository.deleteById(walletId);
    memberRepository.deleteById(memberId);
  }

  @Test
  void 동일한_멱등성키의_동시충전은_한번만_반영된다() throws InterruptedException {
    String idempotencyKey = UUID.randomUUID().toString();
    PointChargeRequest request = chargeRequest();

    Queue<PointBalanceResponse> responses = runConcurrentCharges(index -> idempotencyKey, request);

    PointWallet wallet = paymentRepository.findById(walletId).orElseThrow();
    assertThat(responses).hasSize(THREAD_COUNT);
    assertThat(responses).allMatch(response -> response.balance() == CHARGE_AMOUNT);
    assertThat(wallet.getBalance()).isEqualTo(CHARGE_AMOUNT);
    assertThat(pointHistoryRepository.countByWalletIdAndType(walletId, PointHistoryType.CHARGE))
        .isEqualTo(1L);
  }

  @Test
  void 서로다른_멱등성키의_동시충전은_유실없이_모두_반영된다() throws InterruptedException {
    PointChargeRequest request = chargeRequest();

    Queue<PointBalanceResponse> responses =
        runConcurrentCharges(index -> UUID.randomUUID().toString(), request);

    PointWallet wallet = paymentRepository.findById(walletId).orElseThrow();
    assertThat(responses).hasSize(THREAD_COUNT);
    assertThat(wallet.getBalance()).isEqualTo(THREAD_COUNT * CHARGE_AMOUNT);
    assertThat(pointHistoryRepository.countByWalletIdAndType(walletId, PointHistoryType.CHARGE))
        .isEqualTo(THREAD_COUNT);
  }

  private Queue<PointBalanceResponse> runConcurrentCharges(
      IdempotencyKeyFactory keyFactory, PointChargeRequest request) throws InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
    Queue<PointBalanceResponse> responses = new ConcurrentLinkedQueue<>();
    Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

    try {
      for (int i = 0; i < THREAD_COUNT; i++) {
        int requestIndex = i;
        executorService.submit(
            () -> {
              readyLatch.countDown();
              try {
                startLatch.await();
                responses.add(
                    paymentService.charge(memberId, keyFactory.create(requestIndex), request));
              } catch (Throwable throwable) {
                failures.add(throwable);
              } finally {
                doneLatch.countDown();
              }
            });
      }

      assertThat(readyLatch.await(10, TimeUnit.SECONDS)).isTrue();
      startLatch.countDown();
      assertThat(doneLatch.await(60, TimeUnit.SECONDS)).isTrue();
    } finally {
      executorService.shutdownNow();
    }

    assertThat(failures).isEmpty();
    return responses;
  }

  private PointChargeRequest chargeRequest() {
    PointChargeRequest request = new PointChargeRequest();
    request.setAmount(CHARGE_AMOUNT);
    request.setPaymentMethod(PaymentMethod.CARD);
    return request;
  }

  @FunctionalInterface
  private interface IdempotencyKeyFactory {
    String create(int requestIndex);
  }
}
