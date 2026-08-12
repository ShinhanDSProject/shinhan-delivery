package com.example.shinhandelivery.vehicle.service;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhandelivery.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.vehicle.entity.VehicleApprovalStatus;

/** Vehicle 관련 유스케이스(등록/조회/수정/삭제)를 담당하는 서비스. */
@Service
public class VehicleService {

  /** 오퍼 후보·대기 목록 조회 모두가 공유하는 기본 탐색 반경(km). */
  public static final double DEFAULT_OFFER_RADIUS_KM = 3.0;

  private final VehicleRepository vehicleRepository;
  private final MemberService memberService;

  public VehicleService(VehicleRepository vehicleRepository, @Lazy MemberService memberService) {
    this.vehicleRepository = vehicleRepository;
    this.memberService = memberService;
  }

  /** 소유자(Member) 존재 여부와 무게/거리 유효성을 검증한 뒤 Vehicle을 등록한다 (Entity 리턴). */
  @Transactional
  public Vehicle create(VehicleCreateRequest request) {
    if (request.getMemberId() == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "회원 ID 정보가 필수입니다.");
    }
    memberService.getById(request.getMemberId());
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

  /** 소유자(MemberId) 기준 차량 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesByMemberId(Long memberId) {
    return vehicleRepository.findAllByMemberIdOrderByIdDesc(memberId);
  }

  /** 소유자(MemberId) 기준 차량과 Member 정보를 Fetch Join으로 한 번에 조회한다. */
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesWithMemberByMemberId(Long memberId) {
    return vehicleRepository.findAllByMemberIdWithMember(memberId);
  }

  /** 전체 Vehicle 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<Vehicle> list() {
    return vehicleRepository.findAll();
  }

  /**
   * 신규 배송요청을 감당할 수 있는(AVAILABLE + 용량 충분) 차량 중, 픽업 위치 기준 {@link #DEFAULT_OFFER_RADIUS_KM} 이내에 있는 차량만
   * 오퍼 후보로 조회한다.
   */
  @Transactional(readOnly = true)
  public List<Vehicle> findOfferCandidates(
      double weight, double distance, Location pickupLocation) {
    return vehicleRepository
        .findByStatusAndMaxWeightGreaterThanEqualAndMaxDistanceGreaterThanEqual(
            VehicleStatus.AVAILABLE, weight, distance)
        .stream()
        .filter(
            vehicle ->
                vehicle.getLocation().distanceToKm(pickupLocation) <= DEFAULT_OFFER_RADIUS_KM)
        .toList();
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
   * 배송원의 장비 중 단 1개만 활성화(Exclusive Toggle) 트랜잭션.
   * 승인 완료(APPROVED) 상태인 장비만 활성화 허용.
   */
  @Transactional
  public Vehicle activateVehicle(Long memberId, Long vehicleId) {
    Vehicle target = findVehicleOrThrow(vehicleId);
    if (!target.getMemberId().equals(memberId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    if (target.getApprovalStatus() != VehicleApprovalStatus.APPROVED) {
      throw new BusinessException(ErrorCode.VEHICLE_NOT_APPROVED);
    }

    List<Vehicle> memberVehicles = vehicleRepository.findAllByMemberId(memberId);
    for (Vehicle v : memberVehicles) {
      v.deactivate();
    }
    target.activate();
    return target;
  }

  /** 관리자가 장비를 승인한다. */
  @Transactional
  public Vehicle approveVehicle(Long vehicleId) {
    Vehicle vehicle = findVehicleOrThrow(vehicleId);
    vehicle.approve();
    return vehicle;
  }

  /** 관리자가 장비를 반려한다. */
  @Transactional
  public Vehicle rejectVehicle(Long vehicleId) {
    Vehicle vehicle = findVehicleOrThrow(vehicleId);
    vehicle.reject();
    return vehicle;
  }

  /**
   * 무게/거리 유효성을 검증한 뒤 Vehicle의 종류·무게·거리를 수정한다. ownerId는 변경하지 않는다. 이미 배정되어 BUSY인 차량은 수정할 수
   * 없다(AVAILABLE 상태에서만 허용).
   */
  @Transactional
  public Vehicle update(Long vehicleId, VehicleUpdateRequest request) {
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

  /** 특정 회원(memberId)이 소유한 모든 Vehicle을 삭제한다. */
  @Transactional
  public void deleteAllByMemberId(Long memberId) {
    vehicleRepository.findAllByMemberId(memberId).forEach(vehicleRepository::delete);
  }

  /** Vehicle 객체를 저장한다. */
  @Transactional
  public Vehicle save(Vehicle vehicle) {
    return vehicleRepository.save(vehicle);
  }

  private Vehicle findVehicleOrThrow(Long vehicleId) {
    return vehicleRepository
        .findById(vehicleId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.VEHICLE_NOT_FOUND));
  }
}
