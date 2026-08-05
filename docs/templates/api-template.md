# API 명세서 템플릿

## 문서 정보
| 항목 | 내용 |
|---|---|
| API/기능명 | |
| 작성자 | |
| 작성일 | |
| 버전 | v1.0 |
| 상태 | 초안 / 검토중 / 확정 |
| 관련 PRD/ERD 링크 | |

---

## 1. 개요 (Overview)
- 이 API가 하는 역할 (한두 문장)
- Base URL: `/api/v1` (별도 API 서버가 아니라 이 Spring Boot 애플리케이션이 정적 프론트엔드와 같은 origin에서 서빙)
- 인증 방식: `Authorization: Bearer {accessToken}` (JWT, `JwtProvider` 발급)

## 2. 엔드포인트 목록 (Endpoint Summary)
| Method | Endpoint | 설명 | 인증 필요 |
|---|---|---|---|
| GET | /comments | 댓글 목록 조회 | Y |
| POST | /comments | 댓글 생성 | Y |
| PATCH | /comments/{id} | 댓글 수정 | Y |
| DELETE | /comments/{id} | 댓글 삭제 | Y |

---

## 3. 엔드포인트 상세 (Endpoint Details)

### 3.1 `POST /comments` — 댓글 생성

**설명**
- 게시물에 댓글을 작성한다.

**Request**

Headers
| 이름 | 필수 | 설명 |
|---|---|---|
| Authorization | Y | `Bearer {access_token}` |
| Content-Type | Y | `application/json` |

Path Parameters
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| - | - | - | - |

Query Parameters
| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| - | - | - | - |

Body
| 필드명 | 타입 | 필수 | 설명 | 제약조건 |
|---|---|---|---|---|
| post_id | number | Y | 대상 게시물 ID | |
| content | string | Y | 댓글 내용 | 최대 500자 |

```json
{
  "post_id": 123,
  "content": "댓글 내용입니다."
}
```

**Response**

성공 (`201 Created`)
| 필드명 | 타입 | 설명 |
|---|---|---|
| id | number | 생성된 댓글 ID |
| post_id | number | 게시물 ID |
| content | string | 댓글 내용 |
| created_at | string(ISO8601) | 생성일시 |

```json
{
  "id": 456,
  "post_id": 123,
  "content": "댓글 내용입니다.",
  "created_at": "2026-08-05T09:00:00Z"
}
```

**에러 케이스**
| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 400 | INVALID_CONTENT | content가 비어있거나 500자 초과 |
| 401 | A001 | 인증 토큰 없음/만료 |
| 404 | POST_NOT_FOUND | post_id에 해당하는 게시물 없음 |

```json
{
  "status": 400,
  "code": "INVALID_CONTENT",
  "message": "댓글 내용은 1자 이상 500자 이하여야 합니다.",
  "timestamp": "2026-08-05T09:00:00",
  "traceId": "abc123",
  "errors": [
    { "field": "content", "value": "", "reason": "댓글 내용은 1자 이상 500자 이하여야 합니다." }
  ]
}
```

> 엔드포인트가 여러 개면 위 3.1 구조를 반복 (3.2, 3.3 …)

---

## 4. 공통 응답 규격 (Common Response Format)
- 성공 응답은 각 엔드포인트가 정의한 DTO를 그대로 JSON으로 반환한다(별도 `{success, data}` 래퍼 없음).
- 에러 응답은 `GlobalExceptionHandler`가 `ErrorResponse`로 통일해서 반환한다:
```json
{
  "status": 400,
  "code": "C001",
  "message": "유효하지 않은 입력값입니다.",
  "timestamp": "2026-08-05T09:00:00",
  "traceId": "abc123",
  "errors": []
}
```
- `errors`는 `@Valid` 필드 검증 실패처럼 필드별 상세가 있을 때만 채워지고, 그 외에는 빈 배열이다.
- `traceId`는 MDC 기반 분산 로깅 추적 ID로, 장애 발생 시 로그와 대조하는 용도다.

## 5. 공통 에러 코드 (Common Error Codes)
`ErrorCode.java`의 `C`(공통)/`A`(인증) 접두사 코드 기준. 도메인별 에러 코드(`M`회원/`V`차량/`D`배송/`P`포인트/`N`알림)는 각 엔드포인트의 "에러 케이스"에 개별 기재한다.

| 상태 코드 | 에러 코드 | 설명 |
|---|---|---|
| 400 | C001 | 유효하지 않은 입력값입니다 |
| 405 | C002 | 지원하지 않는 HTTP 메서드입니다 |
| 500 | C003 | 서버 내부 오류가 발생했습니다 |
| 404 | C004 | 존재하지 않는 리소스입니다 |
| 400 | C005 | 허용되지 않는 파일 형식입니다 |
| 400 | C006 | 파일 크기가 허용 범위를 초과했습니다 |
| 403 | C007 | 접근 권한이 없습니다 |
| 401 | A001 | 인증 권한이 필요합니다 |

## 6. 오픈 이슈 (Open Questions)
- [ ] 
- [ ] 
