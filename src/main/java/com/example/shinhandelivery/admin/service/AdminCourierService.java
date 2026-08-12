package com.example.shinhandelivery.admin.service;

import com.example.shinhandelivery.admin.dto.response.PendingCourierResponseDto;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 전용 배송원 자격 검증 및 승인/거절 전담 서비스. */
@Service
@RequiredArgsConstructor
public class AdminCourierService {

  private final MemberService memberService;

  /** 승인 대기(PENDING) 상태인 배송원 목록을 페이징 조회한다. */
  @Transactional(readOnly = true)
  public Page<PendingCourierResponseDto> getPendingCouriers(Pageable pageable) {
    return memberService.getPendingCouriers(pageable).map(PendingCourierResponseDto::from);
  }

  /** 특정 배송원의 자격 심사를 승인(APPROVED) 처리한다. */
  @Transactional
  public PendingCourierResponseDto approveCourier(Long courierId) {
    Member courier = memberService.approveCourier(courierId);
    return PendingCourierResponseDto.from(courier);
  }

  /** 특정 배송원의 자격 심사를 거절(REJECTED) 처리한다. */
  @Transactional
  public PendingCourierResponseDto rejectCourier(Long courierId) {
    Member courier = memberService.rejectCourier(courierId);
    return PendingCourierResponseDto.from(courier);
  }
}
