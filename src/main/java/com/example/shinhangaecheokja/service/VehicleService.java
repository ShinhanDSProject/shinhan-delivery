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

@Service
@RequiredArgsConstructor
public class VehicleService {

  private final VehicleRepository vehicleRepository;
  private final MemberRepository memberRepository;

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

  @Transactional(readOnly = true)
  public VehicleResponse getVehicle(Long vehicleId) {
    return VehicleResponse.from(findVehicleOrThrow(vehicleId));
  }

  @Transactional(readOnly = true)
  public List<VehicleResponse> getVehicles() {
    return vehicleRepository.findAll().stream().map(VehicleResponse::from).toList();
  }

  @Transactional
  public VehicleResponse updateVehicle(Long vehicleId, VehicleUpdateRequest request) {
    validateWeightAndDistance(request.getMaxWeight(), request.getMaxDistance());

    Vehicle vehicle = findVehicleOrThrow(vehicleId);
    vehicle.setType(request.getType());
    vehicle.setMaxWeight(request.getMaxWeight());
    vehicle.setMaxDistance(request.getMaxDistance());
    return VehicleResponse.from(vehicle);
  }

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
