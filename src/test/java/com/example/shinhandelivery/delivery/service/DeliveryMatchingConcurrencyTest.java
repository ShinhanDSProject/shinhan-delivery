package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DeliveryMatchingConcurrencyTest {

  @Autowired private DeliveryMatchingService deliveryMatchingService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MatchingRepository matchingRepository;

  @Test
  @DisplayName("1개의 주문에 10명의 배송원이 동시 수락 시도 시, 단 1명만 성공하고 9명은 409 Conflict 예외가 발생한다")
  void catchDeliveryConcurrencyTestOptimisticLocking() throws InterruptedException {
    // given: 고객 생성 및 1개의 대기 중인 주문 생성
    long time = System.currentTimeMillis();
    Member customer =
        memberRepository.save(
            Member.builder()
                .email("customer_concurrent_" + time + "@shinhan.com")
                .password("password123!")
                .name("주문고객")
                .phoneNumber("010-9999-9999")
                .role(MemberRole.CUSTOMER)
                .build());

    DeliveryRequest request =
        DeliveryRequest.builder()
            .memberId(customer.getId())
            .pickupAddress("서울시 중구 을지로 100")
            .dropoffAddress("서울시 명동 20")
            .pickupLocation(Location.of(37.5665, 126.9780))
            .dropoffLocation(Location.of(37.5600, 126.9800))
            .weight(2.5)
            .distance(1.2)
            .feePoint(5000L)
            .status(DeliveryStatus.REQUESTED)
            .itemSize(ItemSize.MEDIUM)
            .createdAt(LocalDateTime.now())
            .build();
    DeliveryRequest savedRequest = deliveryRequestRepository.save(request);

    // given: 10명의 배송원 및 각 운송수단 생성
    int threadCount = 10;
    List<Member> couriers = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      Member courier =
          memberRepository.save(
              Member.builder()
                  .email("courier_concurrent_" + time + "_" + i + "@shinhan.com")
                  .password("password123!")
                  .name("배송원" + i)
                  .phoneNumber("010-0000-000" + i)
                  .role(MemberRole.COURIER)
                  .build());

      vehicleRepository.save(
          Vehicle.builder()
              .memberId(courier.getId())
              .type(VehicleType.MOTORCYCLE)
              .maxWeight(10.0)
              .maxDistance(20.0)
              .location(Location.of(37.5665, 126.9780))
              .status(VehicleStatus.AVAILABLE)
              .isActive(true)
              .build());

      couriers.add(courier);
    }

    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch readyLatch = new CountDownLatch(threadCount);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // when: 10개 스레드가 동시 정밀 수락 시도
    for (Member courier : couriers) {
      executorService.submit(
          () -> {
            readyLatch.countDown();
            try {
              startLatch.await(); // 10개 스레드가 동시 시작되도록 총성 신호 대기
              deliveryMatchingService.catchDelivery(courier.getId(), savedRequest.getId());
              successCount.incrementAndGet();
            } catch (AlreadyMatchedException e) {
              failureCount.incrementAndGet();
            } catch (Throwable e) {
              failureCount.incrementAndGet();
            } finally {
              doneLatch.countDown();
            }
          });
    }

    readyLatch.await();
    startLatch.countDown(); // 0.1초 차이 없는 동시 수락 총성 발사
    doneLatch.await();

    // then: 단 1명만 성공하고 9명은 경합 패배
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(failureCount.get()).isEqualTo(9);

    DeliveryRequest updatedRequest =
        deliveryRequestRepository.findById(savedRequest.getId()).orElseThrow();
    assertThat(updatedRequest.getStatus()).isEqualTo(DeliveryStatus.MATCHED);

    long matchingCount =
        matchingRepository.findAll().stream()
            .filter(m -> m.getDeliveryRequestId().equals(savedRequest.getId()))
            .count();
    assertThat(matchingCount).isEqualTo(1);
  }

  @Test
  @DisplayName("한 배송원(차량)이 서로 다른 두 주문을 동시에 수락 시도하면 단 1건만 성공하고 나머지는 차량 사용 불가로 실패한다")
  void catchDeliveryConcurrencyTestSameVehicleDoubleBooking() throws InterruptedException {
    // given: 고객 및 서로 다른 대기 중인 주문 2건 생성
    long time = System.currentTimeMillis();
    Member customer =
        memberRepository.save(
            Member.builder()
                .email("customer_double_" + time + "@shinhan.com")
                .password("password123!")
                .name("주문고객")
                .phoneNumber("010-8888-8888")
                .role(MemberRole.CUSTOMER)
                .build());

    DeliveryRequest request1 =
        deliveryRequestRepository.save(
            DeliveryRequest.builder()
                .memberId(customer.getId())
                .pickupAddress("서울시 중구 을지로 100")
                .dropoffAddress("서울시 명동 20")
                .pickupLocation(Location.of(37.5665, 126.9780))
                .dropoffLocation(Location.of(37.5600, 126.9800))
                .weight(2.5)
                .distance(1.2)
                .feePoint(5000L)
                .status(DeliveryStatus.REQUESTED)
                .itemSize(ItemSize.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build());
    DeliveryRequest request2 =
        deliveryRequestRepository.save(
            DeliveryRequest.builder()
                .memberId(customer.getId())
                .pickupAddress("서울시 강남구 테헤란로 200")
                .dropoffAddress("서울시 서초구 서초대로 50")
                .pickupLocation(Location.of(37.5010, 127.0396))
                .dropoffLocation(Location.of(37.4920, 127.0140))
                .weight(2.5)
                .distance(1.2)
                .feePoint(5000L)
                .status(DeliveryStatus.REQUESTED)
                .itemSize(ItemSize.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build());

    // given: 배송원 1명 및 차량 1대
    Member courier =
        memberRepository.save(
            Member.builder()
                .email("courier_double_" + time + "@shinhan.com")
                .password("password123!")
                .name("배송원")
                .phoneNumber("010-7777-7777")
                .role(MemberRole.COURIER)
                .build());
    vehicleRepository.save(
        Vehicle.builder()
            .memberId(courier.getId())
            .type(VehicleType.MOTORCYCLE)
            .maxWeight(10.0)
            .maxDistance(20.0)
            .location(Location.of(37.5665, 126.9780))
            .status(VehicleStatus.AVAILABLE)
            .isActive(true)
            .build());

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch readyLatch = new CountDownLatch(2);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(2);

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failureCount = new AtomicInteger(0);

    // when: 같은 차량으로 서로 다른 주문 2건을 동시에 수락 시도
    for (Long deliveryRequestId : List.of(request1.getId(), request2.getId())) {
      executorService.submit(
          () -> {
            readyLatch.countDown();
            try {
              startLatch.await();
              deliveryMatchingService.catchDelivery(courier.getId(), deliveryRequestId);
              successCount.incrementAndGet();
            } catch (BusinessException e) {
              failureCount.incrementAndGet();
            } catch (Throwable e) {
              failureCount.incrementAndGet();
            } finally {
              doneLatch.countDown();
            }
          });
    }

    readyLatch.await();
    startLatch.countDown();
    doneLatch.await();

    // then: 차량 row 자체를 비관적 락으로 잠그기 때문에, 서로 다른 주문이어도 같은 차량으로는 단 1건만 성공한다
    assertThat(successCount.get()).isEqualTo(1);
    assertThat(failureCount.get()).isEqualTo(1);

    long matchedCount =
        deliveryRequestRepository.findAllById(List.of(request1.getId(), request2.getId())).stream()
            .filter(d -> d.getStatus() == DeliveryStatus.MATCHED)
            .count();
    assertThat(matchedCount).isEqualTo(1);
  }
}
