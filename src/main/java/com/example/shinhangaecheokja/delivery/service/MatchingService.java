package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.MatchingNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.NoAvailableCourierException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.delivery.util.HaversineDistanceCalculator;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Matching 관련 유스케이스(생성/자동매칭/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class MatchingService {

  private final MatchingRepository matchingRepository;
  private final DeliveryRequestRepository deliveryRequestRepository;
  private final VehicleService vehicleService;

  /** 배송 요청·차량 존재 여부와 중복 매칭 여부를 검증한 뒤 매칭을 생성한다(관리자 수동 매칭용). */
  @Transactional
  public MatchingResponse createMatching(MatchingCreateRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(request.getDeliveryRequestId());
    VehicleResponse vehicle = vehicleService.getVehicle(request.getVehicleId());
    if (vehicle.status() != VehicleStatus.AVAILABLE) {
      throw new VehicleNotAvailableException(request.getVehicleId());
    }
    if (matchingRepository.existsByDeliveryRequestId(request.getDeliveryRequestId())) {
      throw new AlreadyMatchedException(request.getDeliveryRequestId());
    }

    Matching matching = new Matching();
    matching.setDeliveryRequestId(request.getDeliveryRequestId());
    matching.setVehicleId(request.getVehicleId());
    matching.setStatus(MatchingStatus.MATCHED);
    matching.setMatchedAt(LocalDateTime.now());

    Matching saved = matchingRepository.save(matching);
    applyStatus(saved, deliveryRequest, MatchingStatus.MATCHED);
    return MatchingResponse.from(saved);
  }

  /**
   * 배송 요청의 위치·무게·거리 조건을 모두 만족하는 가용 차량 중 가장 가까운 차량을 찾아 자동으로 매칭한다. 만족하는 차량이 없으면
   * NoAvailableCourierException.
   */
  @Transactional
  public MatchingResponse autoMatch(DeliveryRequest deliveryRequest) {
    VehicleResponse best =
        vehicleService
            .getCandidateVehicles(deliveryRequest.getWeight(), deliveryRequest.getDistance())
            .stream()
            .min(
                Comparator.comparingDouble(
                        (VehicleResponse v) ->
                            HaversineDistanceCalculator.distanceKm(
                                v.latitude(),
                                v.longitude(),
                                deliveryRequest.getPickupLatitude(),
                                deliveryRequest.getPickupLongitude()))
                    .thenComparing(VehicleResponse::id))
            .orElseThrow(
                () ->
                    new NoAvailableCourierException(
                        deliveryRequest.getWeight(), deliveryRequest.getDistance()));

    Matching matching = new Matching();
    matching.setDeliveryRequestId(deliveryRequest.getId());
    matching.setVehicleId(best.id());
    matching.setStatus(MatchingStatus.MATCHED);
    matching.setMatchedAt(LocalDateTime.now());

    Matching saved = matchingRepository.save(matching);
    applyStatus(saved, deliveryRequest, MatchingStatus.MATCHED);
    return MatchingResponse.from(saved);
  }

  /** id로 매칭 단건을 조회한다. 없으면 MatchingNotFoundException. */
  @Transactional(readOnly = true)
  public MatchingResponse getMatching(Long matchingId) {
    return MatchingResponse.from(findMatchingOrThrow(matchingId));
  }

  /** 전체 매칭 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<MatchingResponse> getMatchings() {
    return matchingRepository.findAll().stream().map(MatchingResponse::from).toList();
  }

  /** 매칭 상태를 변경하고, 연결된 배송 요청·차량 상태도 함께 동기화한다. */
  @Transactional
  public MatchingResponse updateMatching(Long matchingId, MatchingUpdateRequest request) {
    Matching matching = findMatchingOrThrow(matchingId);
    MatchingStatus previousStatus = matching.getStatus();
    MatchingStatus newStatus = request.getStatus();

    if (newStatus == MatchingStatus.MATCHED && previousStatus != MatchingStatus.MATCHED) {
      VehicleResponse vehicle = vehicleService.getVehicle(matching.getVehicleId());
      if (vehicle.status() != VehicleStatus.AVAILABLE) {
        throw new VehicleNotAvailableException(matching.getVehicleId());
      }
    }

    matching.setStatus(newStatus);
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(matching.getDeliveryRequestId());
    applyStatus(matching, deliveryRequest, newStatus);
    return MatchingResponse.from(matching);
  }

  /** id로 매칭을 조회해 삭제하고, 연결된 배송 요청·차량을 매칭 이전 상태로 되돌린다. */
  @Transactional
  public void deleteMatching(Long matchingId) {
    Matching matching = findMatchingOrThrow(matchingId);
    vehicleService.markAvailable(matching.getVehicleId());
    findDeliveryRequestOrThrow(matching.getDeliveryRequestId()).setStatus(DeliveryStatus.REQUESTED);
    matchingRepository.delete(matching);
  }

  /** Matching 상태 변화에 맞춰 DeliveryRequest·Vehicle 상태를 함께 갱신한다. */
  private void applyStatus(Matching matching, DeliveryRequest deliveryRequest, MatchingStatus status) {
    deliveryRequest.setStatus(toDeliveryStatus(status));
    if (status == MatchingStatus.MATCHED) {
      vehicleService.markBusy(matching.getVehicleId());
    } else {
      vehicleService.markAvailable(matching.getVehicleId());
    }
  }

  private DeliveryStatus toDeliveryStatus(MatchingStatus status) {
    return switch (status) {
      case MATCHED -> DeliveryStatus.MATCHED;
      case COMPLETED -> DeliveryStatus.COMPLETED;
      case CANCELLED -> DeliveryStatus.CANCELLED;
    };
  }

  private Matching findMatchingOrThrow(Long matchingId) {
    return matchingRepository
        .findById(matchingId)
        .orElseThrow(() -> new MatchingNotFoundException(matchingId));
  }

  private DeliveryRequest findDeliveryRequestOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findById(deliveryRequestId)
        .orElseThrow(() -> new DeliveryRequestNotFoundException(deliveryRequestId));
  }
}
