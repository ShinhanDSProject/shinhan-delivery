# CLAUDE.md

이 파일은 이 저장소에서 작업할 때 Claude Code(claude.ai/code)에게 필요한 안내를 제공합니다.

## 프로젝트 개요

`shinhan-gaecheokja`는 Spring Boot 4.1.0 / Java 17 기반 백엔드(Gradle 빌드)입니다. 아직 초기 단계로, 현재 코드베이스에는 엔티티, 컨트롤러, 서비스, 리포지토리가 없습니다.

## 코딩 컨벤션 (필독)

**코드를 작성하거나 수정하기 전, 그리고 이 저장소에서 git 작업(커밋, 브랜치 생성)을 하기 전에 반드시 `code-convention.md`를 읽으세요.** 이 문서는 이 저장소의 기준이 되는 명세로, 소스 코드 규칙(DDD + FP + Railway-oriented programming, 예상 가능한 실패에는 예외 대신 `Result<S, F>` 사용)뿐 아니라 git 작업 방식(§15: 커밋 메시지 형식, 브랜치 네이밍)과 PR 체크리스트(§16)까지 포함합니다. 모든 신규 코드와 모든 커밋은 이 문서를 따라야 합니다. 여기서는 요약하거나 다르게 서술하지 않으니, 이 문서는 독립적으로 최신 상태를 유지하므로 반드시 원문을 직접 참고하세요.

코딩 전에 숙지해야 할 핵심 사항(자세한 내용, 코드 예시, PR 체크리스트는 `code-convention.md`에 있습니다):

- 레이어별로 나눈 뒤 도메인별로 패키지를 구성합니다: `domain/<feature>`, `application/<feature>`, `infrastructure/persistence`, `presentation`, `common`. `domain`은 프레임워크(Spring/JPA) 의존성이 전혀 없어야 하며, 이는 `LayeredArchitectureTest`(ArchUnit)로 강제됩니다(아래 참고).
- Value Object는 `private` compact constructor와 `Result<VO, DomainError>`를 반환하는 정적 팩토리 `of(...)`를 가진 `record`로 작성합니다 — record 생성자를 public으로 노출하지 않고, 생성자에서 `throw`하지 않습니다.
- 도메인 코드에는 setter/변경(mutation)이 없습니다. 상태 변경은 새 객체를 반환하는 방식으로 합니다. 예상 가능한 실패는 `Result`로 반환합니다(`common/result/Result.java`에 이미 스캐폴딩되어 있음). `throw`는 진짜 인프라 예외 상황에만 사용합니다.
- `@Transactional`은 `application`(UseCase) 레이어에만 붙입니다. 로깅은 `domain`에서 절대 하지 않습니다. 컨트롤러는 DTO만 다루고 도메인 객체를 직접 다루지 않으며, 공통 매퍼(`DomainErrorHttpMapper`, 컨벤션 §11 참고) 하나를 통해 `Result`를 HTTP 상태로 변환합니다.
- 포맷팅은 Spotless + google-java-format으로 강제됩니다(2칸 들여쓰기, wildcard import 금지) — 스타일 취향이 아니라 규칙이므로, 커밋 전에 `spotlessApply`를 실행하세요.
- 커밋 메시지: `type: 설명`(Conventional Commits 기반, 한글 설명), `type`은 `feat|fix|refactor|test|docs|chore` 중 하나(컨벤션 §15). 브랜치명: `type/도메인-내용`.

`.githooks/`의 git hook이 `core.hooksPath`가 설정되면(클론 후 1회 `git config core.hooksPath .githooks` 실행 필요) 위 규칙을 자동으로 강제합니다: `pre-commit`은 `spotlessApply`를 실행하고 포맷된 `.java` 파일을 다시 스테이징하며, `commit-msg`는 커밋 메시지가 `type: 설명` 형식이 아니면 커밋을 거부합니다.

## 명령어

Gradle wrapper를 사용하세요(Windows에서는 `gradlew.bat`, bash에서는 `./gradlew`) — 전역에 설치된 Gradle에 의존하지 마세요.

```
gradlew.bat build              # 전체 빌드 (컴파일 + 테스트)
gradlew.bat bootRun            # 애플리케이션 실행
gradlew.bat test               # 전체 테스트 실행
gradlew.bat test --tests "com.example.shinhangaecheokja.ShinhanGaecheokjaApplicationTests"   # 특정 테스트 클래스 실행
gradlew.bat test --tests "com.example.shinhangaecheokja.ShinhanGaecheokjaApplicationTests.contextLoads"  # 특정 테스트 메서드 실행
gradlew.bat spotlessCheck      # 포맷팅 검사 (CI에서 실행 / 작업 완료 선언 전에 실행)
gradlew.bat spotlessApply      # 컨벤션에 맞게 자동 포맷팅
```

## 아키텍처

- 베이스 패키지: `com.example.shinhangaecheokja`.
- `code-convention.md` 기준 목표 구조: `domain/<feature>`(Aggregate Root, Value Object, Repository 인터페이스 — 프레임워크 의존성 없음), `application/<feature>`(UseCase, request/response DTO, `@Transactional`), `infrastructure/persistence`(Spring Data 리포지토리 구현체), `presentation`(컨트롤러, DTO만 다룸), `common`(`result/Result.java`, `error/DomainError.java`, `http/DomainErrorHttpMapper.java`).
- `LayeredArchitectureTest`(`src/test/java/.../architecture/`)는 `domain` 클래스가 Spring이나 `application`/`infrastructure`/`presentation`에 의존하면 빌드를 실패시키는 ArchUnit 테스트입니다. 새 도메인 코드를 추가할 때 이 테스트가 계속 통과하도록 유지하세요.
- 영속성: MariaDB(`org.mariadb.jdbc`) 대상 Spring Data JPA. `src/main/resources/application.yaml`은 현재 `spring.application.name`만 설정되어 있고 datasource/profile 설정이 없으므로, 앱이 실제로 데이터베이스에 연결하려면 MariaDB 연결 설정이 필요합니다.
