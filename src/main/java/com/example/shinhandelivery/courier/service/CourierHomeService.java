package com.example.shinhandelivery.courier.service;

import com.example.shinhandelivery.courier.dto.response.CourierHomePageResponse;
import com.example.shinhandelivery.courier.dto.response.CourierStatusResponse;
import com.example.shinhandelivery.courier.entity.WorkStatus;
import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import com.example.shinhandelivery.delivery.service.DeliveryMatchingService;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 배송원 홈 화면 SSR 렌더링에 필요한 기사·차량·상태·대기배달 데이터를 조합하는 서비스. */
@Service
@RequiredArgsConstructor
public class CourierHomeService {

  private final MemberService memberService;
  private final CourierStatusService courierStatusService;
  private final VehicleService vehicleService;
  private final DeliveryMatchingService deliveryMatchingService;

  /** 로그인 배송원의 홈 화면 데이터를 조합한다. */
  @Transactional(readOnly = true)
  public CourierHomePageResponse load(Long memberId) {
    Member member = memberService.getById(memberId);
    CourierStatusResponse statusResponse = courierStatusService.getWorkStatus(memberId);

    List<Vehicle> vehicles = vehicleService.getVehiclesByMemberId(memberId);
    String transportMode =
        formatTransportMode(vehicles.isEmpty() ? null : vehicles.get(0).getType());

    boolean isOnline = statusResponse.getWorkStatus() == WorkStatus.ONLINE;
    List<AvailableDeliveryResponse> availableDeliveries = Collections.emptyList();

    if (isOnline) {
      availableDeliveries =
          deliveryMatchingService.getAvailableDeliveries(
              memberId, statusResponse.getLatitude(), statusResponse.getLongitude(), 3.0);
    }

    return new CourierHomePageResponse(
        member.getName(),
        transportMode,
        statusResponse.getWorkStatus().name(),
        isOnline,
        statusResponse.getLatitude(),
        statusResponse.getLongitude(),
        availableDeliveries,
        availableDeliveries.size());
  }

  private String formatTransportMode(VehicleType type) {
    if (type == null) {
      return "🛵 운송수단 미등록";
    }
    return switch (type) {
      case MOTORCYCLE -> "🛵 오토바이";
      case CAR -> "🚗 승용차";
      case DRONE -> "🚁 드론";
    };
  }
}
