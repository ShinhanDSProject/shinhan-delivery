# 🗄️ 배송원 워크스페이스 & 배송 매칭 시스템 ERD 명세서

> **Document Version**: `v1.1.0`  
> **Last Updated**: `2026-08-06`  
> **Author**: ShinhanDS Delivery Database Engineering Team  
> **Target Audience**: Backend Developers, DBAs, Frontend Developers & AI Agents  

---

## 📌 1. 개요 (Overview)

본 명세서는 **배송원(Courier) 워크스페이스 및 배송 매칭 시스템**의 데이터 모델링(Database Entity Relationship Diagram)을 정의합니다.

* **Database**: MySQL 8.0+ / H2 (Test Environment)
* **ORM Engine**: Spring Data JPA / Hibernate
* **동시성 제어**: 낙관적 락(Optimistic Locking via `@Version`)

---

## 📐 2. ERD 다이어그램 (Mermaid ER Diagram)

```mermaid
erDiagram
    MEMBER ||--o{ VEHICLE : "소유한다 (1:N)"
    MEMBER ||--o{ DELIVERY_REQUEST : "요청한다 (1:N)"
    MEMBER ||--|| POINT_WALLET : "보유한다 (1:1)"
    
    VEHICLE ||--o{ MATCHING : "배차수행한다 (1:N)"
    DELIVERY_REQUEST ||--|| MATCHING : "매칭된다 (1:1)"
    POINT_WALLET ||--o{ POINT_HISTORY : "기록된다 (1:N)"

    MEMBER {
        bigint id PK "회원 식별자"
        string email UK "이메일 계정"
        string password "암호화된 비밀번호"
        string name "회원 성명"
        string phone_number "전화번호"
        string role "권한 (CUSTOMER / COURIER / ADMIN)"
        string activity_region "주 활동 지역"
        double preferred_weight "선호 배송 무게"
    }

    VEHICLE {
        bigint id PK "운송수단 식별자"
        bigint owner_id FK "소유 배송원 ID (MEMBER.id)"
        string type "수단 종류 (MOTORCYCLE / WALK / BICYCLE 등)"
        double max_weight "최대 적재 무게(kg)"
        double max_distance "최대 이동 거리(km)"
        double latitude "현재 GPS 위도"
        double longitude "현재 GPS 경도"
        string status "영업 상태 (AVAILABLE / BUSY / OFFLINE)"
    }

    DELIVERY_REQUEST {
        bigint id PK "배송 요청 식별자"
        bigint customer_id FK "요청 고객 ID (MEMBER.id)"
        string pickup_address "픽업 출발지 주소"
        string dropoff_address "배송 도착지 주소"
        double pickup_latitude "픽업지 위도"
        double pickup_longitude "픽업지 경도"
        double weight "물품 무게(kg)"
        double distance "이동거리(km)"
        string item_size "물품 크기 (SMALL / MEDIUM / LARGE)"
        string status "배송 상태 (REQUESTED / MATCHED / DELIVERING / COMPLETED / CANCELLED)"
        bigint fee_point "배송 수수료 (포인트)"
        bigint version "낙관적 락 버전"
    }

    MATCHING {
        bigint id PK "배차 매칭 식별자"
        bigint delivery_request_id FK, UK "배송 요청 ID (DELIVERY_REQUEST.id)"
        bigint vehicle_id FK "배차 차량 ID (VEHICLE.id)"
        string status "매칭 상태 (MATCHED / DELIVERING / COMPLETED / CANCELLED)"
        datetime matched_at "매칭 일시"
    }

    POINT_WALLET {
        bigint id PK "지갑 식별자"
        bigint member_id FK, UK "회원 ID (MEMBER.id)"
        bigint balance_point "잔여 포인트"
    }

    POINT_HISTORY {
        bigint id PK "이력 식별자"
        bigint wallet_id FK "지갑 ID (POINT_WALLET.id)"
        bigint amount "변동 포인트"
        string type "변동 유형 (CHARGE / PAY / EARN / REFUND)"
        datetime created_at "기록 일시"
    }
```

---

## 📑 3. 테이블별 상세 명세서 (Data Dictionary)

### 1️⃣ `MEMBER` (회원 테이블)
> **고객(CUSTOMER)과 배송원(COURIER) 정보를 관리하는 핵심 엔티티**

| Column Name | Data Type | Nullable | Key | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | **No** | **PK** | Auto Increment | 회원 고유 식별자 |
| `email` | VARCHAR(100) | **No** | **UK** | Unique | 로그인 이메일 계정 |
| `password` | VARCHAR(255) | **No** | - | BCrypt Hashed | 암호화된 비밀번호 |
| `name` | VARCHAR(50) | **No** | - | - | 회원 성명 (기사님 이름) |
| `phone_number` | VARCHAR(20) | **No** | - | - | 연락처 |
| `role` | VARCHAR(20) | **No** | - | Enum | `CUSTOMER`, `COURIER`, `ADMIN` |
| `activity_region` | VARCHAR(100) | Yes | - | - | 배송원 주 활동 지역 |
| `preferred_weight`| DOUBLE | Yes | - | - | 배송원 선호 최대 배송 무게 |

---

### 2️⃣ `VEHICLE` (운송 수단 & 위치/영업상태 테이블)
> **배송원(COURIER)이 등록한 배송 수단 및 실시간 GPS 위치, 온라인/오프라인 영업 상태 관리**

| Column Name | Data Type | Nullable | Key | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | **No** | **PK** | Auto Increment | 운송수단 식별자 |
| `owner_id` | BIGINT | **No** | **FK** | -> `MEMBER.id` | 소유 배송원 회원 ID |
| `type` | VARCHAR(20) | **No** | - | Enum | `MOTORCYCLE`, `WALK`, `BICYCLE`, `KICKBOARD`, `DRONE` |
| `max_weight` | DOUBLE | **No** | - | > 0 | 최대 적재 가능 무게(kg) |
| `max_distance` | DOUBLE | **No** | - | > 0 | 최대 운행 가능 거리(km) |
| `latitude` | DOUBLE | **No** | - | -90.0 ~ 90.0 | 배송원 실시간 위도 (GPS) |
| `longitude` | DOUBLE | **No** | - | -180.0 ~ 180.0 | 배송원 실시간 경도 (GPS) |
| `status` | VARCHAR(20) | **No** | - | Enum | `AVAILABLE`(온라인/대기), `BUSY`(배송중), `OFFLINE`(퇴근) |

---

### 3️⃣ `DELIVERY_REQUEST` (배송 요청/주문 테이블)
> **고객이 신청한 배송 요청 정보 및 낙관적 락(Optimistic Locking) 버전 관리**

| Column Name | Data Type | Nullable | Key | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | **No** | **PK** | Auto Increment | 배송 요청 식별자 |
| `customer_id` | BIGINT | **No** | **FK** | -> `MEMBER.id` | 요청한 고객 회원 ID |
| `pickup_address` | VARCHAR(255) | **No** | - | - | 픽업 출발지 주소 |
| `dropoff_address`| VARCHAR(255) | **No** | - | - | 배송 도착지 주소 |
| `pickup_latitude`| DOUBLE | Yes | - | - | 픽업 위치 위도 |
| `pickup_longitude`| DOUBLE | Yes | - | - | 픽업 위치 경도 |
| `weight` | DOUBLE | **No** | - | > 0 | 물품 무게(kg) |
| `distance` | DOUBLE | **No** | - | > 0 | 출발지-도착지 이동거리(km) |
| `item_size` | VARCHAR(20) | Yes | - | Enum | `SMALL`, `MEDIUM`, `LARGE` |
| `status` | VARCHAR(20) | **No** | - | Enum | `REQUESTED`, `MATCHED`, `DELIVERING`, `COMPLETED`, `CANCELLED` |
| `fee_point` | BIGINT | **No** | - | >= 0 | 배송 수수료 포인트 |
| `version` | BIGINT | **No** | - | `@Version` | **동시성 제어용 낙관적 락 버전** |

---

### 4️⃣ `MATCHING` (배차 매칭 테이블)
> **배송 요청(`DELIVERY_REQUEST`)과 수락한 배송원의 수단(`VEHICLE`) 간 매칭 정보**

| Column Name | Data Type | Nullable | Key | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `id` | BIGINT | **No** | **PK** | Auto Increment | 배차 매칭 식별자 |
| `delivery_request_id` | BIGINT | **No** | **FK, UK** | -> `DELIVERY_REQUEST.id` (Unique) | 1:1 매칭되는 배송 요청 ID |
| `vehicle_id` | BIGINT | **No** | **FK** | -> `VEHICLE.id` | 수락한 배송원의 차량 ID |
| `status` | VARCHAR(20) | **No** | - | Enum | `MATCHED`, `DELIVERING`, `COMPLETED`, `CANCELLED` |
| `matched_at` | DATETIME | **No** | - | Default `NOW()` | 배차 성공 일시 |

---

## ⚡ 4. 핵심 엔티티 관계 & 동시성 제어 메커니즘

1. **배송원 - 차량 (1:N)**
   - 배송원(`MEMBER`)은 1개 이상의 운송수단(`VEHICLE`)을 보유할 수 있으며, 현재 출근 시 사용하는 `VEHICLE.status`가 `AVAILABLE`(온라인)로 업데이트됩니다.

2. **배송 요청 - 매칭 (1:1 Unique)**
   - `DELIVERY_REQUEST.id`와 `MATCHING.delivery_request_id`는 1:1 관계이며 `UNIQUE` 제약조건이 걸려 있어 중복 배차를 방지합니다.

3. **수락 경합 (Optimistic Lock)**
   - 여러 배송원이 동일한 대기 요청(`REQUESTED`)을 동시 수락할 때 `DELIVERY_REQUEST.version`으로 **낙관적 락**을 검증합니다. 가장 먼저 수락한 1명만 DB 커밋에 성공하고, 늦게 요청한 배송원에게는 `409 Conflict` 예외가 발생합니다.
