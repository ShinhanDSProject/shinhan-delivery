package com.example.shinhangaecheokja.address.repository;

import com.example.shinhangaecheokja.address.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Address 엔티티 전용 데이터 접근 리포지토리. */
public interface AddressRepository extends JpaRepository<Address, Long> {

  /** 특정 회원의 자주 쓰는 주소 목록을 조회한다. */
  List<Address> findByMemberId(Long memberId);

  /** 주소 ID와 회원 ID가 모두 일치하는 주소 단건을 조회한다. (인가 검증용) */
  Optional<Address> findByIdAndMemberId(Long id, Long memberId);
}
