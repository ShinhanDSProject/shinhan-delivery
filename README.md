# 신한 개척자 (shinhan-gaecheokja) - Spring Boot & Flyway 가이드

이 프로젝트는 Spring Boot 기반 백엔드 애플리케이션으로, 데이터베이스 형상 관리(Migration) 도구로 **Flyway**를 사용하며, 로컬 환경 설정을 위해 **.env** 파일을 연동하고 있습니다.

처음 접하는 개발자가 로컬 개발 환경을 세팅하고 데이터베이스 작업을 수행할 수 있도록 정리한 가이드입니다.

---

## 1. 로컬 환경 설정 (.env)

로컬 데이터베이스 연결 정보를 포함한 민감한 환경 변수들은 프로젝트 루트의 `.env` 파일로 관리합니다. 이 파일은 보안을 위해 Git 추적에서 제외(`.gitignore`)되어 있습니다.

### 세팅 순서
1. 프로젝트 루트 디렉토리에 `.env` 파일을 생성합니다.
2. 아래 템플릿 내용을 복사한 뒤, 로컬 데이터베이스 환경에 맞게 값을 수정하여 저장합니다.

```env
# 로컬 MariaDB 접속 정보 설정
DB_URL=jdbc:mariadb://localhost:3306/shinhan_gaecheokja
DB_USER=root
DB_PASSWORD=your_password_here
```

> **작동 원리:** Spring Boot가 실행될 때 `spring-dotenv` 라이브러리가 이 파일을 읽어 `application.yaml` 내부의 `${DB_URL}` 등의 플레이스홀더 변수에 자동으로 값을 주입합니다.

---

## 2. 데이터베이스 마이그레이션 (Flyway)

이 프로젝트는 데이터베이스 스키마(테이블 구조 등)의 변경 사항을 SQL 스크립트를 통해 버전별로 관리합니다.

### 작동 방식
* Spring Boot 애플리케이션이 시작될 때, `src/main/resources/db/migration` 폴더 아래의 마이그레이션 SQL 파일들을 읽어 미적용된 파일들을 데이터베이스에 자동 반영합니다.
* 데이터베이스에는 `flyway_schema_history` 테이블이 자동으로 생성되어, 어떤 마이그레이션이 언제 반영되었는지 이력을 추적합니다.

### 마이그레이션 파일 작성 규칙
새로운 테이블을 생성하거나 컬럼을 추가/수정해야 하는 경우, 아래 명명 규칙에 맞게 SQL 파일을 추가해야 합니다.

* **경로:** `src/main/resources/db/migration/`
* **파일명 규칙:** `V<버전번호>__<설명>.sql` (중간에 언더스코어`_`가 **2개** 들어가야 합니다.)
  * 예시:
    * `V1__init_schema.sql` (최초 스키마 생성)
    * `V2__add_phone_to_user.sql` (유저 테이블 컬럼 추가)
    * `V3__create_post_table.sql` (포스트 테이블 신규 생성)

> **주의 사항 (필독):**
> 1. **버전 순서:** 버전 번호는 순차적으로 늘어나야 합니다. (`V1` ➔ `V2` ➔ `V3` ...)
> 2. **수정 불가:** 한 번 적용되어 Git에 올라간 마이그레이션 SQL 파일은 **절대 수정하면 안 됩니다.** 이미 실행된 파일의 내용을 수정하면 해시값(Checksum)이 불일치하여 애플리케이션이 구동되지 않습니다. 변경 사항이 있다면 항상 다음 버전(예: `V3`)의 SQL 파일을 추가 작성하여 적용해야 합니다.

### Node.js (TypeORM) 개발자를 위한 개념 매핑

| 기능 / 개념 | TypeORM | Spring Boot & Flyway |
| :--- | :--- | :--- |
| **마이그레이션 실행** | `typeorm migration:run` | 서버 구동 시 자동 실행 |
| **파일 언어 및 형태** | TS/JS 파일 (`up()` / `down()`) | 순수 DDL SQL 파일 (`V1__init.sql`) |
| **이력 관리 테이블** | `migrations` | `flyway_schema_history` |
| **롤백 / 다운그레이드** | `typeorm migration:revert` | 지원 안 함 (Forward-Only: 다음 버전의 SQL로 덮어쓰기) |
| **변경사항 자동 생성** | `typeorm migration:generate` | IntelliJ의 **JPA Buddy** 플러그인 활용 권장 |

---

## 3. 애플리케이션 실행 방법

모든 세팅이 완료되었다면 아래 명령어를 통해 프로젝트를 구동할 수 있습니다. 서버가 켜지면서 마이그레이션이 함께 자동으로 실행됩니다.

```bash
./gradlew bootRun
```
