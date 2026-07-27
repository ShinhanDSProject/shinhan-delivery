package com.example.shinhangaecheokja.common.config;

import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.payment.entity.PointWallet;
import com.example.shinhangaecheokja.payment.repository.PaymentRepository;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로컬 개발 편의를 위해 초기에 더미 데이터(회원, 지갑, 차량 등)를 자동으로 적재해 주는 컴포넌트. application.yaml의 app.data-seed.enabled
 * 속성이 true인 경우에만 빈으로 등록되어 기동됩니다.
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
    createVehicle(courier1.getId(), VehicleType.CAR, 1500.0, 300.0);
    createVehicle(courier2.getId(), VehicleType.MOTORCYCLE, 50.0, 50.0);

    log.info("Local data seeding completed successfully!");
  }

  private Member createMember(
      String email, String password, String name, String phoneNumber, MemberRole role) {
    Member member = new Member();
    member.setEmail(email);
    member.setPassword(passwordEncoder.encode(password));
    member.setName(name);
    member.setPhoneNumber(phoneNumber);
    member.setRole(role);
    return memberRepository.save(member);
  }

  private PointWallet createPointWallet(Long memberId, long balance) {
    PointWallet wallet = new PointWallet();
    wallet.setMemberId(memberId);
    wallet.setBalance(balance);
    return paymentRepository.save(wallet);
  }

  private Vehicle createVehicle(
      Long ownerId, VehicleType type, double maxWeight, double maxDistance) {
    Vehicle vehicle = new Vehicle();
    vehicle.setOwnerId(ownerId);
    vehicle.setType(type);
    vehicle.setMaxWeight(maxWeight);
    vehicle.setMaxDistance(maxDistance);
    return vehicleRepository.save(vehicle);
  }
}
