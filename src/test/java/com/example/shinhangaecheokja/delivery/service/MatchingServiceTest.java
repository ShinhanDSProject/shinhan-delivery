package com.example.shinhangaecheokja.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.InvalidMatchingTransitionException;
import com.example.shinhangaecheokja.delivery.exception.VehicleCapacityMismatchException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

  @Mock private MatchingRepository matchingRepository;
  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private VehicleService vehicleService;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private MatchingService matchingService;

  private Vehicle availableVehicle(Long id) {
    Vehicle vehicle = new Vehicle();
    vehicle.setId(id);
    vehicle.setOwnerId(1L);
    vehicle.setType(VehicleType.CAR);
    vehicle.setMaxWeight(500);
    vehicle.setMaxDistance(100);
    vehicle.setLatitude(37.5);
    vehicle.setLongitude(127.0);
    vehicle.setStatus(VehicleStatus.AVAILABLE);
    return vehicle;
  }

  private Vehicle vehicleWithStatusAndCapacity(
      Long id, VehicleStatus status, double weight, double distance) {
    Vehicle vehicle = new Vehicle();
    vehicle.setId(id);
    vehicle.setOwnerId(1L);
    vehicle.setType(VehicleType.CAR);
    vehicle.setMaxWeight(weight);
    vehicle.setMaxDistance(distance);
    vehicle.setLatitude(37.5);
    vehicle.setLongitude(127.0);
    vehicle.setStatus(status);
    return vehicle;
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
  @DisplayName("배송요청과 차량이 존재하고 매칭되지 않았으면 매칭을 생성한다")
  void createMatchingSuccess() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);

    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));
    when(vehicleService.getVehicleForUpdate(2L)).thenReturn(availableVehicle(2L));
    when(matchingRepository.save(any(Matching.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Matching response = matchingService.create(request);

    assertThat(response.getDeliveryRequestId()).isEqualTo(1L);
    assertThat(response.getVehicleId()).isEqualTo(2L);
    assertThat(response.getStatus()).isEqualTo(MatchingStatus.MATCHED);
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.MATCHED);
    verify(vehicleService).markBusy(2L);

    ArgumentCaptor<DeliveryStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(DeliveryStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().deliveryRequestId()).isEqualTo(1L);
    assertThat(eventCaptor.getValue().status()).isEqualTo(DeliveryStatus.MATCHED);
  }

  @Test
  @DisplayName("존재하지 않는 배송요청이면 EntityNotFoundException을 던진다")
  void createMatchingDeliveryNotFoundShouldThrowException() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(999L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchingService.create(request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하지 않는 차량이면 EntityNotFoundException을 던진다")
  void createMatchingVehicleNotFoundShouldThrowException() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(999L);

    when(deliveryRequestRepository.findByIdForUpdate(1L))
        .thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(999L))
        .thenThrow(new EntityNotFoundException(ErrorCode.VEHICLE_NOT_FOUND));

    assertThatThrownBy(() -> matchingService.create(request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("이미 BUSY인 차량이면 VehicleNotAvailableException을 던진다")
  void createMatchingVehicleBusyShouldThrowException() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findByIdForUpdate(1L))
        .thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(vehicleWithStatusAndCapacity(2L, VehicleStatus.BUSY, 500, 100));

    assertThatThrownBy(() -> matchingService.create(request))
        .isInstanceOf(VehicleNotAvailableException.class);
  }

  @Test
  @DisplayName("차량이 무게 거리를 감당하지 못하면 VehicleCapacityMismatchException을 던진다")
  void createMatchingCapacityMismatchShouldThrowException() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findByIdForUpdate(1L))
        .thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(vehicleWithStatusAndCapacity(2L, VehicleStatus.AVAILABLE, 5, 100));

    assertThatThrownBy(() -> matchingService.create(request))
        .isInstanceOf(VehicleCapacityMismatchException.class);
  }

  @Test
  @DisplayName("이미 매칭된 배송요청이면 AlreadyMatchedException을 던진다")
  void createMatchingAlreadyMatchedShouldThrowException() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);

    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> matchingService.create(request))
        .isInstanceOf(AlreadyMatchedException.class);
  }

  @Test
  @DisplayName("차량이 가용하면 조건에 맞는 열린 콜 목록을 반환한다")
  void getOpenCallsAvailableVehicleShouldReturnOpenCalls() {
    DeliveryRequest deliveryRequest = deliveryRequest(1L);

    when(vehicleService.getById(2L)).thenReturn(availableVehicle(2L));
    when(deliveryRequestRepository.findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
            DeliveryStatus.REQUESTED, 500, 100))
        .thenReturn(List.of(deliveryRequest));

    List<DeliveryRequest> calls = matchingService.getOpenCalls(2L);

    assertThat(calls).hasSize(1);
    assertThat(calls.get(0).getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("차량이 BUSY면 열린 콜 목록이 비어있다")
  void getOpenCallsBusyVehicleShouldReturnEmptyList() {
    when(vehicleService.getById(2L))
        .thenReturn(vehicleWithStatusAndCapacity(2L, VehicleStatus.BUSY, 500, 100));

    List<DeliveryRequest> calls = matchingService.getOpenCalls(2L);

    assertThat(calls).isEmpty();
  }

  @Test
  @DisplayName("존재하지 않는 차량으로 열린 콜을 조회하면 EntityNotFoundException을 던진다")
  void getOpenCallsVehicleNotFoundShouldThrowException() {
    when(vehicleService.getById(999L))
        .thenThrow(new EntityNotFoundException(ErrorCode.VEHICLE_NOT_FOUND));

    assertThatThrownBy(() -> matchingService.getOpenCalls(999L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하지 않는 매칭을 조회하면 EntityNotFoundException을 던진다")
  void getMatchingNotFoundShouldThrowException() {
    when(matchingRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchingService.getById(1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하는 매칭을 조회하면 Matching을 반환한다")
  void getMatchingSuccess() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.MATCHED);
    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));

    Matching response = matchingService.getById(1L);

    assertThat(response.getDeliveryRequestId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("매칭 상태를 COMPLETED로 변경하면 차량이 AVAILABLE로 복귀하고 배송요청 상태도 동기화된다")
  void updateMatchingToCompletedShouldSynchronizeStatus() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.MATCHED);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.COMPLETED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    Matching response = matchingService.update(1L, request);

    assertThat(response.getStatus()).isEqualTo(MatchingStatus.COMPLETED);
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    verify(vehicleService).markAvailable(2L);
    verify(vehicleService, never()).markBusy(eq(2L));

    ArgumentCaptor<DeliveryStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(DeliveryStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().deliveryRequestId()).isEqualTo(1L);
    assertThat(eventCaptor.getValue().status()).isEqualTo(DeliveryStatus.COMPLETED);
  }

  @Test
  @DisplayName("취소된 매칭을 MATCHED로 되돌릴때 차량이 다른 건으로 BUSY면 VehicleNotAvailableException을 던진다")
  void updateMatchingRevertMatchedWhenBusyShouldThrowException() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.CANCELLED);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.MATCHED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(vehicleWithStatusAndCapacity(2L, VehicleStatus.BUSY, 500, 100));

    assertThatThrownBy(() -> matchingService.update(1L, request))
        .isInstanceOf(VehicleNotAvailableException.class);
    verify(vehicleService, never()).markBusy(2L);
  }

  @Test
  @DisplayName("취소된 매칭을 MATCHED로 되돌릴때 차량이 용량을 감당못하면 VehicleCapacityMismatchException을 던진다")
  void updateMatchingRevertMatchedWhenCapacityExceededShouldThrowException() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.CANCELLED);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.MATCHED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicleForUpdate(2L))
        .thenReturn(vehicleWithStatusAndCapacity(2L, VehicleStatus.AVAILABLE, 5, 100));

    assertThatThrownBy(() -> matchingService.update(1L, request))
        .isInstanceOf(VehicleCapacityMismatchException.class);
    verify(vehicleService, never()).markBusy(2L);
  }

  @Test
  @DisplayName("취소된 매칭을 MATCHED로 되돌릴때 차량이 가용하면 다시 매칭된다")
  void updateMatchingRevertMatchedWhenAvailableShouldReMatch() {
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

    Matching response = matchingService.update(1L, request);

    assertThat(response.getStatus()).isEqualTo(MatchingStatus.MATCHED);
    verify(vehicleService).markBusy(2L);
  }

  @Test
  @DisplayName("COMPLETED된 매칭을 다른 상태로 바꾸려하면 InvalidMatchingTransitionException을 던진다")
  void updateMatchingFromCompletedShouldThrowException() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.COMPLETED);
    MatchingUpdateRequest request = new MatchingUpdateRequest();
    request.setStatus(MatchingStatus.CANCELLED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));

    assertThatThrownBy(() -> matchingService.update(1L, request))
        .isInstanceOf(InvalidMatchingTransitionException.class);
  }

  @Test
  @DisplayName("매칭을 삭제하면 차량이 AVAILABLE로 복귀하고 배송요청 상태가 REQUESTED로 복귀한다")
  void deleteMatchingMatchedStatusShouldRevertStatus() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.MATCHED);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    matchingService.delete(1L);

    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    verify(vehicleService).markAvailable(2L);
    verify(matchingRepository).delete(matching);

    ArgumentCaptor<DeliveryStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(DeliveryStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().deliveryRequestId()).isEqualTo(1L);
    assertThat(eventCaptor.getValue().status()).isEqualTo(DeliveryStatus.REQUESTED);
  }

  @Test
  @DisplayName("이미 COMPLETED된 매칭을 삭제해도 배송요청 상태를 되돌리지 않는다")
  void deleteMatchingCompletedStatusShouldNotRevertStatus() {
    Matching matching = new Matching();
    matching.setDeliveryRequestId(1L);
    matching.setVehicleId(2L);
    matching.setStatus(MatchingStatus.COMPLETED);
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);

    when(matchingRepository.findById(1L)).thenReturn(Optional.of(matching));

    matchingService.delete(1L);

    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    verify(vehicleService, never()).markAvailable(2L);
    verify(matchingRepository).delete(matching);
    verify(eventPublisher, never()).publishEvent(any());
  }
}
