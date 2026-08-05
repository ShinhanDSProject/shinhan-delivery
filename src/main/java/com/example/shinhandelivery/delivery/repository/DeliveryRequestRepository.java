package com.example.shinhandelivery.delivery.repository;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** DeliveryRequest 엔티티에 대한 JPA 저장소. */
public interface DeliveryRequestRepository extends JpaRepository<DeliveryRequest, Long> {

  /** 여러 차량이 동시에 같은 배송 요청을 수락(매칭)하려 할 때를 대비해 비관적 쓰기 락으로 조회한다. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from DeliveryRequest d where d.id = :id")
  Optional<DeliveryRequest> findByIdForUpdate(@Param("id") Long id);

  /** 주어진 상태이면서 무게·거리를 모두 그 값 이하로 감당 가능한 배송 요청 목록을 조회한다(차량의 콜 목록용). */
  List<DeliveryRequest> findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
      DeliveryStatus status, double weight, double distance);

  /**
   * 로그인 회원 본인의 배송 요청을 최신순으로 페이징 조회한다(배송 내역 목록용). created_at이 동률(백필된 레거시 행 등)이어도 페이징이 흔들리지 않도록 id를
   * 2차 정렬 기준으로 사용한다.
   */
  Page<DeliveryRequest> findByCustomerIdOrderByCreatedAtDescIdDesc(
      Long customerId, Pageable pageable);

  /** 로그인 회원 본인의 특정 상태 배송 요청만 최신순(id 2차 정렬 포함)으로 페이징 조회한다. */
  Page<DeliveryRequest> findByCustomerIdAndStatusOrderByCreatedAtDescIdDesc(
      Long customerId, DeliveryStatus status, Pageable pageable);

  Optional<DeliveryRequest> findByCustomerIdAndPaymentIdempotencyKey(
      Long customerId, String paymentIdempotencyKey);
}
