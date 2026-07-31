package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.InvalidMatchingTransitionException;
import com.example.shinhangaecheokja.delivery.exception.VehicleCapacityMismatchException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Matching 관련 유스케이스(콜 수락/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class MatchingService {

  private final MatchingRepository matchingRepository;
  private final DeliveryRequestRepository deliveryRequestRepository;
  private final VehicleService vehicleService;

  /**
   * 매칭을 생성한다. 배송 요청과 차량을 각각 findByIdForUpdate(비관적 락)로 조회해 조건(상태, 무게, 거리)을 검증하고, Matching 엔티티를 저장한다.
   */
  @Transactional
  public Matching createMatching(MatchingCreateRequest request) {
    DeliveryRequest deliveryRequest =
        findDeliveryRequestForUpdateOrThrow(request.getDeliveryRequestId());
    if (deliveryRequest.getStatus() != DeliveryStatus.REQUESTED) {
      throw new AlreadyMatchedException(
          request.getDeliveryRequestId(), deliveryRequest.getStatus());
    }

    VehicleResponse vehicle = vehicleService.getVehicleForUpdate(request.getVehicleId());
    if (vehicle.status() != VehicleStatus.AVAILABLE) {
      throw new VehicleNotAvailableException(request.getVehicleId());
    }
    if (vehicle.maxWeight() < deliveryRequest.getWeight()
        || vehicle.maxDistance() < deliveryRequest.getDistance()) {
      throw new VehicleCapacityMismatchException(
          request.getVehicleId(), deliveryRequest.getWeight(), deliveryRequest.getDistance());
    }

    Matching matching = Matching.from(request);

    Matching saved = matchingRepository.save(matching);
    applyStatus(saved, deliveryRequest, MatchingStatus.MATCHED);
    return saved;
  }

  /**
   * 차량이 지금 수락할 수 있는 열린 콜(REQUESTED 상태이면서 그 차량의 무게·거리 조건을 만족하는 배송 요청) 목록을 조회한다. 차량이 AVAILABLE이 아니면
   * 어차피 수락할 수 없으므로 빈 목록을 반환한다.
   */
  @Transactional(readOnly = true)
  public List<DeliveryRequest> getOpenCalls(Long vehicleId) {
    VehicleResponse vehicle = vehicleService.getVehicle(vehicleId);
    if (vehicle.status() != VehicleStatus.AVAILABLE) {
      return List.of();
    }
    return deliveryRequestRepository.findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
        DeliveryStatus.REQUESTED, vehicle.maxWeight(), vehicle.maxDistance());
  }

  /** id로 매칭 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public MatchingResponse getMatching(Long matchingId) {
    return MatchingResponse.from(findMatchingOrThrow(matchingId));
  }

  /** 전체 매칭 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<MatchingResponse> getMatchings() {
    return matchingRepository.findAll().stream().map(MatchingResponse::from).toList();
  }

  /** 배송 요청 id로 매칭을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public MatchingResponse getMatchingByDeliveryRequestId(Long deliveryRequestId) {
    return matchingRepository
        .findByDeliveryRequestId(deliveryRequestId)
        .map(MatchingResponse::from)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATCHING_NOT_FOUND));
  }

  /** 매칭 상태를 변경하고, 연결된 배송 요청·차량 상태도 함께 동기화한다. */
  @Transactional
  public MatchingResponse updateMatching(Long matchingId, MatchingUpdateRequest request) {
    Matching matching = findMatchingOrThrow(matchingId);
    MatchingStatus previousStatus = matching.getStatus();
    MatchingStatus newStatus = request.getStatus();

    validateTransition(previousStatus, newStatus);

    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(matching.getDeliveryRequestId());

    if (newStatus == MatchingStatus.MATCHED && previousStatus != MatchingStatus.MATCHED) {
      VehicleResponse vehicle = vehicleService.getVehicleForUpdate(matching.getVehicleId());
      if (vehicle.status() != VehicleStatus.AVAILABLE) {
        throw new VehicleNotAvailableException(matching.getVehicleId());
      }
      if (vehicle.maxWeight() < deliveryRequest.getWeight()
          || vehicle.maxDistance() < deliveryRequest.getDistance()) {
        throw new VehicleCapacityMismatchException(
            matching.getVehicleId(), deliveryRequest.getWeight(), deliveryRequest.getDistance());
      }
    }

    matching.setStatus(newStatus);
    applyStatus(matching, deliveryRequest, newStatus);
    return MatchingResponse.from(matching);
  }

  /**
   * id로 매칭을 조회해 삭제한다. 없으면 EntityNotFoundException. 매칭이 진행 중(MATCHED)이었던 경우에만 연결된 배송 요청·차량을 매칭 이전
   * 상태로 되돌린다 — 이미 COMPLETED/CANCELLED된 매칭은 배송 요청 상태가 이미 그에 맞게 반영되어 있으므로 REQUESTED로 되돌리지 않는다.
   */
  @Transactional
  public void deleteMatching(Long matchingId) {
    Matching matching = findMatchingOrThrow(matchingId);
    if (matching.getStatus() == MatchingStatus.MATCHED) {
      vehicleService.markAvailable(matching.getVehicleId());
      findDeliveryRequestOrThrow(matching.getDeliveryRequestId())
          .setStatus(DeliveryStatus.REQUESTED);
    }
    matchingRepository.delete(matching);
  }

  /**
   * 매칭 상태 전이가 허용되는지 검증한다. COMPLETED는 종료 상태라 다른 상태로 전이할 수 없고, CANCELLED는 재매칭(MATCHED)만 허용한다.
   * MATCHED에서는 어디로든 전이 가능하다.
   */
  private void validateTransition(MatchingStatus from, MatchingStatus to) {
    boolean allowed =
        switch (from) {
          case MATCHED -> true;
          case CANCELLED -> to == MatchingStatus.CANCELLED || to == MatchingStatus.MATCHED;
          case COMPLETED -> to == MatchingStatus.COMPLETED;
        };
    if (!allowed) {
      throw new InvalidMatchingTransitionException(from, to);
    }
  }

  /** Matching 상태 변화에 맞춰 DeliveryRequest·Vehicle 상태를 함께 갱신한다. */
  private void applyStatus(
      Matching matching, DeliveryRequest deliveryRequest, MatchingStatus status) {
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
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATCHING_NOT_FOUND));
  }

  private DeliveryRequest findDeliveryRequestOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findById(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }

  private DeliveryRequest findDeliveryRequestForUpdateOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findByIdForUpdate(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }
}
