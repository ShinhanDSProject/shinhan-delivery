package com.example.shinhangaecheokja.service;

import com.example.shinhangaecheokja.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.dto.request.VehicleUpdateRequest;
import com.example.shinhangaecheokja.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.entity.Vehicle;
import com.example.shinhangaecheokja.exception.InvalidWeightException;
import com.example.shinhangaecheokja.exception.MemberNotFoundException;
import com.example.shinhangaecheokja.exception.OverMaxDistanceException;
import com.example.shinhangaecheokja.exception.VehicleNotFoundException;
import com.example.shinhangaecheokja.repository.MemberRepository;
import com.example.shinhangaecheokja.repository.VehicleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vehicle 관련 유스케이스(등록/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final MemberRepository memberRepository;

  /** 소유자(Member) 존재 여부와 무게/거리 유효성을 검증한 뒤 Vehicle을 등록한다. */
  @Transactional
  public VehicleResponse registerVehicle(VehicleCreateRequest request) {
    if (!memberRepository.existsById(request.getOwnerId())) {
      throw new MemberNotFoundException(request.getOwnerId());
    }
    validateWeightAndDistance(request.getMaxWeight(), request.getMaxDistance());

    Vehicle vehicle = new Vehicle();
    vehicle.setOwnerId(request.getOwnerId());
    vehicle.setType(request.getType());
    vehicle.setMaxWeight(request.getMaxWeight());
    vehicle.setMaxDistance(request.getMaxDistance());

    return VehicleResponse.from(vehicleRepository.save(vehicle));
  }

  /** id로 Vehicle 단건을 조회한다. 없으면 VehicleNotFoundException. */
  @Transactional(readOnly = true)
  public VehicleResponse getVehicle(Long vehicleId) {
    return VehicleResponse.from(findVehicleOrThrow(vehicleId));
  }

  /** 전체 Vehicle 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehicles() {
    return vehicleRepository.findAll().stream().map(VehicleResponse::from).toList();
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

  /** id로 Vehicle을 조회해 삭제한다. 없으면 VehicleNotFoundException. */
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
        .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
  }
}
