# 🏛️ 신한 개척자 6대 필수 검증 하네스 명세서 (Project Audit Harness Spec)

이 문서는 `shinhan-gaecheokja` 프로젝트에서 개발자 및 AI 에이전트가 코드를 변경할 때 **엄격히 준수해야 하는 6대 필수 검증 하네스 규칙**을 정의한 단일 진실 출처(Single Source of Truth) 명세서입니다.

---

## 📌 6대 필수 검증 하네스 요약표

| 검증 영역 | 필수 준수 규칙 | 자동 검증 도구 / 하네스 |
| :--- | :--- | :--- |
| **1. 아키텍처 순수성** | `Controller → Service → Repository` 단방향 의존 / Controller에서 Entity 직접 반환 금지 | `ArchUnitTest.java` (`./gradlew test`) |
| **2. 비즈니스 예외 안전성** | 예상 가능한 실패는 커스텀 `RuntimeException` 던짐 및 `GlobalExceptionHandler` 응답 매핑 | `GlobalExceptionHandlerTest` / 리뷰 체크리스트 |
| **3. 무중단 DB 마이그레이션** | Flyway 파괴적 SQL(Online DDL 위반) 금지 / 기존 데이터 적재 상태 마이그레이션 통과 | `lint-flyway-filenames.sh`<br>`lint-flyway-ddl.sh`<br>`FlywayMigrationDataCompatibilityTest` |
| **4. 보안 & Secret 관리** | API 키, DB 비밀번호, PII(개인정보) 소스코드 하드코딩 금지 | `.gitignore` (`.env` 격리) 및 CI 검사 |
| **5. 초보자 DX & 크로스플랫폼** | Mac과 Windows 어디서나 원클릭 구동 지원 / 친절한 에러 로그 출력 | `./pr` (Mac/Linux)<br>`./gradlew verify` (Windows/공통) |
| **6. 유의미한 테스트 가치** | 통과만을 위한 빈 테스트 금지 / 실질적 회귀 버그를 잡는 given-when-then 테스트 | JUnit 5 & JaCoCo 커버리지 |

---

## 1. 아키텍처 순수성 검증 하네스 🏛️
* **규칙:** Controller는 요청 수신 및 DTO 변환만 담당하며, Repository를 직접 참조할 수 없습니다. 모든 비즈니스 로직은 Service 계층에 위치합니다.
* **하네스 연결:** `src/test/java/com/example/shinhangaecheokja/common/ArchUnitTest.java`

## 2. 비즈니스 예외 안전성 검증 하네스 ⚠️
* **규칙:** Controller에서 개별적인 `try-catch`를 남발하지 않고, 비즈니스 예외 발생 시 커스텀 예외를 던져 `GlobalExceptionHandler` 한 곳에서 HTTP 4xx/5xx 표준 응답으로 변환해야 합니다.

## 3. 무중단 DB 마이그레이션 검증 하네스 🛢️
* **규칙:** 테이블 수정 시 기존 운영 데이터를 파괴하는 SQL(예: 기본값 없는 NOT NULL 컬럼 추가 등)을 금지합니다.
* **하네스 연결:** `scripts/lint-flyway-filenames.sh`, `scripts/lint-flyway-ddl.sh`, `FlywayMigrationDataCompatibilityTest.java`

## 4. 보안 & Secret 관리 하네스 🔒
* **규칙:** DB 접속 정보, Secret Key, 개인정보는 절대 Git에 커밋되지 않아야 하며 로컬 `.env`로 격리 관리합니다.

## 5. 초보자 DX & 크로스 플랫폼 하네스 🐣
* **규칙:** 개발 초보자도 OS(Mac/Windows) 구분 없이 원클릭 명령어로 검증부터 PR까지 마칠 수 있어야 합니다.
* **하네스 연결:** `./pr` 및 `./gradlew verify`

## 6. 실질적 테스트 가치 검증 하네스 🧪
* **규칙:** 모든 신규 서비스 기능은 회귀 버그를 방지할 수 있는 유효한 단위/통합 테스트를 동반해야 합니다.
