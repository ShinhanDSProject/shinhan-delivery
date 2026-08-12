package com.example.shinhandelivery.member.service;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.JwtProvider;
import com.example.shinhandelivery.member.dto.request.LoginRequest;
import com.example.shinhandelivery.member.dto.request.MemberCreateRequest;
import com.example.shinhandelivery.member.dto.request.MemberPasswordUpdateRequest;
import com.example.shinhandelivery.member.dto.request.MemberPaymentPinUpdateRequest;
import com.example.shinhandelivery.member.dto.request.MemberProfileUpdateRequestDto;
import com.example.shinhandelivery.member.dto.request.MemberUpdateRequest;
import com.example.shinhandelivery.member.dto.response.TokenResponse;
import com.example.shinhandelivery.member.entity.CourierApprovalStatus;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.exception.DuplicateMemberException;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Member 관련 유스케이스(가입/조회/수정/역할변경/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final VehicleService vehicleService;

  /** 이메일 중복을 검증하고 비밀번호를 암호화해 회원을 생성한다 (Entity 리턴). */
  @Transactional
  public Member create(MemberCreateRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateMemberException(request.getEmail());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    Member created = memberRepository.save(Member.from(request, encodedPassword));
    if (created.getRole() == MemberRole.COURIER) {
      vehicleService.save(createDefaultVehicle(created.getId(), request));
    }
    return created;
  }

  /** 이메일과 비밀번호를 검증하고 JWT Access/Refresh 토큰을 발급한다. */
  @Transactional(readOnly = true)
  public TokenResponse login(LoginRequest request) {
    Member member =
        memberRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken =
        jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole().name());
    String refreshToken = jwtProvider.createRefreshToken(member.getId(), member.getEmail());

    return TokenResponse.of(accessToken, refreshToken);
  }

  /** id로 회원 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public Member getById(Long memberId) {
    return findMemberOrThrow(memberId);
  }

  /** 전체 회원 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<Member> list() {
    return memberRepository.findAll();
  }

  /** 로그인한 본인의 프로필 정보를 조회한다. */
  @Transactional(readOnly = true)
  public Member getMyProfile(Long memberId) {
    return findMemberOrThrow(memberId);
  }

  /** 결제 PIN이 설정된 회원만 후속 기능을 사용하도록 검증한다. */
  @Transactional(readOnly = true)
  public void requirePaymentPinConfigured(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    if (member.getPinHash() == null || member.getPinHash().isBlank()) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "결제 PIN 설정 후 이용할 수 있습니다.");
    }
  }

  /** 로그인한 본인의 프로필 정보(이름, 연락처)를 수정한다. */
  @Transactional
  public Member updateMyProfile(Long memberId, MemberProfileUpdateRequestDto request) {
    return findMemberOrThrow(memberId).updateProfileBy(request);
  }

  /** 현재 비밀번호를 확인한 뒤 새 비밀번호를 BCrypt로 암호화해 저장한다. */
  @Transactional
  public void updatePassword(Long memberId, MemberPasswordUpdateRequest request) {
    Member member = findMemberOrThrow(memberId);

    if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
    }
    if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.CURRENT_PASSWORD_MISMATCH);
    }
    if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
      throw new BusinessException(ErrorCode.PASSWORD_REUSE_NOT_ALLOWED);
    }

    member.changePassword(passwordEncoder.encode(request.getNewPassword()));
  }

  /** 로그인한 회원의 결제 PIN을 설정하거나 변경한다. */
  @Transactional
  public void updatePaymentPin(Long memberId, MemberPaymentPinUpdateRequest request) {
    Member member = findMemberForUpdateOrThrow(memberId);

    if (!request.getNewPin().equals(request.getConfirmNewPin())) {
      throw new BusinessException(ErrorCode.PIN_CONFIRMATION_MISMATCH);
    }

    if (member.getPinHash() != null && !member.getPinHash().isBlank()) {
      String currentPin = request.getCurrentPin() == null ? "" : request.getCurrentPin();
      if (currentPin.isBlank() || !passwordEncoder.matches(currentPin, member.getPinHash())) {
        throw new BusinessException(ErrorCode.CURRENT_PIN_MISMATCH);
      }
    }

    member.changePaymentPin(passwordEncoder.encode(request.getNewPin()));
  }

  /** 회원의 이름과 연락처를 수정한다 (Member Entity 리턴). 이메일/비밀번호/역할은 변경하지 않는다. */
  @Transactional
  public Member update(Long memberId, MemberUpdateRequest request) {
    return findMemberOrThrow(memberId).updateBy(request);
  }

  /** 회원의 역할(CUSTOMER / COURIER)을 변경한다 (Member Entity 리턴). */
  @Transactional
  public Member updateRole(Long memberId, MemberRole role) {
    return findMemberOrThrow(memberId).changeRole(role);
  }

  /** 승인 대기 중인 배송원 목록을 페이징 조회한다. */
  @Transactional(readOnly = true)
  public Page<Member> getPendingCouriers(Pageable pageable) {
    return memberRepository.findAllByRoleAndCourierApprovalStatus(
        MemberRole.COURIER, CourierApprovalStatus.PENDING, pageable);
  }

  /** 특정 배송원의 자격 심사를 최종 승인 처리한다. */
  @Transactional
  public Member approveCourier(Long memberId) {
    Member courier = findMemberOrThrow(memberId);
    if (courier.getRole() != MemberRole.COURIER) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송원 계정만 자격 심사를 진행할 수 있습니다.");
    }
    return courier.approveCourier();
  }

  /** 특정 배송원의 자격 심사를 거절 처리한다. */
  @Transactional
  public Member rejectCourier(Long memberId) {
    Member courier = findMemberOrThrow(memberId);
    if (courier.getRole() != MemberRole.COURIER) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "배송원 계정만 자격 심사를 진행할 수 있습니다.");
    }
    return courier.rejectCourier();
  }

  /** 배송원 본인의 자격 증빙 서류 URL을 등록/재제출한다. */
  @Transactional
  public Member updateProofDocumentUrl(Long memberId, String proofDocumentUrl) {
    Member member = findMemberOrThrow(memberId);
    return member.updateProofDocumentUrl(proofDocumentUrl);
  }

  /** id로 회원을 조회해 삭제한다. 없으면 EntityNotFoundException. */
  @Transactional
  public void delete(Long memberId) {
    Member member = findMemberOrThrow(memberId);
    vehicleService.deleteAllByMemberId(memberId);
    memberRepository.delete(member);
  }

  private Vehicle createDefaultVehicle(Long memberId, MemberCreateRequest request) {
    double distance =
        (request.getMaxDistance() != null && request.getMaxDistance() > 0)
            ? request.getMaxDistance()
            : resolveMaxDistance(request.getVehicleType());
    return Vehicle.createDefault(
        memberId,
        request.getEquipmentName(),
        request.getVehicleType(),
        resolveMaxWeight(request.getPreferredWeight()),
        distance);
  }

  private double resolveMaxWeight(Double preferredWeight) {
    return preferredWeight == null ? 10.0 : preferredWeight;
  }

  private double resolveMaxDistance(VehicleType vehicleType) {
    if (vehicleType == VehicleType.DRONE) {
      return 20.0;
    }
    if (vehicleType == VehicleType.MOTORCYCLE) {
      return 80.0;
    }
    return 300.0;
  }

  private Member findMemberOrThrow(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
  }

  @Transactional
  public Member getByIdForUpdate(Long memberId) {
    return findMemberForUpdateOrThrow(memberId);
  }

  private Member findMemberForUpdateOrThrow(Long memberId) {
    return memberRepository
        .findByIdForUpdate(memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
  }
}
