package com.example.shinhandelivery.vehicle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhandelivery.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleApprovalStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.exception.InvalidWeightException;
import com.example.shinhandelivery.vehicle.exception.OverMaxDistanceException;
import com.example.shinhandelivery.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private MemberService memberService;
  @InjectMocks private VehicleService vehicleService;

  @Test
  @DisplayName("소유자가 존재하고 무게 거리가 유효하면 차량을 등록한다")
  void registerVehicleSuccess() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setMemberId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);

    when(vehicleRepository.save(any(Vehicle.class)))
        .thenAnswer(
            invocation -> {
              Vehicle vehicle = invocation.getArgument(0);
              vehicle.setId(1L);
              return vehicle;
            });

    Vehicle response = vehicleService.create(request);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getMemberId()).isEqualTo(1L);
    assertThat(response.getType()).isEqualTo(VehicleType.CAR);
    assertThat(response.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
  }

  @Test
  @DisplayName("존재하지 않는 소유자면 EntityNotFoundException을 던진다")
  void registerVehicleOwnerNotFoundShouldThrowException() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setMemberId(999L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);

    when(memberService.getById(999L))
        .thenThrow(new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    assertThatThrownBy(() -> vehicleService.create(request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("무게가 0이하면 InvalidWeightException을 던진다")
  void registerVehicleInvalidWeightShouldThrowException() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setMemberId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(-5);
    request.setMaxDistance(100);

    assertThatThrownBy(() -> vehicleService.create(request))
        .isInstanceOf(InvalidWeightException.class);
  }

  @Test
  @DisplayName("최대거리가 0이하면 OverMaxDistanceException을 던진다")
  void registerVehicleOverMaxDistanceShouldThrowException() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setMemberId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(0);

    assertThatThrownBy(() -> vehicleService.create(request))
        .isInstanceOf(OverMaxDistanceException.class);
  }

  @Test
  @DisplayName("존재하지 않는 차량을 조회하면 EntityNotFoundException을 던진다")
  void getVehicleNotFoundShouldThrowException() {
    when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.getById(1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하는 차량을 조회하면 Vehicle을 반환한다")
  void getVehicleSuccess() {
    Vehicle vehicle = new Vehicle();
    vehicle.setMemberId(1L);
    vehicle.setType(VehicleType.DRONE);
    vehicle.setMaxWeight(10);
    vehicle.setMaxDistance(20);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    Vehicle response = vehicleService.getById(1L);

    assertThat(response.getType()).isEqualTo(VehicleType.DRONE);
  }

  @Test
  @DisplayName("존재하지 않는 차량 id로 소유주를 조회하면 EntityNotFoundException을 던진다")
  void getOwnerMemberIdNotFoundShouldThrowException() {
    when(vehicleRepository.findMemberIdById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.getOwnerMemberId(1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하는 차량 id로 조회하면 소유주(memberId)만 반환한다")
  void getOwnerMemberIdSuccess() {
    when(vehicleRepository.findMemberIdById(1L)).thenReturn(Optional.of(7L));

    Long memberId = vehicleService.getOwnerMemberId(1L);

    assertThat(memberId).isEqualTo(7L);
  }

  @Test
  @DisplayName("비관적 락으로 존재하지 않는 차량을 조회하면 EntityNotFoundException을 던진다")
  void getVehicleForUpdateNotFoundShouldThrowException() {
    when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.getVehicleForUpdate(1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("비관적 락으로 존재하는 차량을 조회하면 Vehicle을 반환한다")
  void getVehicleForUpdateSuccess() {
    Vehicle vehicle = new Vehicle();
    vehicle.setMemberId(1L);
    vehicle.setType(VehicleType.DRONE);
    vehicle.setMaxWeight(10);
    vehicle.setMaxDistance(20);
    when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicle));

    Vehicle response = vehicleService.getVehicleForUpdate(1L);

    assertThat(response.getType()).isEqualTo(VehicleType.DRONE);
  }

  @Test
  @DisplayName("AVAILABLE 상태면 차량 정보를 수정한다")
  void updateVehicleSuccess() {
    Vehicle vehicle = new Vehicle();
    vehicle.setStatus(VehicleStatus.AVAILABLE);
    vehicle.setType(VehicleType.CAR);
    vehicle.setMaxWeight(500);
    vehicle.setMaxDistance(100);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    VehicleUpdateRequest request = new VehicleUpdateRequest();
    request.setType(VehicleType.DRONE);
    request.setMaxWeight(10);
    request.setMaxDistance(20);

    Vehicle response = vehicleService.update(1L, request);

    assertThat(response.getType()).isEqualTo(VehicleType.DRONE);
    assertThat(response.getMaxWeight()).isEqualTo(10);
  }

  @Test
  @DisplayName("BUSY 상태면 차량 수정 시 VehicleNotAvailableException을 던진다")
  void updateVehicleBusyShouldThrowException() {
    Vehicle vehicle = new Vehicle();
    vehicle.setStatus(VehicleStatus.BUSY);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    VehicleUpdateRequest request = new VehicleUpdateRequest();
    request.setType(VehicleType.DRONE);
    request.setMaxWeight(10);
    request.setMaxDistance(20);

    assertThatThrownBy(() -> vehicleService.update(1L, request))
        .isInstanceOf(VehicleNotAvailableException.class);
  }

  @Test
  @DisplayName("차량을 BUSY 상태로 전환한다")
  void markBusySuccess() {
    Vehicle vehicle = new Vehicle();
    vehicle.setStatus(VehicleStatus.AVAILABLE);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    vehicleService.markBusy(1L);

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.BUSY);
  }

  @Test
  @DisplayName("차량을 AVAILABLE 상태로 전환한다")
  void markAvailableSuccess() {
    Vehicle vehicle = new Vehicle();
    vehicle.setStatus(VehicleStatus.BUSY);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    vehicleService.markAvailable(1L);

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
  }

  @Test
  @DisplayName("오퍼 후보 조회 시 픽업 위치 기준 반경(3km) 밖 차량은 제외하고 반경 안 차량만 반환한다")
  void findOfferCandidatesExcludesVehiclesOutsideRadius() {
    Location pickupLocation = Location.of(37.5665, 126.9780);

    Vehicle nearbyVehicle =
        Vehicle.builder()
            .id(1L)
            .memberId(10L)
            .maxWeight(1000)
            .maxDistance(100)
            .location(Location.of(37.5670, 126.9785))
            .status(VehicleStatus.AVAILABLE)
            .build();
    Vehicle farVehicle =
        Vehicle.builder()
            .id(2L)
            .memberId(20L)
            .maxWeight(1000)
            .maxDistance(100)
            .location(Location.of(37.6000, 127.0500))
            .status(VehicleStatus.AVAILABLE)
            .build();

    when(vehicleRepository.findByStatusAndMaxWeightGreaterThanEqualAndMaxDistanceGreaterThanEqual(
            VehicleStatus.AVAILABLE, 10.0, 5.0))
        .thenReturn(List.of(nearbyVehicle, farVehicle));

    List<Vehicle> candidates = vehicleService.findOfferCandidates(10.0, 5.0, pickupLocation);

    assertThat(candidates).containsExactly(nearbyVehicle);
  }

  @Test
  @DisplayName("승인 완료된 장비면 단일 활성화(Exclusive Toggle)를 성공적으로 수행한다")
  void activateVehicleSuccess() {
    Vehicle v1 =
        Vehicle.builder()
            .id(1L)
            .memberId(10L)
            .approvalStatus(VehicleApprovalStatus.APPROVED)
            .isActive(true)
            .build();
    Vehicle v2 =
        Vehicle.builder()
            .id(2L)
            .memberId(10L)
            .approvalStatus(VehicleApprovalStatus.APPROVED)
            .isActive(false)
            .build();

    when(vehicleRepository.findById(2L)).thenReturn(Optional.of(v2));
    when(vehicleRepository.findAllByMemberId(10L)).thenReturn(List.of(v1, v2));

    Vehicle activated = vehicleService.activateVehicle(10L, 2L);

    assertThat(activated.isActive()).isTrue();
    assertThat(v1.isActive()).isFalse();
  }

  @Test
  @DisplayName("승인되지 않은 장비(PENDING) 활성화 시 BusinessException을 던진다")
  void activateVehicleNotApprovedShouldThrowException() {
    Vehicle v1 =
        Vehicle.builder()
            .id(1L)
            .memberId(10L)
            .approvalStatus(VehicleApprovalStatus.PENDING)
            .isActive(false)
            .build();

    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(v1));

    assertThatThrownBy(() -> vehicleService.activateVehicle(10L, 1L))
        .isInstanceOf(BusinessException.class);
  }
}
