# 기능 PRD 템플릿

## 문서 정보
| 항목 | 내용 |
|---|---|
| 기능명 | 배송 요청-차량 매칭 (Matching) |
| 작성자 | @kms7522 (kms) |
| 작성일 | 2026-08-05 |
| 상태 | 확정 |
| 관련 링크 | `MatchingController`, `MatchingService`, `Matching` Entity, `DeliveryService.publishOfferIfCandidatesExist`, `DeliveryOfferBroadcastEvent`, `DeliveryOfferBroadcastListener`, `StompAuthChannelInterceptor`, `TrackingService.assertCanSubscribeToOffers`, `docs/design/matching-design.md` |

## 1. 배경 및 문제 (Background & Problem)
- 배송 요청(`DeliveryRequest`)이 `REQUESTED` 상태로 만들어져도, 그 요청을 실제로 수행할 차량(`Vehicle`)과 연결해주지 않으면 배송이 진행되지 않는다.
- 지금은(이 기능이 없다면) 배송 요청과 차량을 이어주는 절차가 없어서, 요청이 아무리 쌓여도 "누가 이 배송을 맡을지"가 시스템 안에서 결정되지 않는다.
- **매칭 자체는 "차량이 열린 콜을 조회해서 수락(POST)"하는 방식이지만, "누구에게 오퍼를 알릴지"는 풀(pull)이 아니라 푸시(push) 방식이다.** 배송 요청이 생성되는 순간 서버가 조건(가용 상태 + 용량)을 만족하는 후보 차량을 계산해서, 각 차량 소유주 전용 WebSocket 채널로 실시간 오퍼를 즉시 밀어준다 (`DeliveryService.publishOfferIfCandidatesExist` → `DeliveryOfferBroadcastEvent` → `DeliveryOfferBroadcastListener`). 차량이 매번 목록을 새로고침하지 않아도 새 콜이 뜨는 즉시 알림을 받을 수 있게 하기 위함이다.

## 2. 목표 (Goal)
- 열려 있는(REQUESTED) 배송 요청과, 그걸 감당할 수 있는(무게·거리 조건을 만족하는) 가용 차량을 매칭시켜 배송을 진행 상태로 전환한다.
- **성공 지표**: 아래 표는 실제 운영 지표 수집 체계가 아직 없어 목표값만 잠정 기재한 것이며, 현재 코드에는 이 지표를 실측하는 로직이 없다.

| 지표 | 현재 | 목표 |
|---|---|---|
| 배송 요청 생성 후 매칭 완료까지 걸리는 시간 | 미측정 | (추후 정의) |
| 매칭 실패율(가용 차량 없음) | 미측정 | (추후 정의) |

## 3. Non-goal (하지 않을 것)
- **자동 배정(시스템이 최종적으로 차량을 확정하는 것)은 이번 범위가 아니다.** 조건에 맞는 후보 전원에게 오퍼를 동시에 푸시하긴 하지만, 실제로 그중 누구에게 배정할지는 여전히 "차량이 먼저 수락(POST)하는" 사람이 가져가는 방식이다 — 최적 후보를 골라 자동으로 확정 배정하는 알고리즘은 없다.
- 매칭 이력(과거에 어떤 차량이 어떤 배송을 몇 번 맡았는지 등 통계)은 다루지 않는다 — `Matching`은 배송 요청 1건당 최대 1개만 존재하는 1:0..1 구조라, 여러 건의 이력을 누적해서 보여주는 기능이 아니다.
- 매칭 후 배송원-고객 간 실시간 통신(채팅 등)은 다루지 않는다.

## 4. 사용자 스토리 (User Stories)
> "~로서, 나는 ~하고 싶다, 그래야 ~할 수 있기 때문이다"

| 우선순위 | 스토리 |
|---|---|
| P0 | 배송원으로서, 나는 새 배송 콜이 뜨면 목록을 새로고침하지 않아도 실시간으로 오퍼 알림을 받고 싶다, 그래야 콜을 놓치지 않고 빠르게 반응할 수 있기 때문이다. |
| P0 | 배송원으로서, 나는 내 차량이 지금 수락할 수 있는 열린 배송 콜 목록을 보고 싶다, 그래야 내가 감당할 수 있는 배송을 골라 수락할 수 있기 때문이다. |
| P0 | 배송원으로서, 나는 콜을 수락하면 그 배송 요청이 나에게 배정되기를 원한다, 그래야 다른 차량이 같은 요청을 중복으로 수락하지 않기 때문이다. |
| P0 | 배송원으로서, 나는 다른 사람 차량의 오퍼 채널을 몰래 엿볼 수 없기를 원한다, 그래야 내 배송 콜 정보가 다른 배송원에게 새지 않기 때문이다. |
| P1 | 배송원으로서, 나는 진행 중인 매칭을 취소하거나 완료 처리하고 싶다, 그래야 배송 상태를 실제 진행 상황과 맞출 수 있기 때문이다. |

## 5. 요구사항 (Requirements)
| ID | 요구사항 | 우선순위 | 비고 |
|---|---|---|---|
| R-1 | 매칭 생성 시 배송 요청이 `REQUESTED` 상태인지 확인한다 | P0 | 아니면 `AlreadyMatchedException` (409) |
| R-2 | 매칭 생성 시 차량이 `AVAILABLE` 상태인지 확인한다 | P0 | 아니면 `VehicleNotAvailableException` (409) |
| R-3 | 차량의 `maxWeight`/`maxDistance`가 배송 요청의 `weight`/`distance` 이상인지 확인한다 | P0 | 아니면 `VehicleCapacityMismatchException` (400) |
| R-4 | 매칭 생성 성공 시 배송 요청 상태를 `MATCHED`로, 차량 상태를 `BUSY`로 동기화한다 | P0 | `MatchingService.applyStatus()` |
| R-5 | 매칭 상태 변경(`PUT`)은 정해진 전이만 허용한다 | P0 | 아래 "5.1 상태 전이 규칙" 참고 |
| R-6 | 매칭 삭제 시, 진행 중(`MATCHED`)이었던 매칭만 배송 요청을 `REQUESTED`로, 차량을 `AVAILABLE`로 되돌린다 | P1 | 이미 `COMPLETED`/`CANCELLED`된 매칭은 되돌리지 않음 |
| R-7 | 배송 요청 생성 시, `AVAILABLE` 상태이면서 무게·거리 조건을 만족하는 후보 차량 전원에게 오퍼를 실시간 푸시한다 | P0 | `VehicleService.findOfferCandidates` → `/topic/vehicles/{vehicleId}/offers` |
| R-8 | 오퍼 채널(`/topic/vehicles/{vehicleId}/offers`) 구독은 그 차량의 소유주만 허용한다 | P0 | `StompAuthChannelInterceptor` + `TrackingService.assertCanSubscribeToOffers`, 위반 시 `UnauthorizedOfferAccessException` |
| R-9 | 오퍼 발행은 배송 요청 생성 트랜잭션이 커밋된 이후에만 나간다 | P1 | `@TransactionalEventListener(phase = AFTER_COMMIT)` — 생성이 롤백되면 오퍼도 나가지 않음 |

### 5.1 상태 전이 규칙 (`MatchingStatus`)
- `MATCHED` → `COMPLETED` / `CANCELLED` / `MATCHED`(자기 자신) 전부 허용
- `CANCELLED` → `CANCELLED`(자기 자신) 또는 `MATCHED`(재매칭)만 허용
- `COMPLETED` → `COMPLETED`(자기 자신)만 허용 — 종료 상태라 다른 상태로 못 감
- 허용 안 되는 전이를 요청하면 `InvalidMatchingTransitionException` (409)

- 예외/엣지 케이스:
  - 요청한 상태가 지금 상태와 같으면(예: `CANCELLED` → `CANCELLED`) 아무것도 바뀌지 않은 것으로 보고, 차량 재검증이나 이벤트 발행 없이 그대로 반환한다.
  - `CANCELLED`에서 `MATCHED`로 재매칭할 때는, 생성 시와 동일하게 차량이 `AVAILABLE`인지·무게/거리 조건을 만족하는지 다시 검증한다.
- 비기능 요구사항:
  - **매칭 생성**(`create`)은 배송 요청과 차량 **둘 다** `findByIdForUpdate` 비관적 락으로 조회한다 — 동시에 여러 차량이 같은 배송 요청을 수락하는 경쟁 상태(race condition)를 막기 위함.
  - **매칭 상태 변경**(`update`, 재매칭 포함)은 **차량만** `getVehicleForUpdate`로 락을 걸고, 배송 요청은 락 없이 조회한다(`findDeliveryRequestOrThrow`) — 생성 시와 잠금 범위가 다르다는 점에 주의.

## 6. 오픈 이슈 (Open Questions)
- [ ] `Matching.status`가 `DeliveryRequest.status`와 사실상 중복 정보 아닌가? (1:1 구조라 굳이 두 곳에서 상태를 관리할 필요가 있는지 — 과거에 검토했으나 현재 설계 그대로 유지하기로 결론)
- [ ] 재매칭(`CANCELLED` → `MATCHED`) 시 원래 차량이 아닌 다른 차량으로 배정을 바꾸는 흐름은 어떻게 되는가? (현재 `MatchingUpdateRequest`는 `status`만 받고 `vehicleId` 변경은 지원하지 않음)
