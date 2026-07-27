package com.example.shinhangaecheokja.vehicle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.member.exception.MemberNotFoundException;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.exception.InvalidWeightException;
import com.example.shinhangaecheokja.vehicle.exception.OverMaxDistanceException;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotFoundException;
import com.example.shinhangaecheokja.vehicle.repository.VehicleRepository;
import java.util.Optional;
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
  void 소유자가_존재하고_무게_거리가_유효하면_차량을_등록한다() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setOwnerId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);

    when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

    VehicleResponse response = vehicleService.registerVehicle(request);

    assertThat(response.ownerId()).isEqualTo(1L);
    assertThat(response.type()).isEqualTo(VehicleType.CAR);
    assertThat(response.status()).isEqualTo(VehicleStatus.AVAILABLE);
  }

  @Test
  void 존재하지_않는_소유자면_MemberNotFoundException을_던진다() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setOwnerId(999L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(100);

    when(memberService.getMember(999L)).thenThrow(new MemberNotFoundException(999L));

    assertThatThrownBy(() -> vehicleService.registerVehicle(request))
        .isInstanceOf(MemberNotFoundException.class);
  }

  @Test
  void 무게가_0이하면_InvalidWeightException을_던진다() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setOwnerId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(-5);
    request.setMaxDistance(100);

    assertThatThrownBy(() -> vehicleService.registerVehicle(request))
        .isInstanceOf(InvalidWeightException.class);
  }

  @Test
  void 최대거리가_0이하면_OverMaxDistanceException을_던진다() {
    VehicleCreateRequest request = new VehicleCreateRequest();
    request.setOwnerId(1L);
    request.setType(VehicleType.CAR);
    request.setMaxWeight(500);
    request.setMaxDistance(0);

    assertThatThrownBy(() -> vehicleService.registerVehicle(request))
        .isInstanceOf(OverMaxDistanceException.class);
  }

  @Test
  void 존재하지_않는_차량을_조회하면_VehicleNotFoundException을_던진다() {
    when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.getVehicle(1L))
        .isInstanceOf(VehicleNotFoundException.class);
  }

  @Test
  void 존재하는_차량을_조회하면_VehicleResponse를_반환한다() {
    Vehicle vehicle = new Vehicle();
    vehicle.setOwnerId(1L);
    vehicle.setType(VehicleType.DRONE);
    vehicle.setMaxWeight(10);
    vehicle.setMaxDistance(20);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    VehicleResponse response = vehicleService.getVehicle(1L);

    assertThat(response.type()).isEqualTo(VehicleType.DRONE);
  }

  @Test
  void 비관적_락으로_존재하지_않는_차량을_조회하면_VehicleNotFoundException을_던진다() {
    when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleService.getVehicleForUpdate(1L))
        .isInstanceOf(VehicleNotFoundException.class);
  }

  @Test
  void 비관적_락으로_존재하는_차량을_조회하면_VehicleResponse를_반환한다() {
    Vehicle vehicle = new Vehicle();
    vehicle.setOwnerId(1L);
    vehicle.setType(VehicleType.DRONE);
    vehicle.setMaxWeight(10);
    vehicle.setMaxDistance(20);
    when(vehicleRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(vehicle));

    VehicleResponse response = vehicleService.getVehicleForUpdate(1L);

    assertThat(response.type()).isEqualTo(VehicleType.DRONE);
  }

  @Test
  void AVAILABLE_상태면_차량_정보를_수정한다() {
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

    VehicleResponse response = vehicleService.updateVehicle(1L, request);

    assertThat(response.type()).isEqualTo(VehicleType.DRONE);
    assertThat(response.maxWeight()).isEqualTo(10);
  }

  @Test
  void BUSY_상태면_차량_수정_시_VehicleNotAvailableException을_던진다() {
    Vehicle vehicle = new Vehicle();
    vehicle.setStatus(VehicleStatus.BUSY);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    VehicleUpdateRequest request = new VehicleUpdateRequest();
    request.setType(VehicleType.DRONE);
    request.setMaxWeight(10);
    request.setMaxDistance(20);

    assertThatThrownBy(() -> vehicleService.updateVehicle(1L, request))
        .isInstanceOf(VehicleNotAvailableException.class);
  }

  @Test
  void 차량을_BUSY_상태로_전환한다() {
    Vehicle vehicle = new Vehicle();
    vehicle.setStatus(VehicleStatus.AVAILABLE);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    vehicleService.markBusy(1L);

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.BUSY);
  }

  @Test
  void 차량을_AVAILABLE_상태로_전환한다() {
    Vehicle vehicle = new Vehicle();
    vehicle.setStatus(VehicleStatus.BUSY);
    when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

    vehicleService.markAvailable(1L);

    assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
  }
}
