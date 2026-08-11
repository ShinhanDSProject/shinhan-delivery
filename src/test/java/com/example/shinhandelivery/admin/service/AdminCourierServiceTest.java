package com.example.shinhandelivery.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.admin.dto.response.PendingCourierResponseDto;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.entity.CourierApprovalStatus;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminCourierServiceTest {

  @Mock private MemberService memberService;
  @InjectMocks private AdminCourierService adminCourierService;

  @Test
  @DisplayName("승인 대기(PENDING) 상태인 배송원 목록을 페이징하여 반환한다")
  void getPendingCouriersReturnsPage() {
    Member courier =
        Member.builder()
            .id(1L)
            .email("courier@example.com")
            .name("박배송")
            .phoneNumber("010-1234-5678")
            .role(MemberRole.COURIER)
            .courierApprovalStatus(CourierApprovalStatus.PENDING)
            .proofDocumentUrl("http://localhost:8080/uploads/license.png")
            .build();

    Page<Member> memberPage = new PageImpl<>(List.of(courier));
    when(memberService.getPendingCouriers(any())).thenReturn(memberPage);

    Page<PendingCourierResponseDto> result =
        adminCourierService.getPendingCouriers(PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).name()).isEqualTo("박배송");
    assertThat(result.getContent().get(0).courierApprovalStatus())
        .isEqualTo(CourierApprovalStatus.PENDING);
    assertThat(result.getContent().get(0).proofDocumentUrl())
        .isEqualTo("http://localhost:8080/uploads/license.png");
  }

  @Test
  @DisplayName("배송원의 자격 심사를 승인하면 상태가 APPROVED로 변경된다")
  void approveCourierUpdatesStatusToApproved() {
    Member courier =
        Member.builder()
            .id(1L)
            .email("courier@example.com")
            .name("박배송")
            .role(MemberRole.COURIER)
            .courierApprovalStatus(CourierApprovalStatus.APPROVED)
            .build();

    when(memberService.approveCourier(1L)).thenReturn(courier);

    PendingCourierResponseDto result = adminCourierService.approveCourier(1L);

    assertThat(result.courierApprovalStatus()).isEqualTo(CourierApprovalStatus.APPROVED);
  }

  @Test
  @DisplayName("배송원의 자격 심사를 거절하면 상태가 REJECTED로 변경된다")
  void rejectCourierUpdatesStatusToRejected() {
    Member courier =
        Member.builder()
            .id(1L)
            .email("courier@example.com")
            .name("박배송")
            .role(MemberRole.COURIER)
            .courierApprovalStatus(CourierApprovalStatus.REJECTED)
            .build();

    when(memberService.rejectCourier(1L)).thenReturn(courier);

    PendingCourierResponseDto result = adminCourierService.rejectCourier(1L);

    assertThat(result.courierApprovalStatus()).isEqualTo(CourierApprovalStatus.REJECTED);
  }

  @Test
  @DisplayName("배송원이 아닌 계정을 승인 시도하면 예외가 발생한다")
  void approveNonCourierThrowsException() {
    when(memberService.approveCourier(2L))
        .thenThrow(
            new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송원 계정만 자격 심사를 진행할 수 있습니다."));

    assertThatThrownBy(() -> adminCourierService.approveCourier(2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("배송원 계정만");
  }
}
