package com.example.shinhandelivery.member.repository;

import com.example.shinhandelivery.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Member 엔티티에 대한 JPA 저장소. */
public interface MemberRepository extends JpaRepository<Member, Long> {

  /** 이메일로 회원을 조회한다. */
  Optional<Member> findByEmail(String email);

  /** 이메일 중복 가입 여부를 확인한다. */
  boolean existsByEmail(String email);
}
