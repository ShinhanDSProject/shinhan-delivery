package com.example.shinhandelivery.member.repository;

import com.example.shinhandelivery.member.entity.Member;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Member 엔티티에 대한 JPA 저장소. */
public interface MemberRepository extends JpaRepository<Member, Long> {

  /** 이메일로 회원을 조회한다. */
  Optional<Member> findByEmail(String email);

  /** 이메일 중복 가입 여부를 확인한다. */
  boolean existsByEmail(String email);

  /** 회원 행을 비관적 락으로 조회한다. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select m from Member m where m.id = :id")
  Optional<Member> findByIdForUpdate(@Param("id") Long id);
}
