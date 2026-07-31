package com.example.shinhangaecheokja.vehicle.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.exception.InvalidWeightException;
import com.example.shinhangaecheokja.vehicle.exception.OverMaxDistanceException;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotAvailableException;
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

  /** 소유자(Member) 존재 여부와 무게/거리 유효성을 검증한 뒤 Vehicle을 등록한다 (Entity 리턴). */
  @Transactional
  public Vehicle create(VehicleCreateRequest request) {
    memberService.getById(request.getOwnerId());
    validateWeightAndDistance(request.getMaxWeight(), request.getMaxDistance());

    return vehicleRepository.save(Vehicle.from(request));
  }

  /** id로 Vehicle 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public Vehicle getById(Long vehicleId) {
    return findVehicleOrThrow(vehicleId);
  }

  /**
   * 매칭(배정) 직전에 비관적 쓰기 락으로 Vehicle을 조회한다. 동시에 들어온 다른 매칭 요청이 같은 차량을 동시에 가져가지 못하도록, 호출한 트랜잭션이 끝날 때까지
   * 해당 차량 행을 잠근다.
   */
  @Transactional
  public Vehicle getVehicleForUpdate(Long vehicleId) {
    return vehicleRepository
        .findByIdForUpdate(vehicleId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.VEHICLE_NOT_FOUND));
  }

  /** 전체 Vehicle 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<Vehicle> list() {
    return vehicleRepository.findAll();
  }

  /** Vehicle을 BUSY 상태로 전환한다. */
  @Transactional
  public void markBusy(Long vehicleId) {
    findVehicleOrThrow(vehicleId).markAs(VehicleStatus.BUSY);
  }

  /** Vehicle을 AVAILABLE 상태로 전환한다. */
  @Transactional
  public void markAvailable(Long vehicleId) {
    findVehicleOrThrow(vehicleId).markAs(VehicleStatus.AVAILABLE);
  }

  /**
   * 무게/거리 유효성을 검증한 뒤 Vehicle의 종류·무게·거리를 수정한다. ownerId는 변경하지 않는다. 이미 배정되어 BUSY인 차량은 수정할 수
   * 없다(AVAILABLE 상태에서만 허용).
   */
  @Transactional
  public Vehicle update(Long vehicleId, VehicleUpdateRequest request) {
    validateWeightAndDistance(request.getMaxWeight(), request.getMaxDistance());

    Vehicle vehicle = findVehicleOrThrow(vehicleId);
    if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
      throw new VehicleNotAvailableException(vehicleId);
    }
    return vehicle.updateBy(request);
  }

  /** id로 Vehicle을 조회해 삭제한다. 없으면 EntityNotFoundException. */
  @Transactional
  public void delete(Long vehicleId) {
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
