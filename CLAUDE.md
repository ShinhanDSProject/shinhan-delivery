# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`shinhan-gaecheokja` is a Spring Boot 4.1.0 / Java 17 backend (Gradle build). It is early-stage: the codebase currently contains no entities, controllers, services, or repositories yet.

## Coding convention (required reading)

**Read `code-convention.md` before writing or modifying any code.** It is the authoritative spec for this repo — DDD (domain-driven design) + FP (functional programming) + Railway-oriented programming (`Result<S, F>` instead of exceptions for expected failures). All new code must follow it; do not summarize/deviate from it here, refer to the file directly since it is kept up to date independently of this file.

Key points to internalize before coding (full detail, code samples, and the PR checklist are in `code-convention.md`):

- Package by layer then by domain: `domain/<feature>`, `application/<feature>`, `infrastructure/persistence`, `presentation`, `common`. `domain` must have zero framework (Spring/JPA) dependencies — enforced by `LayeredArchitectureTest` (ArchUnit), see below.
- Value Objects are `record`s with a `private` compact constructor and a static `of(...)` factory returning `Result<VO, DomainError>` — never a public record constructor, never a `throw` from the constructor.
- No setters/mutation in domain code; state changes return new objects. Expected failures return `Result` (`common/result/Result.java`, already scaffolded); `throw` is reserved for genuine infrastructure exceptions.
- `@Transactional` only in the `application` (UseCase) layer. Logging never happens in `domain`. Controllers only touch DTOs, never domain objects, and convert `Result` → HTTP status via a single shared mapper (`DomainErrorHttpMapper`, per convention §11).
- Formatting is enforced by Spotless + google-java-format (2-space indent, no wildcard imports) — not a style preference, run `spotlessApply` before committing.

## Commands

Use the Gradle wrapper (`gradlew.bat` on Windows, `./gradlew` in bash) — do not rely on a globally installed Gradle.

```
gradlew.bat build              # full build (compile + test)
gradlew.bat bootRun            # run the application
gradlew.bat test               # run all tests
gradlew.bat test --tests "com.example.shinhangaecheokja.ShinhanGaecheokjaApplicationTests"   # run a single test class
gradlew.bat test --tests "com.example.shinhangaecheokja.ShinhanGaecheokjaApplicationTests.contextLoads"  # run a single test method
gradlew.bat spotlessCheck      # verify formatting (run in CI / before declaring work done)
gradlew.bat spotlessApply      # auto-format to match convention
```

## Architecture

- Base package: `com.example.shinhangaecheokja`.
- Target layout per `code-convention.md`: `domain/<feature>` (Aggregate Roots, Value Objects, Repository interfaces — framework-free), `application/<feature>` (UseCases, request/response DTOs, `@Transactional`), `infrastructure/persistence` (Spring Data repository implementations), `presentation` (controllers, DTO-only), `common` (`result/Result.java`, `error/DomainError.java`, `http/DomainErrorHttpMapper.java`).
- `LayeredArchitectureTest` (`src/test/java/.../architecture/`) is an ArchUnit test that fails the build if `domain` classes depend on Spring or on `application`/`infrastructure`/`presentation`. Keep it passing when adding new domain code.
- Persistence: Spring Data JPA against MariaDB (`org.mariadb.jdbc`). `src/main/resources/application.yaml` currently only sets `spring.application.name`; no datasource/profile config exists yet, so a MariaDB connection must be configured before the app can actually connect to a database.
