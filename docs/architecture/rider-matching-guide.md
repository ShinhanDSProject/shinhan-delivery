# 📖 배송원 출근 및 동시성 제어 주문 매칭 가이드 (Phase 1)

> **문서 버전:** v1.0  
> **작성일:** 2026-08-05  
> **관련 이슈:** [#232 (출근 및 배송 매칭)](https://github.com/ShinhanDSProject/shinhan-delivery/issues/232)  
> **대상:** 신입 백엔드/프론트엔드 개발자 및 리뷰어

---

## 1. 개요 (Overview)

본 문서는 **Phase 1: 배송원 코어 라이프사이클(출근/퇴근 영업 상태 관리, 주변 3km 대기열 조회, 주문 동시 수락 및 동시성 제어)**의 기술 구조와 설계 원리를 쉽게 설명하기 위해 작성되었습니다.

---

## 2. 핵심 기능별 메커니즘

### 📍 1) [REQ-001] 영업 상태 토글 (ONLINE / OFFLINE)
* **API**: `PATCH /api/v1/couriers/status`
* **동작 원리**:
  * 배송원(`MemberRole.COURIER`)이 출근 버튼(스위치)을 누르면 `WorkStatus.ONLINE` 요청이 전송됩니다.
  * 서버는 배송원 소유의 `Vehicle` 상태를 `AVAILABLE`로 변경하고, 요청에 포함된 GPS 위도/경도 좌표를 갱신합니다.
  * 오프라인 전환 시 `VehicleStatus.BUSY`로 변경하여 콜 수락 가능 대상에서 제외합니다.

---

### 📍 2) [REQ-003] 주변 3km 대기열 목록 조회
* **API**: `GET /api/v1/delivery-requests/available?latitude=...&longitude=...&radiusKm=3.0`
* **동작 원리**:
  * DB 내의 `status = DeliveryStatus.REQUESTED` 상태인 모든 신규 배송 요청을 조회합니다.
  * `DeliveryFeeCalculator`의 하버사인(Haversine) 대권 거리 공식을 적용해 배송원의 현재 위치와 주문 픽업지 간 거리(`distanceToPickupKm`)를 계산합니다.
  * 지정된 반경(기본 3.0km) 이내의 콜만 필터링한 후, **거리 오름차순(가까운 콜 우선)**으로 정렬하여 반환합니다.

---

### 📍 3) [REQ-004 & 핵심 NFR] 주문 수락(Catch) 및 동시성 제어
* **API**: `POST /api/v1/delivery-requests/{deliveryRequestId}/catch`
* **문제 상황**:
  * 인기 있는 배송 주문이 발생했을 때, 여러 명의 배송원이 0.1초 차이로 동시 수락 버튼을 누를 수 있습니다.
  * 동시성 처리가 되어있지 않으면 동일한 주문에 중복 매칭이 일어나 데이터 꼬임 현상이 발생합니다.

* **해결 기술: JPA 낙관적 락 (`@Version`)**:
  1. `delivery_request` 테이블에 `version` (BIGINT) 컬럼을 추가하고, Entity에 `@Version private Long version;`을 매핑했습니다.
  2. 첫 번째 배송원이 수락(Catch)을 요청하면 `status = MATCHED` 변경 및 `version = version + 1`로 DB 커밋이 성공합니다 (`HTTP 200/201 OK`).
  3. 거의 동시에 들어온 2번째 이후의 수락 요청은 DB 충돌(`OptimisticLockingFailureException`)이 발생합니다.
  4. 서버는 이를 감지하여 **`AlreadyMatchedException` (`HTTP 409 Conflict`)**으로 변환하여 반환합니다.
  5. 프론트엔드는 `409 Conflict` 수신 시 `"이미 다른 배송원에게 배차된 주문입니다."` 안내 팝업을 띄우고 대기열을 리로드합니다.

---

## 3. 검증 명령어 및 마이크로 테스트

```bash
# 코드 포맷팅 검증 및 자동 교정
.\gradlew.bat spotlessApply

# 전체 테스트 수행 (동시성 경합 테스트 포함)
.\gradlew.bat test
```
