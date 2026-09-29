# 📦 SQL 제공 스크립트 (스키마 초기화 & 더미 데이터)

외부 제공·리뷰·로컬 샌드박스 구축용으로 **전체 스키마 1개 파일**과 **테스트 더미 데이터 1개 파일**을 분리해 제공합니다.

| 파일 | 역할 | 포함 내용 |
| :--- | :--- | :--- |
| [`schema-init.sql`](./schema-init.sql) | 스키마 초기화 | 테이블 11개 + 인덱스/FK + 기준 데이터(카테고리 12종, 초기 공지 3건) |
| [`dummy-data.sql`](./dummy-data.sql) | 더미 데이터 | 회원 7명, 장비 5대, 배송 요청 8건, 매칭 5건, 포인트 지갑 7건·원장 13건, 알림 8건, 주소 5건, 공지 2건 |

---

## 🚀 실행 방법

```bash
# 0. (최초 1회) 데이터베이스 생성 — 한글 데이터가 있어 utf8mb4 필수
mysql -u root -p -e "CREATE DATABASE shinhan_delivery DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 1. 스키마 생성
mysql -u root -p shinhan_delivery < scripts/sql/schema-init.sql

# 2. 더미 데이터 적재
mysql -u root -p shinhan_delivery < scripts/sql/dummy-data.sql
```

> Windows에서 `mysql` 명령을 찾지 못하면 MariaDB 설치 경로의 `bin`을 PATH에 추가하거나
> `"C:\Program Files\MariaDB 11\bin\mysql.exe"` 처럼 전체 경로로 실행하세요.
> MariaDB 11부터는 `mysql` 대신 `mariadb` 명령을 써도 동일하게 동작합니다.

---

## ⚠️ 꼭 알아야 할 2가지

### 1. 스키마 변경은 여전히 Flyway로만 합니다

`schema-init.sql`은 Flyway 마이그레이션 `V1 ~ V32`를 하나로 합친(squash) **스냅샷**입니다.
스키마를 바꿀 때는 이 파일을 수정하는 게 아니라 **새 마이그레이션 파일**
(`src/main/resources/db/migration/V33__설명.sql`)을 추가해야 합니다.
→ [Flyway 마이그레이션 가이드](../../docs/architecture/Flyway-마이그레이션-가이드.md)

마이그레이션이 추가되면 이 파일도 함께 갱신해 주세요.

### 2. `schema-init.sql`로 만든 DB에 앱을 붙일 때는 Flyway를 끕니다

이미 테이블이 있는 DB에 Flyway가 `V1`부터 다시 적용을 시도해 "테이블이 이미 존재한다"는 오류가 납니다.

```bash
# schema-init.sql로 만든 DB에 붙이는 경우
SPRING_FLYWAY_ENABLED=false ./gradlew bootRun
```

반대로 **빈 데이터베이스**에 `./gradlew bootRun`을 하면 Flyway가 `V1~V32`를 자동 적용하므로
`schema-init.sql`을 실행할 필요가 없습니다. 이때는 더미 데이터만 넣으면 됩니다.

---

## 🔑 테스트 계정 (비밀번호 모두 `password123`)

| 이메일 | 이름 | 역할 | 특징 |
| :--- | :--- | :--- | :--- |
| `admin@shinhan.com` | 관리자 | ADMIN | 배송원/장비 승인 화면 확인용 |
| `client@example.com` | 김화주 | CUSTOMER | 잔액 466,709P, 결제 PIN `135790`, 배송 4건 보유 |
| `customer2@example.com` | 최고객 | CUSTOMER | 완료 1건 + 고객 취소(수수료 정산) 1건 |
| `customer3@example.com` | 정단골 | CUSTOMER | 자동 타임아웃 취소·전액 환불 1건, 미결제 요청 1건 |
| `courier1@example.com` | 박배송 | COURIER | 승인 완료, 트럭·오토바이 2대, 이동 보상 1,000P 수령 |
| `courier2@example.com` | 이배송 | COURIER | 승인 완료, 오토바이·도보 2대 |
| `courier3@example.com` | 신입배송 | COURIER | **승인 대기(PENDING)** + 장비도 심사 대기 → 관리자 승인 플로우 테스트용 |

## 🚚 배송 데이터 시나리오 (배송 전 상태 재현)

| ID | 상태 | 설명 |
| :-- | :--- | :--- |
| 1 | `REQUESTED` | 결제 완료, 배송원 매칭 대기 |
| 2 | `MATCHED` | 박배송 트럭이 수락 → 실시간 위치 추적 화면 테스트용 |
| 3 | `PICKED_UP` | 이배송 오토바이가 픽업 완료, 배송 중 |
| 4 | `COMPLETED` | 완료 + 인증 사진 보유 |
| 5 | `CANCELLED` | 매칭 후 고객 취소 → 수수료 1,000P, 환불 7,221P, 배송원 보상 1,000P |
| 6 | `CANCELLED` | 30분 미배정 자동 취소 → 13,872P 전액 환불 |
| 7 | `REQUESTED` | 포인트 결제를 거치지 않은 요청 (`payment_idempotency_key IS NULL`) |
| 8 | `COMPLETED` | 커스텀 전달 요청(CUSTOM) + 참고 사진 보유 |

배송비는 `DeliveryFeeCalculator` 공식(기본 3,000 + 거리 500/km + 무게 200/kg, 크기 할증
SMALL 0% / MEDIUM 30% / LARGE 60%)으로 계산했고, `point_history` 원장의 잔액 흐름과
`point_wallet.balance`도 서로 일치합니다.

---

## 🔁 Java 기반 더미 데이터 시더와의 관계

`DATA_SEED_ENABLED=true`로 기동하면 `DataSeedInitializer`가 더미 데이터를 적재하지만,
이미 회원이 2명 이상이면 **관리자 계정 확인 외에는 아무것도 하지 않고 건너뜁니다.**
따라서 `dummy-data.sql`을 적재한 DB에서는 두 방식이 충돌하지 않습니다.

| 구분 | Java 시더 (`DataSeedInitializer`) | `dummy-data.sql` |
| :--- | :--- | :--- |
| 실행 시점 | 앱 기동 시 자동 | 개발자가 직접 실행 |
| 데이터 규모 | 회원 4명 / 배송 1건 | 회원 7명 / 배송 8건 (전 상태 재현) |
| 적합한 용도 | 로컬 최소 기동 | 데모·QA·화면 전수 확인, 외부 제공 |

---

## ✅ 검증 이력

두 파일은 MariaDB 11.8에서 다음을 확인했습니다.

- `schema-init.sql`로 만든 스키마 == Flyway `V1~V32`를 순차 적용한 스키마
  (컬럼 112개 / 인덱스 / 외래키 전수 비교 일치)
- `dummy-data.sql`은 두 방식으로 만든 스키마 모두에 정상 적재되며, **재실행해도 같은 상태**가 됩니다
  (맨 앞 초기화 블록이 더미 데이터 범위만 삭제)
- 포인트 원장 합계 == 지갑 잔액, 배송비 == 원장 결제액, 취소 수수료 + 환불 == 결제액,
  매칭 장비의 적재 능력 >= 배송 요청 스펙 — 모두 일치
