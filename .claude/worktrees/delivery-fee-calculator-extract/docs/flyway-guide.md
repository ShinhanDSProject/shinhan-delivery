# Flyway 데이터베이스 마이그레이션 가이드

이 문서에서는 프로젝트의 데이터베이스 형상 관리 도구인 **Flyway**의 작동 원리, 규칙 및 유의사항에 대해 상세히 설명합니다.

---

## 1. 작동 방식 (How it works)
* Spring Boot 애플리케이션이 구동(Run)될 때, `src/main/resources/db/migration` 폴더 내의 마이그레이션 SQL 스크립트를 감지하여 데이터베이스에 자동으로 적용합니다.
* 마이그레이션 수행 이력은 데이터베이스 내의 `flyway_schema_history` 테이블에 기록되며, 이미 성공적으로 적용된 파일은 다시 실행되지 않고 스킵됩니다.

---

## 2. 마이그레이션 파일 명명 규칙
스키마 변경이나 데이터 추가(Seed) 등의 작업이 필요할 때, 규칙에 맞춘 SQL 파일을 추가해야 올바른 순서대로 반영됩니다.

* **파일 위치:** `src/main/resources/db/migration/`
* **파일명 포맷:** `V<버전번호>__<설명>.sql` (주의: 언더스코어`_`는 반드시 **2개**여야 합니다.)
* **예시:**
  * `V1__init_schema.sql` (최초 스키마 생성)
  * `V2__add_phone_to_user.sql` (사용자 테이블 컬럼 추가)
  * `V3__create_post_table.sql` (게시판 테이블 생성)

---

## 3. 핵심 유의 사항 (필독 ⚠️)

### ⚠️ 이미 반영된 파일은 절대 수정하지 마세요.
* 데이터베이스에 반영된 마이그레이션 파일은 해시값(Checksum)으로 기록됩니다.
* 만약 `V1__init_schema.sql`이 이미 로컬이나 서버 DB에 반영되었는데 해당 파일의 SQL 코드를 임의로 수정하면, 다음 서버 기동 시 **Checksum 불일치 오류**와 함께 애플리케이션 시작에 실패하게 됩니다.
* 잘못 작성된 쿼리나 스키마 수정 사항이 있다면, 항상 버전 번호를 하나 올린 **새로운 마이그레이션 파일(예: `V2`)을 만들어 추가 작성**해야 합니다.

### ⚠️ JPA ddl-auto 속성은 validate 혹은 none으로 설정해야 합니다.
* Hibernate가 스키마를 직접 생성하지 않도록 `spring.jpa.hibernate.ddl-auto` 설정은 `validate`로 유지해야 합니다. 그렇지 않으면 Hibernate와 Flyway의 라이프사이클이 꼬이거나, 마이그레이션 히스토리 없이 임의로 컬럼이 추가/삭제될 수 있습니다.

---

## 4. 실무 개발 및 사용 흐름 (JPA Entity 추가/변경 시)

프로젝트에서 새로운 Java Entity 클래스를 추가하거나 기존 Entity의 필드를 수정할 때 데이터베이스에 반영하는 단계별 사용 흐름입니다. **(단순히 Entity 클래스만 생성/수정해서는 DB에 반영되지 않습니다.)**

### 1단계: Java Entity 코드 작성
* 예: `User` 엔티티에 `phone` 필드 추가
  ```java
  @Column(length = 20)
  private String phone;
  ```

### 2단계: 신규 마이그레이션 SQL 파일 생성 (JPA Buddy 활용)
JPA Buddy 플러그인을 활용하여 Java Entity 코드와 실제 데이터베이스 스키마 간의 차이점(Diff)을 분석하고, 마이그레이션 SQL 파일을 자동으로 생성하는 방법입니다.

1. **IntelliJ 플러그인 설치**
   * IntelliJ의 설정 창 (`Preferences` ➔ `Plugins`)에서 **JPA Buddy**를 검색하여 설치합니다.
2. **Flyway Versioned Migration 메뉴 실행**
   * `src/main/resources/db/migration` 폴더를 우클릭한 후 **New** ➔ **Flyway Versioned Migration**을 클릭합니다.
   * (또는 IntelliJ 하단의 **JPA Structure** 도구 창에서 **Flyway** ➔ **Diff Versioned Migration**을 클릭합니다.)
3. **대조 대상(Source & Target) 설정**
   * **Source (기준이 되는 소스 코드):** `JPA Entities`를 선택합니다.
   * **Target (변경 전의 데이터베이스):** 로컬 데이터베이스 커넥션을 선택합니다.
4. **마이그레이션 스크립트 저장**
   * 파일의 설명 설명문(예: `add_phone_to_user`)을 작성한 뒤 **OK**를 누르면, 플러그인이 자동으로 버전 번호를 계산하여 `V2__add_phone_to_user.sql` 형태로 빈 스크립트 파일을 만들고, 내부에 테이블 변경용 `ALTER TABLE ... ADD COLUMN` DDL 쿼리문까지 완성하여 저장해 줍니다.
   * 생성된 SQL 쿼리에 이상이 없는지 최종 검토합니다.

### 3단계: 애플리케이션 실행 및 검증
* 서버를 실행(`bootRun`)하면 Flyway가 자동으로 `V2` 마이그레이션 파일을 감지하여 DB에 적용합니다.
* 마이그레이션 완료 후 Hibernate가 Entity와 실제 DB의 일치 여부를 검증(`validate`)하며 정상 구동됩니다.

