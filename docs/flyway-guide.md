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

### 2단계: 신규 마이그레이션 SQL 파일 생성 (자동 생성 명령어)
* 새로운 마이그레이션 SQL 파일 틀을 만들려면 터미널에서 다음 명령어를 실행합니다:
  ```bash
  ./gradlew migrationCreate -Pdesc=설명문
  ```
  * 예: `./gradlew migrationCreate -Pdesc=add_phone_to_user`를 실행하면 현재 존재하는 마지막 버전의 다음 번호를 자동으로 계산하여 `V2__add_phone_to_user.sql` 빈 파일을 `db/migration/` 아래에 만들어 줍니다.
* 생성된 파일 내에 스키마 변경 DDL 쿼리를 작성합니다:
  ```sql
  ALTER TABLE user ADD COLUMN phone VARCHAR(20);
  ```

> 💡 **Tip (JPA Buddy 활용 - 자동 SQL 생성):** 만약 `ALTER TABLE` DDL 구문을 직접 타이핑하기 번거로운 경우, IntelliJ 플러그인인 **JPA Buddy**를 활용해 보세요. 플러그인 설치 후 `Flyway` -> `Diff Versioned Migration` 메뉴를 실행하면 Java Entity 클래스의 변경 코드와 데이터베이스 상태를 대조하여 위와 같은 SQL 변경 쿼리를 자동으로 뽑아서 마이그레이션 파일로 저장해 줍니다.

### 3단계: 애플리케이션 실행 및 검증
* 서버를 실행(`bootRun`)하면 Flyway가 자동으로 `V2` 마이그레이션 파일을 감지하여 DB에 적용합니다.
* 마이그레이션 완료 후 Hibernate가 Entity와 실제 DB의 일치 여부를 검증(`validate`)하며 정상 구동됩니다.

