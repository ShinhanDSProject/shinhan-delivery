# AGENTS.md

이 저장소에서 코드를 작성하는 모든 AI 에이전트(Claude Code, Cursor, Copilot, Antigravity 등)는 아래 지침과 하네스 검증 규칙을 준수해야 합니다.

## 1. 코딩 컨벤션 지침
* 모든 코드 작성 및 수정 전 `code-convention.md`와 `CLAUDE.md`를 필독하세요.
* 의존성 방향은 항상 단방향(`Controller -> Service -> Repository`)을 유지해야 합니다.
* Controller는 DTO만 다루며 Entity를 직접 반환해서는 안 됩니다.
* **Getter, Setter 작성 시 수동 코딩을 금지하고 무조건 Lombok 라이브러리(`@Getter`, `@Setter`)를 100% 사용하여 개발해야 합니다.**
* **신규 기술 도입 및 핵심 아키텍처 결정 시 `docs/adr/` 규격에 따라 공식 ADR(Architecture Decision Record) 문서를 필수 작성해야 합니다.**
* **PR 생성 시 리뷰어(관리자)의 검토 소요 시간을 단축하기 위해 `docs/pr-review-guide.md` 규격에 따라 [리뷰어 3분 족보 가이드] 및 [Files changed 핀포인트 인라인 댓글]을 필수 작성/부착해야 합니다.**

## 2. AI Pre-Flight Self-Review & Test Harness 자가 치유 피드백 루프
작업을 완료하거나 `./pr`을 구동하기 전에 **반드시 아래 2단계 사전 검토를 완료해야 합니다**:

1. **AI 사전 셀프 코드 리뷰 (Pre-Flight Self-Review):**
   - 수정한 코드에 미사용 import, 불필요한 `System.out.println` 콘솔 출력, 명명 규칙 위반, 주석 누락이 없는지 스스로 1차 검토 후 보정하세요.
2. **Test Harness 자가 치유 피드백 루프:**
   - `./scripts/verify.sh` (또는 `./pr`)를 실행하여 린트, 코드 포맷팅(Spotless), ArchUnit 아키텍처 규칙, 단위 테스트 실패 시 에러 로그를 읽고 수초 내로 자가 치유(Auto-Fix)하여 0 exit code 상태를 확보하세요. (`docs/harness-decision-framework.md` 6대 통제 정책 준수)
3. **Multi-Pass Project Audit 피드백 루프:**
   - 1차 검증을 통과했더라도 아래 **6대 프로젝트 맞춤형 관점**에서 2차, 3차 다각도로 재검토하여 안전성과 완성도를 100% 확보하세요:
     1) 아키텍처 순수성 (`Controller`에서 `Entity` 반환 금지)
     2) 비즈니스 예외 안전성 (`GlobalExceptionHandler` 매핑)
     3) 운영 DB & Flyway 마이그레이션 무중단 규격 준수
     4) 보안 & Secret/개인정보 유출 위험 여부
     5) 초보자 개발자 경험 (Mac/Windows 크로스 플랫폼 지원)
     6) 실질적 회귀 버그 검증 가치가 있는 유의미한 테스트인가?

## 3. 🏆 World-Class Quality Principles (세계 최고 IT 전문가 수준 8대 작업 원칙)
모든 AI 에이전트는 평범한 작업물이 아닌 **Google, Apple, Meta, Netflix 수준의 최고 결과물**을 도출하기 위해 다음 8대 원칙을 철저히 준수해야 합니다:

1. **결함 0개 사수 (Zero-Defect Quality Gate):**
   - 소스 수정 후 반드시 `./scripts/verify.sh` (Spotless + ArchUnit + JaCoCo 커버리지 60%+ 게이트 + 46개 전체 테스트)를 수행하여 100% 그린 빌드가 검증된 무결점 결과물만 최종 제출합니다.
2. **KISS & Clean Architecture (클래스 폭발 차단):**
   - 불필요하게 예외 클래스나 유틸리티를 무분별하게 늘리지 않고, 공통 `EntityNotFoundException` + `ErrorCode`처럼 명쾌하고 간결한 아키텍처(KISS 원칙)와 단방향 의존성만 사수합니다.
3. **초급자 튜터링 문서 동기화 (100% 지식 자산화):**
   - 기능 및 패턴 변경 시 신입 개발자도 이해할 수 있는 초급자 눈높이 문서(`docs/`) 및 `code-convention.md`를 소스 코드와 세트로 100% 최신화합니다.
4. **AI 사전 셀프 리뷰 & 다회차 교정 (Multi-Pass Self-Correction):**
   - 1차 코드 생성에 만족하지 않고, 스스로 6대 프로젝트 관점(아키텍처, 예외, DB, 보안, DX, 테스트 유의미성)에서 사전 3차례 셀프 검토를 거쳐 결함을 완벽히 보완한 결과만 제공합니다.
5. **무상태성 및 멱등성 사수 (Stateless & Idempotency):**
   - 모든 Service 및 Controller는 공유 인스턴스 필드 상태를 가지지 않는 순수 무상태(Stateless)로 설계하며, 동시성 환경에서도 멱등성(Idempotency)과 스레드 안전성을 사수합니다.
6. **다층 방어 보안 및 개인정보 무노출 (Defense-in-Depth Security):**
   - 비밀번호, 개인정보, Secret Key는 로그(`log.info`), 예외 메시지, JSON DTO에 절대 노출하지 않으며, SQL Injection / XSS 방어 및 입력값 샌드박싱 검증을 필수 배치합니다.
7. **실증 데이터 기반 원인 진단 (Empirical Evidence-Driven Diagnosis):**
   - 버그 발생 시 추측이나 짐작으로 코드를 수정하는 행위("이럴 것 같아서 수정했습니다")를 금지합니다. 반드시 실제 스택트레이스, 로그, 재생 가능한 실패 테스트라는 객관적 실증 증거를 수집한 후에만 수정을 집행합니다.
8. **기존 API 계약 및 하위 호환성 100% 유지 (Zero Side-Effect Stability):**
   - 기존 응답 DTO 필드, HTTP Status, 파라미터 시그니처의 파괴적 변경(Breaking Change)을 금지하며, 사용 중인 기존 기능에 부작용(Side-effect)이 없음을 전수로 검증합니다.
