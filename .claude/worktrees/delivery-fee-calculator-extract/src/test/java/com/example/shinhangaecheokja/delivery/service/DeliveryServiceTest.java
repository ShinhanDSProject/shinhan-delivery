package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryDetailResponseDto;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryWeightException;
import com.example.shinhandelivery.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhandelivery.delivery.helper.DeliveryFeeCalculator;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private MemberService memberService;
  @Mock private VehicleService vehicleService;
  @Mock private MatchingRepository matchingRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Spy private DeliveryFeeCalculator deliveryFeeCalculator = new DeliveryFeeCalculator();
  @InjectMocks private DeliveryService deliveryService;

  @Test
  @DisplayName("고객이 존재하면 REQUESTED 상태로 배송을 요청한다")
  void requestDeliverySuccess() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    DeliveryRequest response = deliveryService.requestDelivery(request);

    assertThat(response.getCustomerId()).isEqualTo(1L);
    assertThat(response.getFeePoint()).isEqualTo(78776L);
    assertThat(response.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
  }
}
