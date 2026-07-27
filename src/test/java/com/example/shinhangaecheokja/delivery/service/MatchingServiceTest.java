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
import com.example.shinhangaecheokja.delivery.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.MatchingNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.NoAvailableCourierException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotAvailableException;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotFoundException;
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
    return new VehicleResponse(id, 1L, VehicleType.CAR, 500, 100, 37.5, 127.0, VehicleStatus.AVAILABLE);
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

    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));
    when(vehicleService.getVehicle(2L)).thenReturn(availableVehicle(2L));
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(false);
    when(matchingRepository.save(any(Matching.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MatchingResponse response = matchingService.createMatching(request);

    assertThat(response.deliveryRequestId()).isEqualTo(1L);
    assertThat(response.vehicleId()).isEqualTo(2L);
    assertThat(response.status()).isEqualTo(MatchingStatus.MATCHED);
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.MATCHED);
    verify(vehicleService).markBusy(2L);
  }

  @Test
  void 존재하지_않는_배송요청이면_DeliveryRequestNotFoundException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(999L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(DeliveryRequestNotFoundException.class);
  }

  @Test
  void 존재하지_않는_차량이면_VehicleNotFoundException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(999L);

    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicle(999L)).thenThrow(new VehicleNotFoundException(999L));

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(VehicleNotFoundException.class);
  }

  @Test
  void 이미_BUSY인_차량이면_VehicleNotAvailableException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicle(2L))
        .thenReturn(
            new VehicleResponse(2L, 1L, VehicleType.CAR, 500, 100, 37.5, 127.0, VehicleStatus.BUSY));

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(VehicleNotAvailableException.class);
  }

  @Test
  void 이미_매칭된_배송요청이면_AlreadyMatchedException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest(1L)));
    when(vehicleService.getVehicle(2L)).thenReturn(availableVehicle(2L));
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(true);

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(AlreadyMatchedException.class);
  }

  @Test
  void 조건을_만족하는_차량_중_가장_가까운_차량을_자동_매칭한다() {
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    VehicleResponse near =
        new VehicleResponse(2L, 1L, VehicleType.CAR, 500, 100, 37.50, 127.00, VehicleStatus.AVAILABLE);
    VehicleResponse far =
        new VehicleResponse(3L, 1L, VehicleType.CAR, 500, 100, 40.00, 130.00, VehicleStatus.AVAILABLE);

    when(vehicleService.getCandidateVehicles(10, 5)).thenReturn(List.of(far, near));
    when(matchingRepository.save(any(Matching.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MatchingResponse response = matchingService.autoMatch(deliveryRequest);

    assertThat(response.vehicleId()).isEqualTo(2L);
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.MATCHED);
    verify(vehicleService).markBusy(2L);
  }

  @Test
  void 거리가_동일하면_id가_더_작은_차량을_자동_매칭한다() {
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    VehicleResponse smallerId =
        new VehicleResponse(2L, 1L, VehicleType.CAR, 500, 100, 37.50, 127.00, VehicleStatus.AVAILABLE);
    VehicleResponse largerId =
        new VehicleResponse(3L, 1L, VehicleType.CAR, 500, 100, 37.50, 127.00, VehicleStatus.AVAILABLE);

    when(vehicleService.getCandidateVehicles(10, 5)).thenReturn(List.of(largerId, smallerId));
    when(matchingRepository.save(any(Matching.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MatchingResponse response = matchingService.autoMatch(deliveryRequest);

    assertThat(response.vehicleId()).isEqualTo(2L);
  }

  @Test
  void 조건을_만족하는_차량이_없으면_NoAvailableCourierException을_던진다() {
    DeliveryRequest deliveryRequest = deliveryRequest(1L);
    when(vehicleService.getCandidateVehicles(10, 5)).thenReturn(List.of());

    assertThatThrownBy(() -> matchingService.autoMatch(deliveryRequest))
        .isInstanceOf(NoAvailableCourierException.class);
  }

  @Test
  void 존재하지_않는_매칭을_조회하면_MatchingNotFoundException을_던진다() {
    when(matchingRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> matchingService.getMatching(1L))
        .isInstanceOf(MatchingNotFoundException.class);
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
    when(vehicleService.getVehicle(2L))
        .thenReturn(
            new VehicleResponse(2L, 1L, VehicleType.CAR, 500, 100, 37.5, 127.0, VehicleStatus.BUSY));

    assertThatThrownBy(() -> matchingService.updateMatching(1L, request))
        .isInstanceOf(VehicleNotAvailableException.class);
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
    when(vehicleService.getVehicle(2L)).thenReturn(availableVehicle(2L));
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    MatchingResponse response = matchingService.updateMatching(1L, request);

    assertThat(response.status()).isEqualTo(MatchingStatus.MATCHED);
    verify(vehicleService).markBusy(2L);
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
