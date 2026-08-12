package com.example.shinhandelivery.courier.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.courier.dto.request.CourierStatusUpdateRequest;
import com.example.shinhandelivery.courier.dto.response.CourierStatusResponse;
import com.example.shinhandelivery.courier.entity.WorkStatus;
import com.example.shinhandelivery.member.entity.CourierApprovalStatus;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
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
class CourierStatusServiceTest {

  @Mock private VehicleService vehicleService;

  @InjectMocks private CourierStatusService courierStatusService;

  @Test
  @DisplayName("배송원이 ONLINE으로 출근 등록 시 vehicle.status가 AVAILABLE로 업데이트된다")
  void updateWorkStatusOnlineSuccess() {
    // given
    Long memberId = 1L;
    Member courier = Member.builder().id(memberId).role(MemberRole.COURIER).build();
    Vehicle vehicle =
        Vehicle.builder()
            .id(10L)
            .memberId(memberId)
            .member(courier)
            .type(VehicleType.MOTORCYCLE)
            .status(VehicleStatus.BUSY)
            .location(Location.of(37.5, 127.0))
            .build();

    given(vehicleService.getVehiclesWithMemberByMemberId(memberId)).willReturn(List.of(vehicle));

    CourierStatusUpdateRequest request =
        new CourierStatusUpdateRequest(WorkStatus.ONLINE, 37.5665, 126.9780);

    // when
    CourierStatusResponse response = courierStatusService.updateWorkStatus(memberId, request);

    // then
    assertThat(response.getWorkStatus()).isEqualTo(WorkStatus.ONLINE);
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    assertThat(response.getLatitude()).isEqualTo(37.5665);
    assertThat(response.getLongitude()).isEqualTo(126.9780);
  }

  @Test
  @DisplayName("일반 고객(CUSTOMER)이 출근 시도 시 ACCESS_DENIED 예외가 발생한다")
  void updateWorkStatusCustomerAccessDenied() {
    // given
    Long memberId = 2L;
    Member customer = Member.builder().id(memberId).role(MemberRole.CUSTOMER).build();
    Vehicle vehicle =
        Vehicle.builder()
            .id(20L)
            .memberId(memberId)
            .member(customer)
            .type(VehicleType.CAR)
            .status(VehicleStatus.BUSY)
            .location(Location.of(37.5, 127.0))
            .build();

    given(vehicleService.getVehiclesWithMemberByMemberId(memberId)).willReturn(List.of(vehicle));
    CourierStatusUpdateRequest request =
        new CourierStatusUpdateRequest(WorkStatus.ONLINE, 37.5, 127.0);

    // when & then
    assertThatThrownBy(() -> courierStatusService.updateWorkStatus(memberId, request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("배송원(COURIER) 권한만");
  }

  @Test
  @DisplayName("승인 대기(PENDING) 상태인 배송원이 ONLINE 출근 시도 시 서류 심사 예외가 발생한다")
  void updateWorkStatusPendingCourierAccessDenied() {
    Long memberId = 6L;
    Member pendingCourier =
        Member.builder()
            .id(memberId)
            .role(MemberRole.COURIER)
            .courierApprovalStatus(CourierApprovalStatus.PENDING)
            .build();

    Vehicle vehicle =
        Vehicle.builder()
            .id(60L)
            .memberId(memberId)
            .member(pendingCourier)
            .type(VehicleType.MOTORCYCLE)
            .status(VehicleStatus.BUSY)
            .location(Location.of(37.5, 127.0))
            .build();

    given(vehicleService.getVehiclesWithMemberByMemberId(memberId)).willReturn(List.of(vehicle));
    CourierStatusUpdateRequest request =
        new CourierStatusUpdateRequest(WorkStatus.ONLINE, 37.5, 127.0);

    assertThatThrownBy(() -> courierStatusService.updateWorkStatus(memberId, request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("서류 심사가 진행 중입니다")
        .extracting("errorCode")
        .isEqualTo(ErrorCode.ACCESS_DENIED);
  }

  @Test
  @DisplayName("배송원이 OFFLINE으로 퇴근 등록 시 vehicle.status가 BUSY로 업데이트된다")
  void updateWorkStatusOfflineSuccess() {
    Long memberId = 3L;
    Member courier = Member.builder().id(memberId).role(MemberRole.COURIER).build();
    Vehicle vehicle =
        Vehicle.builder()
            .id(30L)
            .memberId(memberId)
            .member(courier)
            .type(VehicleType.CAR)
            .status(VehicleStatus.AVAILABLE)
            .location(Location.of(37.7, 127.2))
            .build();

    given(vehicleService.getVehiclesWithMemberByMemberId(memberId)).willReturn(List.of(vehicle));

    CourierStatusUpdateRequest request =
        new CourierStatusUpdateRequest(WorkStatus.OFFLINE, null, null);

    CourierStatusResponse response = courierStatusService.updateWorkStatus(memberId, request);

    assertThat(response.getWorkStatus()).isEqualTo(WorkStatus.OFFLINE);
    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.BUSY);
    assertThat(response.getLatitude()).isEqualTo(37.7);
    assertThat(response.getLongitude()).isEqualTo(127.2);
  }

  @Test
  @DisplayName("등록된 차량이 없으면 출근 상태를 변경할 수 없다")
  void updateWorkStatusWithoutVehicleShouldThrow() {
    Long memberId = 4L;
    given(vehicleService.getVehiclesWithMemberByMemberId(memberId)).willReturn(List.of());

    CourierStatusUpdateRequest request =
        new CourierStatusUpdateRequest(WorkStatus.ONLINE, 37.5, 127.0);

    assertThatThrownBy(() -> courierStatusService.updateWorkStatus(memberId, request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("등록된 차량");
  }

  @Test
  @DisplayName("등록 차량이 없으면 조회 상태는 OFFLINE 기본 좌표를 반환한다")
  void getWorkStatusWithoutVehicleReturnsOffline() {
    Long memberId = 5L;
    given(vehicleService.getVehiclesByMemberId(memberId)).willReturn(List.of());

    CourierStatusResponse response = courierStatusService.getWorkStatus(memberId);

    assertThat(response.getWorkStatus()).isEqualTo(WorkStatus.OFFLINE);
    assertThat(response.getLatitude()).isZero();
    assertThat(response.getLongitude()).isZero();
  }
}
