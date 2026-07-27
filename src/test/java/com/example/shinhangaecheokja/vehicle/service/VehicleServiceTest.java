package com.example.shinhangaecheokja.vehicle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.member.exception.MemberNotFoundException;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.exception.InvalidWeightException;
import com.example.shinhangaecheokja.vehicle.exception.OverMaxDistanceException;
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
}
