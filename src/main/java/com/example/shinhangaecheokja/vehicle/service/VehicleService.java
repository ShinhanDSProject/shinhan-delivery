package com.example.shinhangaecheokja.vehicle.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.exception.InvalidWeightException;
import com.example.shinhangaecheokja.vehicle.exception.OverMaxDistanceException;
import com.example.shinhangaecheokja.vehicle.repository.VehicleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vehicle 관련 유스케이스(등록/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final MemberService memberService;

  /** 소유자(Member) 존재 여부와 무게/거리 유효성을 검증한 뒤 Vehicle을 등록한다. */
  @Transactional
  public VehicleResponse registerVehicle(VehicleCreateRequest request) {
    memberService.getMember(request.getOwnerId());
    validateWeightAndDistance(request.getMaxWeight(), request.getMaxDistance());

    Vehicle vehicle = new Vehicle();
    vehicle.setOwnerId(request.getOwnerId());
    vehicle.setType(request.getType());
    vehicle.setMaxWeight(request.getMaxWeight());
    vehicle.setMaxDistance(request.getMaxDistance());

    return VehicleResponse.from(vehicleRepository.save(vehicle));
  }

  /** id로 Vehicle 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public VehicleResponse getVehicle(Long vehicleId) {
    return VehicleResponse.from(findVehicleOrThrow(vehicleId));
  }

  /** 전체 Vehicle 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehicles() {
    return vehicleRepository.findAll().stream().map(VehicleResponse::from).toList();
  }

  /** 주어진 무게·거리를 감당할 수 있는 차량이 하나라도 있는지 확인한다. */
  @Transactional(readOnly = true)
  public boolean existsAvailableVehicle(double weight, double distance) {
    return vehicleRepository.existsByMaxWeightGreaterThanEqualAndMaxDistanceGreaterThanEqual(
        weight, distance);
  }

  /** 무게/거리 유효성을 검증한 뒤 Vehicle의 종류·무게·거리를 수정한다. ownerId는 변경하지 않는다. */
  @Transactional
  public VehicleResponse updateVehicle(Long vehicleId, VehicleUpdateRequest request) {
    validateWeightAndDistance(request.getMaxWeight(), request.getMaxDistance());

    Vehicle vehicle = findVehicleOrThrow(vehicleId);
    vehicle.setType(request.getType());
    vehicle.setMaxWeight(request.getMaxWeight());
    vehicle.setMaxDistance(request.getMaxDistance());
    return VehicleResponse.from(vehicle);
  }

  /** id로 Vehicle을 조회해 삭제한다. 없으면 EntityNotFoundException. */
  @Transactional
  public void deleteVehicle(Long vehicleId) {
    Vehicle vehicle = findVehicleOrThrow(vehicleId);
    vehicleRepository.delete(vehicle);
  }

  private void validateWeightAndDistance(double maxWeight, double maxDistance) {
    if (maxWeight <= 0) {
      throw new InvalidWeightException(maxWeight);
    }
    if (maxDistance <= 0) {
      throw new OverMaxDistanceException(maxDistance);
    }
  }

  private Vehicle findVehicleOrThrow(Long vehicleId) {
    return vehicleRepository
        .findById(vehicleId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.VEHICLE_NOT_FOUND));
  }
}
