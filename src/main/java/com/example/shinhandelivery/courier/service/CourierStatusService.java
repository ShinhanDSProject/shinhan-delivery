package com.example.shinhandelivery.courier.service;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.courier.dto.request.CourierStatusUpdateRequest;
import com.example.shinhandelivery.courier.dto.response.CourierStatusResponse;
import com.example.shinhandelivery.courier.entity.WorkStatus;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 배송원의 영업 상태(온라인/오프라인) 관리를 전담하는 서비스. */
@Service
@RequiredArgsConstructor
public class CourierStatusService {

  private final MemberService memberService;
  private final VehicleService vehicleService;

  /** 배송원 영업 상태(ONLINE/OFFLINE) 및 GPS 위치를 변경한다. */
  @Transactional
  public CourierStatusResponse updateWorkStatus(Long memberId, CourierStatusUpdateRequest request) {
    Member member = memberService.getById(memberId);

    if (member.getRole() != MemberRole.COURIER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "배송원(COURIER) 권한만 영업 상태를 설정할 수 있습니다.");
    }

    List<Vehicle> vehicles = vehicleService.getVehiclesByOwnerId(memberId);
    if (vehicles.isEmpty()) {
      throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND, "등록된 차량(운송수단)이 없어 출근할 수 없습니다.");
    }

    Vehicle vehicle = vehicles.get(0);
    WorkStatus workStatus = request.getStatus();

    if (workStatus == WorkStatus.ONLINE) {
      vehicle.setStatus(VehicleStatus.AVAILABLE);
    } else {
      vehicle.setStatus(VehicleStatus.BUSY);
    }

    if (request.getLatitude() != null && request.getLongitude() != null) {
      vehicle.setLatitude(request.getLatitude());
      vehicle.setLongitude(request.getLongitude());
    }

    return CourierStatusResponse.builder()
        .memberId(memberId)
        .workStatus(workStatus)
        .latitude(vehicle.getLatitude())
        .longitude(vehicle.getLongitude())
        .build();
  }

  /** 배송원의 현재 영업 상태를 조회한다. */
  @Transactional(readOnly = true)
  public CourierStatusResponse getWorkStatus(Long memberId) {
    Member member = memberService.getById(memberId);

    List<Vehicle> vehicles = vehicleService.getVehiclesByOwnerId(memberId);
    if (vehicles.isEmpty()) {
      return CourierStatusResponse.builder()
          .memberId(memberId)
          .workStatus(WorkStatus.OFFLINE)
          .latitude(0.0)
          .longitude(0.0)
          .build();
    }

    Vehicle vehicle = vehicles.get(0);
    WorkStatus workStatus =
        (vehicle.getStatus() == VehicleStatus.AVAILABLE) ? WorkStatus.ONLINE : WorkStatus.OFFLINE;

    return CourierStatusResponse.builder()
        .memberId(memberId)
        .workStatus(workStatus)
        .latitude(vehicle.getLatitude())
        .longitude(vehicle.getLongitude())
        .build();
  }
}
