package com.example.shinhangaecheokja.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.MatchingNotFoundException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.vehicle.exception.VehicleNotFoundException;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
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

  @Test
  void 배송요청과_차량이_존재하고_매칭되지_않았으면_매칭을_생성한다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.existsById(1L)).thenReturn(true);
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(false);
    when(matchingRepository.save(any(Matching.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MatchingResponse response = matchingService.createMatching(request);

    assertThat(response.deliveryRequestId()).isEqualTo(1L);
    assertThat(response.vehicleId()).isEqualTo(2L);
    assertThat(response.status()).isEqualTo(MatchingStatus.MATCHED);
  }

  @Test
  void 존재하지_않는_배송요청이면_DeliveryRequestNotFoundException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(999L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.existsById(999L)).thenReturn(false);

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(DeliveryRequestNotFoundException.class);
  }

  @Test
  void 존재하지_않는_차량이면_VehicleNotFoundException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(999L);

    when(deliveryRequestRepository.existsById(1L)).thenReturn(true);
    when(vehicleService.getVehicle(999L)).thenThrow(new VehicleNotFoundException(999L));

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(VehicleNotFoundException.class);
  }

  @Test
  void 이미_매칭된_배송요청이면_AlreadyMatchedException을_던진다() {
    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(1L);
    request.setVehicleId(2L);

    when(deliveryRequestRepository.existsById(1L)).thenReturn(true);
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(true);

    assertThatThrownBy(() -> matchingService.createMatching(request))
        .isInstanceOf(AlreadyMatchedException.class);
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
}
