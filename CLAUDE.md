# CLAUDE.md

이 파일은 이 저장소에서 작업할 때 Claude Code(claude.ai/code)에게 필요한 안내를 제공합니다.

## 프로젝트 개요

`shinhan-gaecheokja`는 Spring Boot 4.1.0 / Java 17 기반 백엔드(Gradle 빌드)입니다. Flyway로 데이터베이스 스키마를 버전 관리합니다(`docs/flyway-guide.md` 참고).

## 코딩 컨벤션 (필독)

**코드를 작성하거나 수정하기 전, 그리고 이 저장소에서 git 작업(커밋, 브랜치 생성)을 하기 전에 반드시 `code-convention.md`를 읽으세요.** 이 문서는 이 저장소의 기준이 되는 명세로, 전통적인 계층형 아키텍처(`Controller → Service → Repository → Entity`)와 예외 기반 에러 처리, git 작업 방식(§16: 커밋 메시지 형식, 브랜치 네이밍, PR 규칙), PR 체크리스트(§17)를 다룹니다. 모든 신규 코드와 모든 커밋은 이 문서를 따라야 합니다. 여기서는 요약하거나 다르게 서술하지 않으니, 이 문서는 독립적으로 최신 상태를 유지하므로 반드시 원문을 직접 참고하세요.

코딩 전에 숙지해야 할 핵심 사항(자세한 내용, 코드 예시, PR 체크리스트는 `code-convention.md`에 있습니다):

- 레이어(기술적 역할) 기준으로 패키지를 나눕니다: `controller`, `service`, `repository`, `entity`, `dto/{request,response}`, `exception`, `config`. 의존 방향은 항상 위에서 아래로만(`Controller → Service → Repository`) — 역방향 의존은 금지입니다.
- `Entity`는 JPA 표준 방식(`@Entity`, Lombok `@Getter`/`@Setter`/`@NoArgsConstructor`)을 그대로 사용합니다. Value Object나 `Result` 타입 같은 함수형 패턴은 쓰지 않습니다.
- 프론트엔드/UI 개발 시 React 등 외부 프레임워크를 사용하지 않고 `HTML5 + Vanilla CSS + Thymeleaf` 스택을 사용하며, 무조건 공통 디자인 시스템(`/css/design-system.css`, `docs/design-system.md`) 토큰 및 Thymeleaf 프래그먼트(`templates/fragments/components.html`)를 100% 사용하여 개발합니다.
- 비즈니스 로직은 전부 `Service` 계층에 모읍니다. 생성자 주입(`@RequiredArgsConstructor`)만 쓰고 필드 주입(`@Autowired` on field)은 금지입니다. 하나의 public 메서드가 하나의 유스케이스입니다.
- 예상 가능한 실패는 커스텀 `RuntimeException`을 던지고, `@RestControllerAdvice`인 `GlobalExceptionHandler` 한 곳에서만 HTTP 응답으로 변환합니다(컨벤션 §6). Controller에서 개별적으로 `try-catch`하지 않습니다.
- `@Transactional`은 `Service` 계층에만 붙입니다. Controller는 DTO만 다루고 Entity를 직접 반환하지 않습니다.
- 포맷팅은 Spotless + google-java-format으로 강제됩니다(2칸 들여쓰기, wildcard import 금지) — 스타일 취향이 아니라 규칙이므로, 커밋 전에 `spotlessApply`를 실행하세요.
- 커밋 메시지: `type: 설명`(Conventional Commits 기반, 한글 설명), `type`은 `feat|fix|refactor|test|docs|chore` 중 하나(컨벤션 §16). 브랜치명: `feat/도메인-내용`, `develop`을 대상으로 PR합니다(`hotfix`는 `main` 예외).

## 명령어

Gradle wrapper를 사용하세요(Windows에서는 `gradlew.bat`, bash에서는 `./gradlew`) — 전역에 설치된 Gradle에 의존하지 마세요.

```
./scripts/verify.sh            # 로컬 CI 통합 하네스 검증 (Flyway + Spotless + Test 한번에 구동)
gradlew.bat build              # 전체 빌드 (컴파일 + 테스트)
gradlew.bat bootRun            # 애플리케이션 실행
gradlew.bat test               # 전체 테스트 실행
gradlew.bat spotlessCheck      # 포맷팅 검사 (CI에서 실행 / 작업 완료 선언 전에 실행)
gradlew.bat spotlessApply      # 컨벤션에 맞게 자동 포맷팅
```

### AI Pre-Flight Self-Review (사전 셀프 코드 리뷰) & 자가 치유 피드백 루프 (AI 에이전트 필독)

코드를 생성하거나 수정한 후, 작업을 완료하거나 `./pr`을 구동하기 전에 **AI 에이전트는 아래 2단계 사전 검토 루프를 거쳐야 합니다**:

1. **1단계 - AI 사전 셀프 코드 리뷰 (Pre-Flight Self-Review):**
   - 수정한 코드에 미사용 import, 불필요한 System.out 출력, 변수/메서드 명명 규칙 위반, 주석 미비 사항이 없는지 스스로 1차 셀프 코드 리뷰를 수행하고 보정합니다.
2. **2단계 - Test Harness 자가 치유 피드백 루프:**
   - `./scripts/verify.sh`를 구동하여 오류(Spotless 포맷팅 위반, ArchUnit 아키텍처 규칙 위반, Flyway 규격 오류, 단위 테스트 실패) 발생 시, 출력된 에러 메시지와 스택 트레이스를 분석해 스스로 코드를 수정한 뒤 성공(exit status 0)할 때까지 루프를 반복합니다.
3. **3단계 - Multi-Pass Project Audit (우리 프로젝트 맞춤형 다회차 재검토):**
   - 1차 빌드/테스트를 통과했더라도, **실제 우리 프로젝트에 적합하고 안전한지 아래 6대 프로젝트 관점**에서 2차, 3차 다각도로 재검토하여 완성도를 100% 확보하세요:
     1. **아키텍처 순수성:** Controller에서 Entity 반환 금지, Controller -> Repository 직접 참조 금지 (`code-convention.md` 준수)
     2. **비즈니스 예외 안전성:** 커스텀 예외 던짐 및 `GlobalExceptionHandler` 응답 매핑 부합 여부
     3. **운영 DB & 마이그레이션 안전성:** Flyway Online DDL 규격 준수 및 기존 데이터 정합성 위배 여부
     4. **보안 & 데이터 프라이버시:** 하드코딩된 Secret/API 토큰 및 개인정보 유출 위험 여부
     5. **초보자 개발자 경험 (DX):** Mac/Windows 크로스 플랫폼 지원 및 오류 메시지의 친절한 해설 여부
     6. **실질적 테스트 가치:** 단순 통과용 깡통 테스트가 아닌 실제 회귀 버그를 잡는 유의미한 검증인가?

로컬 DB 연결(`.env`)과 Flyway 마이그레이션 작성법은 `README.md`, `docs/flyway-guide.md`를 참고하세요.

## 아키텍처

- 베이스 패키지: `com.example.shinhangaecheokja`.
- `code-convention.md` §2 기준 목표 구조: `controller`(DTO만 다룸), `service`(비즈니스 로직·트랜잭션·예외 발생), `repository`(`~Repository extends JpaRepository<Entity, Long>`, Entity 1개당 1개), `entity`(`@Entity` + Lombok), `dto/{request,response}`, `exception`(커스텀 예외 + `GlobalExceptionHandler`), `config`.
- 새 도메인 로직을 추가할 때는 "Controller → Service → Repository" 단방향 의존과 "Repository는 Service/Controller에 의존하지 않는다"는 규칙(컨벤션 §14)을 지키세요. 아직 이 규칙을 빌드 시점에 강제하는 ArchUnit 테스트는 없으니, 필요하면 컨벤션 §14의 예시를 참고해 추가하세요.
- 영속성: MariaDB(`org.mariadb.jdbc`) 대상 Spring Data JPA + Flyway. 스키마 변경은 항상 새 마이그레이션 파일(`src/main/resources/db/migration/V<n>__설명.sql`)로 추가하고, `spring.jpa.hibernate.ddl-auto`는 `validate`(또는 `none`)로 유지합니다 — Hibernate가 스키마를 직접 생성하게 하면 안 됩니다.
