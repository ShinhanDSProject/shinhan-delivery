package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Matching 관련 유스케이스(생성/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class MatchingService {

  private final MatchingRepository matchingRepository;
  private final DeliveryRequestRepository deliveryRequestRepository;
  private final VehicleService vehicleService;

  /** 배송 요청·차량 존재 여부와 중복 매칭 여부를 검증한 뒤 매칭을 생성한다. */
  @Transactional
  public MatchingResponse createMatching(MatchingCreateRequest request) {
    if (!deliveryRequestRepository.existsById(request.getDeliveryRequestId())) {
      throw new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND);
    }
    vehicleService.getVehicle(request.getVehicleId());
    if (matchingRepository.existsByDeliveryRequestId(request.getDeliveryRequestId())) {
      throw new AlreadyMatchedException(request.getDeliveryRequestId());
    }

    Matching matching = new Matching();
    matching.setDeliveryRequestId(request.getDeliveryRequestId());
    matching.setVehicleId(request.getVehicleId());
    matching.setStatus(MatchingStatus.MATCHED);
    matching.setMatchedAt(LocalDateTime.now());

    return MatchingResponse.from(matchingRepository.save(matching));
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

  /** 매칭 상태를 변경한다. */
  @Transactional
  public MatchingResponse updateMatching(Long matchingId, MatchingUpdateRequest request) {
    Matching matching = findMatchingOrThrow(matchingId);
    matching.setStatus(request.getStatus());
    return MatchingResponse.from(matching);
  }

  /** id로 매칭을 조회해 삭제한다. 없으면 EntityNotFoundException. */
  @Transactional
  public void deleteMatching(Long matchingId) {
    Matching matching = findMatchingOrThrow(matchingId);
    matchingRepository.delete(matching);
  }

  private Matching findMatchingOrThrow(Long matchingId) {
    return matchingRepository
        .findById(matchingId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }
}
