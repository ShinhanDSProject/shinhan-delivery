package com.example.shinhandelivery.courier.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.courier.dto.response.CourierHomePageResponse;
import com.example.shinhandelivery.courier.dto.response.CourierStatusResponse;
import com.example.shinhandelivery.courier.entity.WorkStatus;
import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.service.DeliveryMatchingService;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourierHomeServiceTest {

  @Mock private MemberService memberService;
  @Mock private CourierStatusService courierStatusService;
  @Mock private VehicleService vehicleService;
  @Mock private DeliveryMatchingService deliveryMatchingService;

  @InjectMocks private CourierHomeService courierHomeService;

  @Test
  @DisplayName("영업 상태가 ONLINE인 경우 기사 정보, 운송수단, 주변 대기배달 목록을 포함하여 반환한다")
  void loadOnlineCourierHomeData() {
    Long memberId = 1L;
    Member member =
        Member.builder()
            .email("courier@example.com")
            .password("encoded-pw")
            .name("박배송")
            .phoneNumber("010-1234-5678")
            .role(MemberRole.COURIER)
            .build();

    CourierStatusResponse statusResponse =
        CourierStatusResponse.builder()
            .memberId(memberId)
            .workStatus(WorkStatus.ONLINE)
            .latitude(37.5665)
            .longitude(126.9780)
            .build();

    Vehicle vehicle = Vehicle.createDefault(memberId, VehicleType.MOTORCYCLE, 50.0, 50.0);

    AvailableDeliveryResponse delivery =
        AvailableDeliveryResponse.builder()
            .deliveryRequestId(101L)
            .pickupAddress("서울 마포구 백범로 31")
            .dropoffAddress("서울 마포구 독막로 12")
            .feePoint(4500)
            .distanceKm(2.1)
            .distanceToPickupKm(0.8)
            .itemSize(ItemSize.SMALL)
            .build();

    when(memberService.getById(memberId)).thenReturn(member);
    when(courierStatusService.getWorkStatus(memberId)).thenReturn(statusResponse);
    when(vehicleService.getVehiclesByMemberId(memberId)).thenReturn(List.of(vehicle));
    when(deliveryMatchingService.getAvailableDeliveries(
            eq(memberId), anyDouble(), anyDouble(), eq(3.0)))
        .thenReturn(List.of(delivery));

    CourierHomePageResponse result = courierHomeService.load(memberId);

    assertThat(result.memberName()).isEqualTo("박배송");
    assertThat(result.transportMode()).isEqualTo("🛵 오토바이");
    assertThat(result.workStatus()).isEqualTo("ONLINE");
    assertThat(result.isOnline()).isTrue();
    assertThat(result.availableCount()).isEqualTo(1);
    assertThat(result.availableDeliveries()).hasSize(1);
  }

  @Test
  @DisplayName("영업 상태가 OFFLINE인 경우 빈 대기배달 목록을 반환한다")
  void loadOfflineCourierHomeData() {
    Long memberId = 1L;
    Member member =
        Member.builder()
            .email("courier@example.com")
            .password("encoded-pw")
            .name("이배송")
            .phoneNumber("010-9876-5432")
            .role(MemberRole.COURIER)
            .build();

    CourierStatusResponse statusResponse =
        CourierStatusResponse.builder()
            .memberId(memberId)
            .workStatus(WorkStatus.OFFLINE)
            .latitude(0.0)
            .longitude(0.0)
            .build();

    when(memberService.getById(memberId)).thenReturn(member);
    when(courierStatusService.getWorkStatus(memberId)).thenReturn(statusResponse);
    when(vehicleService.getVehiclesByMemberId(memberId)).thenReturn(List.of());

    CourierHomePageResponse result = courierHomeService.load(memberId);

    assertThat(result.memberName()).isEqualTo("이배송");
    assertThat(result.transportMode()).isEqualTo("🛵 운송수단 미등록");
    assertThat(result.workStatus()).isEqualTo("OFFLINE");
    assertThat(result.isOnline()).isFalse();
    assertThat(result.availableCount()).isEqualTo(0);
    assertThat(result.availableDeliveries()).isEmpty();
  }
}
