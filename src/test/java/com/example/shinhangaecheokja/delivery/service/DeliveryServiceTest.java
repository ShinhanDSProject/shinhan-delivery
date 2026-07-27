package com.example.shinhangaecheokja.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.exception.NoAvailableCourierException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private MemberService memberService;
  @Mock private VehicleService vehicleService;
  @InjectMocks private DeliveryService deliveryService;

  @Test
  void 고객이_존재하고_가용_차량이_있으면_배송을_요청한다() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setDistance(5);

    when(vehicleService.existsAvailableVehicle(10, 5)).thenReturn(true);
    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    DeliveryResponse response = deliveryService.requestDelivery(request);

    assertThat(response.customerId()).isEqualTo(1L);
    assertThat(response.status()).isEqualTo(DeliveryStatus.REQUESTED);
    assertThat(response.feePoint()).isEqualTo(600L);
  }

  @Test
  void 존재하지_않는_고객이면_EntityNotFoundException을_던진다() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(999L);
    request.setWeight(10);
    request.setDistance(5);

    when(memberService.getMember(999L))
        .thenThrow(
            new com.example.shinhangaecheokja.common.exception.EntityNotFoundException(
                com.example.shinhangaecheokja.common.exception.ErrorCode.MEMBER_NOT_FOUND));

    assertThatThrownBy(() -> deliveryService.requestDelivery(request))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 가용_차량이_없으면_NoAvailableCourierException을_던진다() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setWeight(1000);
    request.setDistance(500);

    when(vehicleService.existsAvailableVehicle(1000, 500)).thenReturn(false);

    assertThatThrownBy(() -> deliveryService.requestDelivery(request))
        .isInstanceOf(NoAvailableCourierException.class);
  }

  @Test
  void 존재하지_않는_배송_요청을_조회하면_EntityNotFoundException을_던진다() {
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deliveryService.getDeliveryRequest(1L))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 존재하는_배송_요청을_조회하면_DeliveryResponse를_반환한다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(1L);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryResponse response = deliveryService.getDeliveryRequest(1L);

    assertThat(response.customerId()).isEqualTo(1L);
  }
}
