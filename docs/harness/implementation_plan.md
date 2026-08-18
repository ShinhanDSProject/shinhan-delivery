# Implementation Plan - Issue #309: [Bug] 포인트 지갑의 최근 내역이 실제 이력이 아니라 1회성 표시이며 화면 이탈 시 사라짐

이 문서는 GitHub Issue `#309`의 버그 원인과 저장소 규칙을 기준으로 작성한 구현 계획서다. 목표는 `point-wallet.html`의 1회성 `sessionStorage` 스냅샷 표시를 제거하고, 서버에 저장된 실제 `PointHistory`를 회원 기준으로 안정적으로 조회하도록 API와 UI를 정렬하는 것이다.

---

## User Review Required

> [!IMPORTANT]
> 아래 3가지는 구현 전에 승인 기준으로 고정한다.
> 1. **API 위치 유지:** 포인트 지갑 기능은 기존 규격과 일관되게 `/api/v1/point-wallets` 하위에 확장한다. 신규 목록 조회는 `GET /api/v1/point-wallets/{walletId}/histories` 또는 인증 사용자 단일 지갑 전제를 활용한 동등한 REST 경로로 설계한다.
> 2. **이력 소스 단일화:** `point-wallet.html`의 최근 내역은 `sessionStorage.pointChargeSnapshot`을 더 이상 읽지 않고, 서버 `PointHistory` 조회 결과만 렌더링한다.
> 3. **페이지 진입 안정성:** 충전 직후, 다른 화면 이동 후 재진입, 새로고침 후 재진입 모두 같은 조회 결과를 보여야 한다.

---

## Problem Summary

- 현재 `src/main/resources/templates/point-wallet.html`은 `/api/v1/point-wallets`로 잔액만 읽고, 최근 내역은 `sessionStorage.getItem("pointChargeSnapshot")`에 의존한다.
- 해당 스냅샷은 충전 페이지에서 1회 저장된 뒤 지갑 화면에서 즉시 삭제되므로, 화면을 벗어났다가 다시 오면 최근 내역이 사라진다.
- 백엔드에는 `PointHistory` 엔티티와 기록 적재 로직은 있지만, 인증 사용자 기준 이력 목록 조회 API가 없다.

---

## Proposed Changes

### 1. Repository Layer

- [src/main/java/com/example/shinhandelivery/payment/repository/PointHistoryRepository.java](D:/JAVA/DS/shinhan-gaecheokja/src/main/java/com/example/shinhandelivery/payment/repository/PointHistoryRepository.java)
  - 회원 기준 `PointHistory` 목록 조회 메서드를 추가한다.
  - 최신순 정렬과 페이징을 지원하도록 `Pageable` 기반 시그니처를 우선 검토한다.
  - 다른 도메인에서 직접 조회하지 않고 `PaymentService`를 통해서만 사용한다.

### 2. Service Layer

- [src/main/java/com/example/shinhandelivery/payment/service/PaymentService.java](D:/JAVA/DS/shinhan-gaecheokja/src/main/java/com/example/shinhandelivery/payment/service/PaymentService.java)
  - 인증 회원 ID와 지갑 소유권을 검증한 뒤 최근 포인트 이력을 조회하는 유스케이스를 추가한다.
  - 반환 타입은 Controller가 Entity를 직접 노출하지 않도록 응답 DTO 전용 구조로 변환한다.
  - 조회용 메서드는 읽기 전용 트랜잭션으로 두고, 기존 충전/사용/환불 로직의 API 계약은 깨지지 않게 유지한다.

### 3. DTO / Controller Layer

- `payment.dto.response` 아래에 최근 이력 응답 DTO를 추가한다.
  - 예: `PointHistoryItemResponse`, `PointHistoryListResponse`
  - 필드 후보: `historyId`, `type`, `amount`, `balanceAfter`, `referenceId`, `createdAt`
- [src/main/java/com/example/shinhandelivery/payment/controller/PointController.java](D:/JAVA/DS/shinhan-gaecheokja/src/main/java/com/example/shinhandelivery/payment/controller/PointController.java)
  - 인증 사용자 기준 최근 이력 조회 API를 추가한다.
  - API는 200 OK + 목록 응답을 반환하며, 지갑 미존재/권한 오류는 기존 전역 예외 체계를 따른다.

### 4. Frontend Template

- [src/main/resources/templates/point-wallet.html](D:/JAVA/DS/shinhan-gaecheokja/src/main/resources/templates/point-wallet.html)
  - 최근 내역 렌더링 로직을 서버 API 호출 기반으로 교체한다.
  - `pointChargeSnapshot` 읽기/삭제 코드를 제거한다.
  - 빈 목록일 때의 UX는 "아직 포인트 이용 내역이 없습니다" 같은 명시적 상태로 정리하고, 잔액 표시와 혼동되지 않게 분리한다.
  - 기존 디자인 시스템 토큰과 공통 컴포넌트 사용 규칙을 유지한다.

### 5. Optional Document Sync

- [docs/design/포인트-지갑-기능-설계서.md](D:/JAVA/DS/shinhan-gaecheokja/docs/design/포인트-지갑-기능-설계서.md)
  - 이미 문서에 존재하는 충전/사용 이력 개념과 실제 구현이 어긋난다면, 신규 조회 API와 화면 흐름을 SSOT 기준으로 보강한다.

---

## Architecture / Risk Review

### 1. 아키텍처 순수성

- Controller는 Entity를 반환하지 않는다.
- `PointController -> PaymentService -> PointHistoryRepository` 단방향 의존만 사용한다.

### 2. 비즈니스 예외 안전성

- 지갑 미존재, 타 회원 지갑 접근, 인증 누락 시 기존 `GlobalExceptionHandler` 매핑을 재사용한다.
- 신규 전용 예외 클래스 추가는 꼭 필요할 때만 검토하고, 가능하면 기존 예외 체계 안에서 해결한다.

### 3. DB / 마이그레이션 영향

- 이번 작업은 `PointHistory` 조회 API 추가가 핵심이므로 스키마 변경 없이 끝낼 가능성이 높다.
- 실제로 필요한 컬럼이 이미 존재하는지 먼저 검증하고, 불필요한 Flyway 마이그레이션은 만들지 않는다.

### 4. 보안 / 개인정보

- 응답 DTO에 민감정보나 멱등키를 노출하지 않는다.
- 인증 회원 본인 이력만 조회되도록 소유권 검증을 강제한다.

### 5. DX / 크로스 플랫폼

- 프론트 로직은 브라우저 내 `sessionStorage` 의존을 제거해 재현성과 테스트 가능성을 높인다.
- 검증 명령은 기존 Windows/Unix 공용 하네스인 `./scripts/verify.sh` 기준으로 유지한다.

### 6. 회귀 테스트 가치

- "충전 직후 보임"만 확인하는 테스트는 부족하다.
- "재진입 후에도 서버 이력이 계속 조회됨"을 보장하는 Controller/Service 테스트가 필요하다.

---

## Test Plan

### Backend Tests

- `PaymentServiceTest`
  - 회원 기준 최근 이력 조회가 최신순으로 DTO 변환되는지 검증
  - 지갑 미존재 또는 회원 불일치 시 예외가 발생하는지 검증
- `PointControllerTest`
  - 최근 이력 조회 API의 200 OK, JSON 필드, 인증 컨텍스트 처리 검증

### Frontend / View Verification

- `point-wallet.html`의 최근 내역 섹션이 더 이상 `sessionStorage` 문자열에 의존하지 않는지 정적 확인
- 필요 시 뷰 테스트 또는 문자열 기반 회귀 테스트로 API 호출 경로와 빈 상태 문구를 검증

### Full Verification

```bash
./scripts/verify.sh
```

- Spotless
- ArchUnit
- 전체 테스트
- 커버리지 게이트

---

## Execution Order

1. `PointHistoryRepository` 조회 메서드 추가
2. `PaymentService` 최근 이력 조회 유스케이스 추가
3. 응답 DTO 및 `PointController` API 추가
4. `point-wallet.html`을 신규 API 기반으로 전환
5. 관련 테스트 보강
6. `./scripts/verify.sh` 실행 및 실패 시 자가 치유

---

## Approval Checkpoint

이 계획은 `AGENTS.md`의 `/plan <이슈번호>` 규칙에 따른 승인 대기 상태다. 승인되면 위 순서대로 구현에 들어가고, 구현 후 `./scripts/verify.sh` 0 exit code까지 마무리한다.
