package com.example.shinhandelivery.common.config;

import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.delivery.service.MatchingService;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.repository.NotificationRepository;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로컬 개발 편의를 위해 초기에 더미 데이터(회원, 지갑, 차량, 매칭된 배송 요청 등)를 자동으로 적재해 주는 컴포넌트. application.yaml의
 * app.data-seed.enabled 속성이 true인 경우에만 빈으로 등록되어 기동됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.data-seed.enabled", havingValue = "true")
public class DataSeedInitializer implements CommandLineRunner {

  private final MemberRepository memberRepository;
  private final VehicleRepository vehicleRepository;
  private final PaymentRepository paymentRepository;
  private final PasswordEncoder passwordEncoder;
  private final DeliveryService deliveryService;
  private final MatchingService matchingService;
  private final NotificationRepository notificationRepository;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    log.info("Starting local data seeding...");

    // 테이블이 완전히 비어있을 때만 더미 데이터를 시딩하여 중복 발생 방지
    if (memberRepository.count() > 0) {
      log.info("Database already contains member data. Skipping local data seeding.");
      return;
    }

    // 1. 테스트 회원 생성 (화주 1명, 배송원 2명)
    Member client =
        createMember(
            "client@example.com", "password123", "김화주", "010-1234-5678", MemberRole.CUSTOMER);
    Member courier1 =
        createMember(
            "courier1@example.com", "password123", "박배송", "010-2345-6789", MemberRole.COURIER);
    Member courier2 =
        createMember(
            "courier2@example.com", "password123", "이배송", "010-3456-7890", MemberRole.COURIER);

    // 2. 포인트 지갑 생성 (화주에게는 배송 결제용 50만 원 지급)
    createPointWallet(client.getId(), 500000L);
    createPointWallet(courier1.getId(), 0L);
    createPointWallet(courier2.getId(), 0L);

    // 3. 배송원 차량 등록 (1.5톤 트럭 1대, 오토바이 1대)
    Vehicle truck = createVehicle(courier1.getId(), VehicleType.CAR, 1500.0, 300.0);
    createVehicle(courier2.getId(), VehicleType.MOTORCYCLE, 50.0, 50.0);

    // 4. 실시간 추적 화면(FE-020/021) 로컬 테스트용 매칭된 배송 요청 1건 (트럭 콜 수락 상태)
    createMatchedDelivery(client.getId(), truck.getId());

    // 5. 알림센터 UI 로컬 테스트용 카테고리별 알림 (읽음/안읽음 혼합). 공지사항은 V13 마이그레이션이 이미 시딩하므로 여기서는 다루지 않는다.
    createNotifications(client.getId());

    log.info("Local data seeding completed successfully!");
  }

  private Member createMember(
      String email, String password, String name, String phoneNumber, MemberRole role) {
    Member member =
        Member.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .name(name)
            .phoneNumber(phoneNumber)
            .role(role)
            .build();
    return memberRepository.save(member);
  }

  private PointWallet createPointWallet(Long memberId, long balance) {
    PointWallet wallet = PointWallet.builder().memberId(memberId).balance(balance).build();
    return paymentRepository.save(wallet);
  }

  private Vehicle createVehicle(
      Long memberId, VehicleType type, double maxWeight, double maxDistance) {
    Vehicle vehicle =
        Vehicle.builder()
            .memberId(memberId)
            .type(type)
            .maxWeight(maxWeight)
            .maxDistance(maxDistance)
            .status(VehicleStatus.AVAILABLE)
            .build();
    return vehicleRepository.save(vehicle);
  }

  /** 배송 요청을 생성하고 곧바로 차량이 콜을 수락하게 해, MATCHED 상태의 배송 요청을 만든다. */
  private void createMatchedDelivery(Long customerId, Long vehicleId) {
    DeliveryCreateRequest deliveryRequest = new DeliveryCreateRequest();
    deliveryRequest.setPickupAddress("서울시 강남구 테헤란로 123");
    deliveryRequest.setDropoffAddress("서울시 서초구 서초대로 456");
    deliveryRequest.setWeight(10.0);
    deliveryRequest.setPickupLatitude(37.5);
    deliveryRequest.setPickupLongitude(127.0);
    deliveryRequest.setDropoffLatitude(37.6);
    deliveryRequest.setDropoffLongitude(127.05);
    deliveryRequest.setItemSize(ItemSize.MEDIUM);
    Long deliveryRequestId = deliveryService.requestDelivery(customerId, deliveryRequest).getId();

    MatchingCreateRequest matchingRequest = new MatchingCreateRequest();
    matchingRequest.setDeliveryRequestId(deliveryRequestId);
    matchingRequest.setVehicleId(vehicleId);
    matchingService.create(matchingRequest);
  }

  /** 알림센터 UI(홈 화면 배지, 목록·필터)를 로컬에서 바로 확인할 수 있도록 카테고리별 알림을 만든다. */
  private void createNotifications(Long memberId) {
    createNotification(
        memberId, "배송원이 매칭됐어요", "박배송님이 배송을 수락했어요. 실시간 위치를 확인해보세요.", "MATCHING", false);
    createNotification(memberId, "배송원이 픽업하러 가고 있어요", "박배송님이 픽업 장소로 이동 중이에요.", "DELIVERY", false);
    createNotification(memberId, "포인트가 충전됐어요", "500,000P가 충전됐어요.", "POINT", true);
  }

  private void createNotification(
      Long memberId, String title, String message, String category, boolean isRead) {
    notificationRepository.save(Notification.of(memberId, title, message, category, isRead));
  }
}
