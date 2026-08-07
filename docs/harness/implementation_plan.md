# 📋 Implementation Plan - Issue #232 (Phase 1: 출근 및 배송 매칭)

이 이슈와 PRD Phase 1 요구사항 명세서(`v1.0`), 그리고 현재 프로젝트 소스 코드 기반의 **출근, 대기열 조회 및 동시성 제어 배송 매칭 구현 계획**입니다.

---

## 🎯 User Review Required

> [!IMPORTANT]
> **동시성 제어 방식 및 DB 컬럼 추가 결정**
> 1. **낙관적 락 (`@Version`) 도입**: 동시 수락 시 0.1초 차이의 경합 상황에서 단 1명에게만 배정을 허용하고 정합성을 사수하기 위해 `delivery_request` 테이블에 `version` (BIGINT DEFAULT 0) 컬럼을 추가합니다. (`V26__add_version_to_delivery_request.sql`)
> 2. **예외 및 응답 코드**: 이미 타 배송원에게 배정된 콜 수락 시 `409 Conflict`와 메시지 `"이미 다른 배송원에게 배차된 주문입니다."`를 반환합니다.

---

## 🛠️ Proposed Changes

### 1. Database Migration
- `src/main/resources/db/migration/V26__add_version_to_delivery_request.sql`:
  - `delivery_request` 테이블에 `version BIGINT NOT NULL DEFAULT 0` 컬럼 추가

### 2. Domain & Entity
- **`DeliveryRequest.java`**:
  - `@Version private Long version;` 필드 추가
  - `assignCourier()` 또는 `match()` 도메인 비즈니스 메서드 추가: `REQUESTED` 상태가 아닐 경우 `IllegalStateException` / `DeliveryAlreadyMatchedException` 예외 던짐

### 3. Repository
- **`DeliveryRequestRepository.java`**:
  - GPS 위경도 기반 반경 3km 이내 `status = 'REQUESTED'` 주문 조회 JPQL / Native Query 구현 (Haversine 공식 거리 산출)
- **`VehicleRepository.java`**:
  - 소유자(`ownerId = memberId`)의 차량 조회 및 상태(`status`) 업데이트 지원

### 4. Service Layer
- **`CourierService.java`** (또는 `CourierStatusService.java`):
  - `REQ-001`: `updateWorkStatus(Long memberId, WorkStatus status)` - `ONLINE` 시 `VehicleStatus.AVAILABLE`, `OFFLINE` 시 `VehicleStatus.BUSY`로 변경
- **`DeliveryMatchingService.java`**:
  - `REQ-003`: `getAvailableDeliveries(Long memberId, double lat, double lon, double radiusKm)` - 반경 3km 내 대기 주문 목록 반환
  - `REQ-004`: `catchDelivery(Long memberId, Long deliveryRequestId)` - `@Transactional` 기반 주문 수락. `OptimisticLockingFailureException` 감지 시 `DeliveryAlreadyMatchedException` (`409 Conflict`) 던짐

### 5. Controller & Exception Handling
- **`CourierController.java`** (또는 `DeliveryController.java`):
  - `PATCH /api/v1/couriers/status` : 영업 상태 토글
  - `GET /api/v1/deliveries/available` : 대기열 목록 반환 (단가, 거리 포함)
  - `POST /api/v1/deliveries/{id}/accept` (또는 `/catch`) : 주문 수락
- **`GlobalExceptionHandler.java`**:
  - `DeliveryAlreadyMatchedException` 발생 시 `HttpStatus.CONFLICT (409)` 반환

### 6. Tests & Verification
- **단위 테스트**: `DeliveryMatchingServiceTest`, `CourierServiceTest`
- **동시성 검증 테스트**: `DeliveryMatchingConcurrencyTest` (`ExecutorService` + `CountDownLatch`를 활용해 10명 이상의 배송원 동시 수락 시 단 1명만 성공하고 9명은 409 Conflict 예외가 발생하는지 실증 검증)

---

## 🧪 Verification Plan

### Automated Tests
```bash
.\gradlew.bat spotlessCheck test
```
- ArchUnit 아키텍처 규칙 검증
- Spotless 포맷팅 검증
- 100% 그린 빌드 패스 및 테스트 성공 확보
