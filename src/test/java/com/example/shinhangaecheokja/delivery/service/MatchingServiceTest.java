package com.example.shinhangaecheokja.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

  @Mock private MatchingRepository matchingRepository;
  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private VehicleService vehicleService;
  @InjectMocks private MatchingService matchingService;

  private VehicleResponse availableVehicle(Long id) {
    return new VehicleResponse(
        id, 1L, VehicleType.CAR, 500, 100, 37.5, 127.0, VehicleStatus.AVAILABLE);
  }

  private DeliveryRequest deliveryRequest(Long id) {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setId(id);
    deliveryRequest.setCustomerId(1L);
    deliveryRequest.setWeight(10);
    deliveryRequest.setDistance(5);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setPickupLatitude(37.5);
    deliveryRequest.setPickupLongitude(127.0);
    return deliveryRequest;
  }

  @Test
  void 배송요청과_차량이_존재하고_매칭되지_않았으면_매칭을_생성한다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);

    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));
    when(vehicleService.getVehicleForUpdate(2L)).thenReturn(availableVehicle(2L));
    when(matchingRepository.save(any(Matching.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Matching response = matchingService.createMatching(request);

    assertThat(response.getDeliveryRequestId()).isEqualTo(1L);
    assertThat(response.getVehicleId()).isEqualTo(2L);
    assertThat(response.getStatus()).isEqualTo(MatchingStatus.MATCHED);
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.MATCHED);
    verify(vehicleService).markBusy(2L);
  }

  @Test
  void 존재하지_않는_배송요청이면_EntityNotFoundException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(999L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 존재하지_않는_차량이면_EntityNotFoundException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(999L);

    when(deliveryRequestRepository.findByIdForUpdate(1L))
        .thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(999L))
        .thenThrow(
            new com.example.shinhangaecheokja.common.exception.EntityNotFoundException(
                com.example.shinhangaecheokja.common.exception.ErrorCode.VEHICLE_NOT_FOUND));

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 이미_BUSY인_차량이면_VehicleNotAvailableException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findByIdForUpdate(1L))
        .thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(
            new VehicleResponse(
                2L, 1L, VehicleType.CAR, 500, 100, 37.5, 127.0, VehicleStatus.BUSY));

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(VehicleNotAvailableException.class);
  }

  @Test
  void 차량이_무게_거리를_감당하지_못하면_VehicleCapacityMismatchException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findByIdForUpdate(1L))
        .thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(
            new VehicleResponse(
                2L, 1L, VehicleType.CAR, 5, 100, 37.5, 127.0, VehicleStatus.AVAILABLE));

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(VehicleCapacityMismatchException.class);
  }

  @Test
  void 이미_매칭된_배송요청이면_AlreadyMatchedException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);

    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(AlreadyMatchedException.class);
  }

  @Test
  void 차량이_가용하면_조건에_맞는_열린_콜_목록을_반환한다() {
    DeliveryRequest deliveryRequest = deliveryRequest(1L);

    when(vehicleService.getVehicle(2L)).thenReturn(availableVehicle(2L));
    when(deliveryRequestRepository.findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
            DeliveryStatus.REQUESTED, 500, 100))
        .thenReturn(List.of(deliveryRequest));

    List<DeliveryRequest> calls = matchingService.getOpenCalls(2L);

    assertThat(calls).hasSize(1);
    assertThat(calls.get(0).getId()).isEqualTo(1L);
  }

  @Test
  void 차량이_BUSY면_열린_콜_목록이_비어있다() {
    when(vehicleService.getVehicle(2L))
        .thenReturn(
            new VehicleResponse(
                2L, 1L, VehicleType.CAR, 500, 100, 37.5, 127.0, VehicleStatus.BUSY));

    List<DeliveryRequest> calls = matchingService.getOpenCalls(2L);

    assertThat(calls).isEmpty();
  }

  @Test
  void 존재하지_않는_차량으로_열린_콜을_조회하면_EntityNotFoundException을_던진다() {
    when(vehicleService.getVehicle(999L))
        .thenThrow(
            new com.example.shinhangaecheokja.common.exception.EntityNotFoundException(
                com.example.shinhangaecheokja.common.exception.ErrorCode.VEHICLE_NOT_FOUND));

    assertThatThrownBy(() -> matchingService.getOpenCalls(999L))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 존재하지_않는_매칭을_조회하면_EntityNotFoundException을_던진다() {
    when(matchingRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchingService.getMatching(1L))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 존재하는_매칭을_조회하면_MatchingResponse를_반환한다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.MATCHED);
    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));

    MatchingResponse response = matchingService.getMatching(1L);

    assertThat(response.deliveryRequestId()).isEqualTo(1L);
  }

  @Test
  void 매칭_상태를_COMPLETED로_변경하면_차량이_AVAILABLE로_복귀하고_배송요청_상태도_동기화된다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.MATCHED);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.COMPLETED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    MatchingResponse response = matchingService.updateMatching(1L, request);

    assertThat(response.status()).isEqualTo(MatchingStatus.COMPLETED);
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    verify(vehicleService).markAvailable(2L);
    verify(vehicleService, never()).markBusy(eq(2L));
  }

  @Test
  void 취소된_매칭을_MATCHED로_되돌릴때_차량이_다른_건으로_BUSY면_VehicleNotAvailableException을_던진다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.CANCELLED);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.MATCHED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(
            new VehicleResponse(
                2L, 1L, VehicleType.CAR, 500, 100, 37.5, 127.0, VehicleStatus.BUSY));

    assertThatThrownBy(() -> matchingService.updateMatching(1L, request))
        .isInstanceOf(VehicleNotAvailableException.class);
    verify(vehicleService, never()).markBusy(2L);
  }

  @Test
  void 취소된_매칭을_MATCHED로_되돌릴때_차량이_용량을_감당못하면_VehicleCapacityMismatchException을_던진다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.CANCELLED);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.MATCHED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(
            new VehicleResponse(
                2L, 1L, VehicleType.CAR, 5, 100, 37.5, 127.0, VehicleStatus.AVAILABLE));

    assertThatThrownBy(() -> matchingService.updateMatching(1L, request))
        .isInstanceOf(VehicleCapacityMismatchException.class);
    verify(vehicleService, never()).markBusy(2L);
  }

  @Test
  void 취소된_매칭을_MATCHED로_되돌릴때_차량이_가용하면_다시_매칭된다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.CANCELLED);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.MATCHED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(vehicleService.getVehicleForUpdate(2L)).thenReturn(availableVehicle(2L));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    MatchingResponse response = matchingService.updateMatching(1L, request);

    assertThat(response.status()).isEqualTo(MatchingStatus.MATCHED);
    verify(vehicleService).markBusy(2L);
  }

  @Test
  void COMPLETED된_매칭을_다른_상태로_바꾸려하면_InvalidMatchingTransitionException을_던진다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.COMPLETED);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.CANCELLED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));

    assertThatThrownBy(() -> matchingService.updateMatching(1L, request))
        .isInstanceOf(InvalidMatchingTransitionException.class);
  }

  @Test
  void 매칭을_삭제하면_차량이_AVAILABLE로_복귀하고_배송요청_상태가_REQUESTED로_복귀한다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.MATCHED);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    matchingService.deleteMatching(1L);

    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    verify(vehicleService).markAvailable(2L);
    verify(matchingRepository).delete(matching);
  }

  @Test
  void 이미_COMPLETED된_매칭을_삭제해도_배송요청_상태를_되돌리지_않는다() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.COMPLETED);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));

    matchingService.deleteMatching(1L);

    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    verify(vehicleService, never()).markAvailable(2L);
    verify(matchingRepository).delete(matching);
  }
}
