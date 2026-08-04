package com.example.shinhandelivery.vehicle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhandelivery.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.exception.InvalidWeightException;
import com.example.shinhandelivery.vehicle.exception.OverMaxDistanceException;
import com.example.shinhandelivery.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
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
    request.setOwnerId(1L);
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
    assertThat(response.getOwnerId()).isEqualTo(1L);
    assertThat(response.getType()).isEqualTo(VehicleType.CAR);
    assertThat(response.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
  }

  @Test
  @DisplayName("존재하지 않는 소유자면 EntityNotFoundException을 던진다")
  void registerVehicleOwnerNotFoundShouldThrowException() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setOwnerId(999L);
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
    request.setOwnerId(1L);
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
    request.setOwnerId(1L);
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
    vehicle.setOwnerId(1L);
    vehicle.setType(VehicleType.DRONE);
    vehicle.setMaxWeight(10);
    vehicle.setMaxDistance(20);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    Vehicle response = vehicleService.getById(1L);

    assertThat(response.getType()).isEqualTo(VehicleType.DRONE);
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
    vehicle.setOwnerId(1L);
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
}
