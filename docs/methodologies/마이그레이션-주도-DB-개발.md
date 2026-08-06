---
metadata:
  version: "1.0.0"
  ssot_owner: "docs/methodologies/마이그레이션-주도-DB-개발.md"
  last_updated: "2026-07-31"
  status: "APPROVED"
---

# 🗄️ Migration-Driven Development (마이그레이션 주도 DB 개발) 학습 가이드

이 문서는 `shinhan-delivery` 프로젝트에서 **DB DDL 조작을 직접 수행하지 않고, 버전 관리된 마이그레이션 스크립트와 무중단 Online DDL 규격을 사수하는 마이그레이션 주도 DB 개발** 방법론의 가이드북입니다.

---

## 📌 1. 마이그레이션 주도 DB 개발이란 무엇인가? (WHY)

로컬/운영 데이터베이스에 직접 SQL을 수동으로 구동하거나 `hibernate.hbm2ddl.auto=update`에 의존하면, **개발 환경 간 스키마 불일치(Schema Drift)** 및 운영 DB 데이터 파괴 위험이 발생합니다.

마이그레이션 주도 개발은 **모든 DB 스키마 변경을 버전 관리된 Flyway SQL 스크립트로 작성하고 무중단 Online DDL 규격을 준수**하여 배포하는 전략입니다.

```mermaid
graph LR
    Dev["💻 개발자 DDL 수정"] --> FlywayScript["📜 Flyway SQL 작성<br/>(V{버전}__{설명}.sql)"]
    FlywayScript --> Harness["🛡️ ./scripts/verify.sh<br/>(Flyway 파일명 & 무중단 DDL 검사)"]
    Harness --> DB["🗄️ MariaDB 스키마 마이그레이션"]
```

---

## 📐 2. Flyway 3대 필수 작성 수칙 (Core Rules)

### ① 📜 파일명 명명 규격
- `V{버전}__{설명}.sql` (예: `V1.0.1__create_members_table.sql`) — 언더바 2개(`__`) 필수.

### ② 🛡️ 무중단 Online DDL 규격
- 테이블/컬럼 추가 시 기존 운영 서비스를 마비시키는 락(Lock)을 방지하고 `ALGORITHM=INPLACE, LOCK=NONE` 수칙 적용.

### ③ 🚫 파괴적 변경(Breaking Change) 금지
- 사용 중인 컬럼 삭제/이름 변경 시 즉시 삭제하지 않고 2단계 마이그레이션 전략 수립.

---

## 💻 3. 우리 프로젝트 실천 가이드

- [Flyway 데이터베이스 마이그레이션 가이드 (docs/Flyway-마이그레이션-가이드.md)](../Flyway-마이그레이션-가이드.md)
