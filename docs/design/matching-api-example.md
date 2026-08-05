# API 명세서 템플릿

## 문서 정보
| 항목 | 내용 |
|---|---|
| API/기능명 | 배송 요청-차량 매칭 (Matching) |
| 작성자 | @kms7522 (kms) |
| 작성일 | 2026-08-05 |
| 버전 | v1.0 |
| 상태 | 확정 |
| 관련 PRD/ERD 링크 | `docs/design/matching-prd-example.md`, `docs/design/matching-erd-example.md` |

---

## 1. 개요 (Overview)
- 배송 요청과 차량을 연결(매칭)하는 CRUD API. 배송원이 열린 콜을 조회해 수락하거나, 매칭 상태를 변경/삭제한다.
- Base URL: `/api/v1/matchings`
- 인증 방식: `Authorization: Bearer {accessToken}` (JWT) — **다만 아래 §6에서 설명하듯, 이 컨트롤러는 실제로 인증 여부를 검증하지 않는다.**

## 2. 엔드포인트 목록 (Endpoint Summary)
| Method | Endpoint | 설명 | 인증 필요 |
|---|---|---|---|
| POST | /api/v1/matchings | 매칭 생성 (콜 수락) | 명세상 Y, 실제로는 미검증 (§6) |
| GET | /api/v1/matchings/calls | 열린 콜(배송 요청) 목록 조회 | 명세상 Y, 실제로는 미검증 (§6) |
| GET | /api/v1/matchings/{matchingId} | 매칭 단건 조회 | 명세상 Y, 실제로는 미검증 (§6) |
| GET | /api/v1/matchings | 매칭 전체 목록 조회 | 명세상 Y, 실제로는 미검증 (§6) |
| PUT | /api/v1/matchings/{matchingId} | 매칭 상태 변경 | 명세상 Y, 실제로는 미검증 (§6) |
| DELETE | /api/v1/matchings/{matchingId} | 매칭 삭제 | 명세상 Y, 실제로는 미검증 (§6) |

---

## 3. 엔드포인트 상세 (Endpoint Details)

### 3.1 `POST /api/v1/matchings` — 매칭 생성 (콜 수락)

**설명**
- 배송원이 열린 콜(REQUESTED 상태의 배송 요청)을 자기 차량으로 수락한다.

**Request**

Headers
| 이름 | 필수 | 설명 |
|---|---|---|
| Authorization | Y (명세상) | `Bearer {accessToken}` |
| Content-Type | Y | `application/json` |

Body
| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|---|---|---|---|---|
| deliveryRequestId | number | Y | 수락할 배송 요청 ID | `@NotNull` |
| vehicleId | number | Y | 수락하는 차량 ID | `@NotNull` |

```json
{
  "deliveryRequestId": 12,
  "vehicleId": 3
}
```

**Response**

성공 (`201 Created`)
| 필드명 | 타입 | 설명 |
|---|---|---|
| id | number | 생성된 매칭 ID |
| deliveryRequestId | number | 배송 요청 ID |
| vehicleId | number | 차량 ID |
| status | string | `MATCHED` |
| matchedAt | string(ISO8601) | 매칭 성사 시각 |

```json
{
  "id": 45,
  "deliveryRequestId": 12,
  "vehicleId": 3,
  "status": "MATCHED",
  "matchedAt": "2026-08-05T09:00:00"
}
```

**에러 케이스**
| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 404 | D001 | deliveryRequestId에 해당하는 배송 요청 없음 |
| 404 | V001 | vehicleId에 해당하는 차량 없음 |
| 409 | D003 | 배송 요청이 이미 REQUESTED 상태가 아님(이미 매칭됨) |
| 409 | V003 | 차량이 AVAILABLE 상태가 아님 |
| 400 | V002 | 차량의 최대 중량/거리가 배송 요청 조건을 감당 못함 |

```json
{
  "status": 409,
  "code": "D003",
  "message": "이미 처리된 배송 요청입니다.",
  "timestamp": "2026-08-05T09:00:00",
  "traceId": "abc123",
  "errors": []
}
```

---

### 3.2 `GET /api/v1/matchings/calls` — 열린 콜 목록 조회

**설명**
- 특정 차량이 지금 수락할 수 있는 열린 콜(REQUESTED 상태이면서 그 차량의 무게·거리 조건을 만족하는 배송 요청) 목록을 조회한다.

**Request**

Query Parameters
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| vehicleId | number | Y | 콜을 조회할 차량 ID |

**Response**

성공 (`200 OK`) — `DeliveryResponse` 배열
```json
[
  {
    "id": 12,
    "customerId": 7,
    "pickupAddress": "서울시 강남구 테헤란로 123",
    "dropoffAddress": "서울시 서초구 서초대로 456",
    "weight": 10.0,
    "distance": 5.2,
    "status": "REQUESTED",
    "feePoint": 8500,
    "pickupLatitude": 37.5,
    "pickupLongitude": 127.0,
    "dropoffLatitude": 37.6,
    "dropoffLongitude": 127.05,
    "itemSize": "MEDIUM"
  }
]
```

**에러 케이스**
| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 404 | V001 | vehicleId에 해당하는 차량 없음 |

- 차량이 `AVAILABLE`이 아니거나 조건에 맞는 배송 요청이 하나도 없으면 에러가 아니라 **빈 배열(`[]`)**을 200으로 반환한다.

---

### 3.3 `GET /api/v1/matchings/{matchingId}` — 매칭 단건 조회

**Request**

Path Parameters
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| matchingId | number | Y | 조회할 매칭 ID |

**Response**

성공 (`200 OK`) — §3.1과 동일한 `MatchingResponse` 형태

**에러 케이스**
| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 404 | D002 | matchingId에 해당하는 매칭 없음 |

---

### 3.4 `GET /api/v1/matchings` — 매칭 전체 목록 조회

**Request**
- 파라미터 없음. (페이지네이션 미적용 — `List<MatchingResponse>`를 그대로 반환하며 `Page<T>`가 아니다. §7 참고)

**Response**

성공 (`200 OK`) — `MatchingResponse` 배열

---

### 3.5 `PUT /api/v1/matchings/{matchingId}` — 매칭 상태 변경

**설명**
- 매칭 상태를 전이한다. 허용되는 전이만 가능하다: `MATCHED`→`COMPLETED`/`CANCELLED`, `CANCELLED`→`MATCHED`(재매칭). `COMPLETED`는 종료 상태라 다른 상태로 못 감.

**Request**

Path Parameters
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| matchingId | number | Y | 상태를 바꿀 매칭 ID |

Body
| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|---|---|---|---|---|
| status | string | Y | 변경할 상태 | `MATCHED`/`COMPLETED`/`CANCELLED` 중 하나, `@NotNull` |

```json
{ "status": "COMPLETED" }
```

**Response**

성공 (`200 OK`) — §3.1과 동일한 `MatchingResponse` 형태 (요청 상태가 현재 상태와 같으면 아무것도 안 바뀐 채 그대로 반환)

**에러 케이스**
| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 404 | D002 | matchingId에 해당하는 매칭 없음 |
| 409 | D006 | 허용되지 않는 상태 전이 (예: COMPLETED → MATCHED) |
| 409 | V003 | (재매칭 시) 차량이 AVAILABLE이 아님 |
| 400 | V002 | (재매칭 시) 차량이 배송 조건을 감당 못함 |

---

### 3.6 `DELETE /api/v1/matchings/{matchingId}` — 매칭 삭제

**설명**
- 매칭을 삭제한다. 삭제 시점에 상태가 `MATCHED`(진행 중)였다면 배송 요청을 `REQUESTED`로, 차량을 `AVAILABLE`로 되돌린다. 이미 `COMPLETED`/`CANCELLED`였던 매칭은 되돌리지 않고 그냥 삭제만 한다.

**Request**

Path Parameters
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| matchingId | number | Y | 삭제할 매칭 ID |

**Response**

성공 (`204 No Content`) — 본문 없음

**에러 케이스**
| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 404 | D002 | matchingId에 해당하는 매칭 없음 |

---

## 4. 공통 응답 규격 (Common Response Format)
- 성공 응답은 각 엔드포인트가 정의한 DTO(또는 그 배열)를 그대로 반환한다. 별도 `{success, data}` 래퍼는 없다.
- 에러 응답은 `GlobalExceptionHandler`가 `ErrorResponse`로 통일해서 반환한다 (§3.1 예시 참고): `status`/`code`/`message`/`timestamp`/`traceId`/`errors`.

## 5. 공통 에러 코드 (Common Error Codes)
이 API에서 실제로 쓰이는 도메인 코드만 정리 (전체 공통 코드는 `docs/templates/api-template.md` §5 참고).

| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 404 | D001 | 존재하지 않는 배송 요청입니다 |
| 404 | D002 | 존재하지 않는 매칭입니다 |
| 409 | D003 | 이미 처리된 배송 요청입니다 |
| 409 | D006 | 허용되지 않는 매칭 상태 전이입니다 |
| 404 | V001 | 존재하지 않는 차량입니다 |
| 400 | V002 | 차량이 배송 조건을 감당할 수 없습니다 |
| 409 | V003 | 이미 배정되어 사용할 수 없는 차량입니다 |

## 6. 인증/권한 (Auth)
- **실제 코드 확인 결과, `MatchingController`의 6개 엔드포인트 전부에 `@PreAuthorize`가 붙어 있지 않다.** `SecurityConfig`가 `anyRequest().permitAll()`로 필터 체인 레벨의 기본 접근은 전부 허용해두고, 인증/인가는 컨트롤러 메서드별 `@PreAuthorize`로 개별 적용하는 구조인데(`DeliveryController`/`NotificationController` 등은 `@PreAuthorize("isAuthenticated()")`가 붙어 있음), `MatchingController`만 이게 빠져 있다.
- 즉 지금은 **토큰 없이도 누구나 `POST /api/v1/matchings`로 아무 배송 요청이나 수락하거나, 다른 사람 매칭을 `PUT`/`DELETE`할 수 있다.** 위 §2 표의 "인증 필요" 열이 "명세상 Y"라고 적은 이유가 이것이다.
- 소유권 검증(예: "이 차량이 진짜 내 차량인지")도 코드 어디에도 없다 — `MatchingCreateRequest.vehicleId`를 요청자가 그냥 아무 값이나 넣을 수 있다.

## 7. Rate Limit / 정책
- 호출 제한: 구현되어 있지 않음.
- 페이지네이션: `GET /api/v1/matchings`는 페이지네이션 없이 전체 목록을 `List`로 반환한다 (다른 도메인의 목록 API가 쓰는 `Pageable`/`Page<T>` 패턴과 다르다 — 데이터가 많아지면 문제가 될 수 있음, §9 참고).

## 8. 변경 이력 (Changelog)
| 버전 | 날짜 | 변경 내용 |
|---|---|---|
| v1.0 | 2026-08-05 | 최초 작성 |

## 9. 오픈 이슈 (Open Questions)
- [ ] **(보안, 우선순위 높음)** `MatchingController`에 인증/소유권 검증이 전혀 없다. `@PreAuthorize("isAuthenticated()")` 추가와, "요청자 본인 소유 차량인지" 검증 로직이 필요해 보인다.
- [ ] `GET /api/v1/matchings`가 페이지네이션 없이 전체 목록을 반환하는데, 매칭 건수가 늘어나면 다른 도메인처럼 `Pageable`로 바꿔야 하지 않을지 확인 필요.
- [ ] 배송 요청 생성 시 후보 차량에게 WebSocket으로 오퍼가 푸시되는 별도 경로(`/topic/vehicles/{vehicleId}/offers`)가 있는데, 이건 REST가 아니라서 이 문서 범위 밖이다 — 별도 실시간 API 명세 문서가 필요한지 확인 필요.
