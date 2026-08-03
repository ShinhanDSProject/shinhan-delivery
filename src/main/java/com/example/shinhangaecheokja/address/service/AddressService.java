package com.example.shinhangaecheokja.address.service;

import com.example.shinhangaecheokja.address.dto.request.AddressCreateRequest;
import com.example.shinhangaecheokja.address.dto.request.AddressUpdateRequest;
import com.example.shinhangaecheokja.address.entity.Address;
import com.example.shinhangaecheokja.address.repository.AddressRepository;
import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 자주 쓰는 주소 CRUD 비즈니스 로직을 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class AddressService {

  private final AddressRepository addressRepository;

  /** 특정 회원의 자주 쓰는 주소 목록을 조회한다 (Entity 리턴). */
  @Transactional(readOnly = true)
  public List<Address> list(Long memberId) {
    return addressRepository.findByMemberId(memberId);
  }

  /** 신규 자주 쓰는 주소를 생성한다 (Entity 리턴). */
  @Transactional
  public Address create(Long memberId, AddressCreateRequest request) {
    return addressRepository.save(Address.from(memberId, request));
  }

  /** 회원 본인 소유의 주소 정보를 수정한다 (Entity 리턴). */
  @Transactional
  public Address update(Long memberId, Long id, AddressUpdateRequest request) {
    return findAddressOrThrow(memberId, id).updateBy(request);
  }

  /** 회원 본인 소유의 주소를 삭제한다. */
  @Transactional
  public void delete(Long memberId, Long id) {
    Address address = findAddressOrThrow(memberId, id);
    addressRepository.delete(address);
  }

  private Address findAddressOrThrow(Long memberId, Long id) {
    return addressRepository
        .findByIdAndMemberId(id, memberId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND));
  }
}
