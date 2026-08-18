# Implementation Plan - Issue #311: [Bug] 신규 회원이 지갑 생성 화면을 거치지 않고 배송 신청부터 하면 첫 포인트 충전이 실패함

이 문서는 GitHub Issue `#311`의 재현 절차와 저장소 규칙을 기준으로 작성한 구현 계획서다. 목표는 신규 회원이 `/point-wallet` 또는 `/point-charge`를 방문하지 않았더라도 배송 신청 흐름에서 첫 포인트 충전을 정상 수행할 수 있게 `PointWallet` 생성 시점을 서버 중심으로 안정화하는 것이다.

---

## User Review Required

> [!IMPORTANT]
> 아래 3가지는 구현 전에 승인 기준으로 고정한다.
> 1. **근본 해결 우선:** 회원가입 완료 시점에 `PointWallet`을 생성하는 서버 측 보장을 우선 적용한다.
> 2. **방어 로직 병행 여부:** 가입 이전 데이터나 예외 흐름까지 고려해 `PaymentService.chargePoint(memberId, ...)`가 지갑 부재 시 자동 생성까지 허용할지 함께 결정한다.
> 3. **클라이언트 임시 생성 로직 축소:** `point-wallet.html`/`point-charge.html`의 “없으면 생성” 패턴은 서버 보장이 들어가면 점진적으로 제거 또는 단순 조회로 축소하는 방향을 검토한다.

---

## Problem Summary

- 현재 `PointWallet` 생성은 회원가입 시점이 아니라 특정 클라이언트 화면 진입 시점에만 수행된다.
- 배송 신청 흐름의 `address-input.html` 및 `payment-confirmation.html`은 이 생성 경로를 거치지 않으므로, 신규 회원이 바로 충전을 시도하면 `POST /api/v1/points/charge` 내부에서 `POINT_WALLET_NOT_FOUND`로 실패한다.
- 즉, 지갑 존재 여부가 UI 진입 순서에 종속되어 있고, 서버가 “회원은 언제나 지갑을 가진다”는 도메인 불변식을 보장하지 못하고 있다.

---

## Proposed Changes

### 1. Member Registration Flow

- 회원가입 완료 유스케이스를 담당하는 `member` 도메인 서비스와 컨트롤러 흐름을 확인한다.
- 회원 생성이 커밋되는 시점에 `PaymentService.create(...)` 또는 동등한 지갑 생성 유스케이스를 호출해 `PointWallet`을 함께 만든다.
- 이미 지갑이 있는 회원에 대해 중복 생성이 일어나지 않도록 멱등성을 검토한다.

### 2. Payment Service Safeguard

- [src/main/java/com/example/shinhandelivery/payment/service/PaymentService.java](D:/JAVA/DS/shinhan-gaecheokja/src/main/java/com/example/shinhandelivery/payment/service/PaymentService.java)
  - `chargePoint(memberId, ...)`와 필요 시 `usePoint(memberId, ...)`에서 지갑 부재 시 자동 생성 또는 복구가 가능한지 검토한다.
  - 회원가입 이전 데이터, 테스트 fixture, 운영 중 기존 계정 같은 예외 상황을 생각하면 방어 로직을 두는 편이 안전하다.
  - 단, 지갑 생성 책임이 `PaymentService` 내부에 과하게 섞이지 않도록 유스케이스 경계를 명확히 유지한다.

### 3. Repository / Domain Constraints

- `PaymentRepository`와 `PointWallet` 생성 규칙을 확인해 회원당 지갑 1개 제약이 DB 및 코드에서 안전한지 검토한다.
- 중복 생성 경쟁이 가능한 구조면 고유 제약 또는 조회-생성 흐름의 동시성 안전성을 확인한다.

### 4. Frontend Flow Audit

- [src/main/resources/templates/address-input.html](D:/JAVA/DS/shinhan-gaecheokja/src/main/resources/templates/address-input.html)
- [src/main/resources/templates/payment-confirmation.html](D:/JAVA/DS/shinhan-gaecheokja/src/main/resources/templates/payment-confirmation.html)
- [src/main/resources/templates/point-wallet.html](D:/JAVA/DS/shinhan-gaecheokja/src/main/resources/templates/point-wallet.html)
- [src/main/resources/templates/point-charge.html](D:/JAVA/DS/shinhan-gaecheokja/src/main/resources/templates/point-charge.html)

프론트는 서버 보장 이후에도 아래를 점검한다.
- 잔액 조회 시 지갑 부재를 0원 표시에만 의존하지 않는지 확인
- 배송 신청 중 충전 모달이 서버 자동 생성과 충돌 없이 동작하는지 확인
- 중복된 “지갑 없으면 생성” 로직이 남아 있다면 정리 후보로 표시

### 5. Optional Document Sync

- 포인트 지갑 또는 회원가입 설계 문서가 현재 구현과 달라졌다면 SSOT 문서를 업데이트한다.
- 서버가 회원가입 시점 지갑 생성을 보장한다는 도메인 규칙을 문서에 명시한다.

---

## Architecture / Risk Review

### 1. 아키텍처 순수성

- `member` 도메인에서 `payment` 서비스를 호출하는 방향은 `Controller -> Service -> Repository` 규칙 안에서 허용 가능하지만, 직접 Repository 참조는 금지한다.
- Controller에서 지갑 생성 분기를 넣지 않고 Service 유스케이스로만 처리한다.

### 2. 비즈니스 예외 안전성

- 기존 `POINT_WALLET_NOT_FOUND` 예외가 어떤 경로에서 여전히 유효한지 구분해야 한다.
- “신규 회원 첫 충전”에서는 더 이상 404가 발생하지 않아야 하지만, 비정상 회원/삭제된 데이터 등 진짜 이상 상태는 예외로 남길 수 있다.

### 3. DB / 마이그레이션 영향

- 스키마 변경 없이 해결 가능할 가능성이 높다.
- 다만 회원당 지갑 1개 보장이 DB 차원에서 약하면 제약 조건 점검이 필요하다.

### 4. 보안 / 개인정보

- 회원가입 직후 지갑 생성은 인증/인가보다는 도메인 초기화 책임이다.
- 이 과정에서 개인정보나 결제 정보가 로그에 노출되지 않게 유지한다.

### 5. DX / 크로스 플랫폼

- 지갑 생성이 UI 진입 순서에 의존하면 재현이 불안정하다.
- 서버 보장으로 바꾸면 브라우저별/플로우별 차이를 줄여 초급자도 이해하기 쉬운 구조가 된다.

### 6. 회귀 테스트 가치

- 신규 회원이 지갑 화면을 거치지 않고 배송 신청 중 바로 충전하는 시나리오가 핵심 회귀 포인트다.
- 회원가입 후 즉시 충전, 기존 회원 충전, 기존 지갑 보유 회원 재가입 불가/중복 생성 방지 등을 같이 검증해야 한다.

---

## Test Plan

### Backend Tests

- `MemberService` 또는 회원가입 서비스 테스트
  - 회원가입 성공 시 `PointWallet`이 함께 생성되는지 검증
- `PaymentServiceTest`
  - 지갑 없는 회원의 첫 충전이 성공하는지 검증
  - 기존 지갑이 있는 회원은 중복 생성 없이 기존 지갑을 사용하는지 검증

### Frontend / Flow Verification

- 배송 신청 중 충전 모달 경로에서 첫 충전이 실패하지 않는지 확인
- 지갑 화면 미방문 신규 회원 기준 잔액/충전 동작이 일관된지 확인

### Full Verification

```bash
./scripts/verify.sh
```

---

## Execution Order

1. 회원가입 서비스와 지갑 생성 책임 위치 파악
2. 서버 측 기본 생성 로직 구현
3. `PaymentService` 방어 로직 보강 여부 구현
4. 관련 테스트 보강
5. 배송 신청/충전 화면 회귀 확인
6. `./scripts/verify.sh` 실행 및 실패 시 자가 치유

---

## Approval Checkpoint

이 계획은 `AGENTS.md`의 `/plan <이슈번호>` 규칙에 따른 승인 대기 상태다. 승인되면 위 순서대로 구현에 들어가고, 구현 후 `./scripts/verify.sh` 0 exit code까지 마무리한다.
