package com.example.shinhandelivery.courier.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.courier.dto.CourierStatusResponse;
import com.example.shinhandelivery.courier.dto.CourierStatusUpdateRequest;
import com.example.shinhandelivery.courier.dto.WorkStatus;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
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

  @Mock private MemberService memberService;
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
            .type(VehicleType.MOTORCYCLE)
            .status(VehicleStatus.BUSY)
            .latitude(37.5)
            .longitude(127.0)
            .build();

    given(memberService.getById(memberId)).willReturn(courier);
    given(vehicleService.getVehiclesByMemberId(memberId)).willReturn(List.of(vehicle));

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

    given(memberService.getById(memberId)).willReturn(customer);
    CourierStatusUpdateRequest request =
        new CourierStatusUpdateRequest(WorkStatus.ONLINE, 37.5, 127.0);

    // when & then
    assertThatThrownBy(() -> courierStatusService.updateWorkStatus(memberId, request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("배송원(COURIER) 권한만");
  }
}
