# 신한 개척자 (shinhan-gaecheokja)

이 프로젝트는 Spring Boot 기반 백엔드 애플리케이션입니다.

---

## 🚀 빠른 시작 (Quick Start)

### 1. 로컬 환경 설정 (.env)
로컬 데이터베이스 연결 설정을 위해 프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 아래 내용을 입력합니다.
*(주의: `.env` 파일은 절대 Git에 커밋되지 않도록 제외처리 되어 있습니다.)*

```env
# 로컬 MariaDB 설정
DB_URL=jdbc:mariadb://localhost:3306/shinhan_gaecheokja
DB_USER=root
DB_PASSWORD=your_password_here

# 로컬 테스트용 더미 데이터(회원, 지갑, 차량 등) 자동 적재 여부 (true/false)
DATA_SEED_ENABLED=true
```

### 2. 애플리케이션 실행
아래 명령어를 사용하여 애플리케이션을 기동합니다.
```bash
./gradlew bootRun
```

💡 *실행 중 에러가 발생하거나 연결이 되지 않는다면 [**로컬 개발 트러블슈팅 가이드 (docs/troubleshooting.md)**](./docs/troubleshooting.md)를 참고해 주세요.*

### 3. 로컬 CI 검증 및 원클릭 PR 제출 하네스 (./pr)
개발 완료 또는 소스 수정 후 터미널에 아래 명령어를 실행하면, **Flyway 린트, Spotless 포맷팅, ArchUnit 아키텍처 검증, JaCoCo 커버리지 게이트(60%+), 46개 전체 테스트**를 1초 만에 검증하고 커밋/PR까지 일괄 처리합니다.
```bash
./pr
# 또는 검증만 수행 시: ./scripts/verify.sh
```
💡 *JaCoCo 테스트 커버리지 시각화 HTML 리포트는 `./gradlew jacocoTestReport` 실행 후 `build/reports/jacoco/test/html/index.html`에서 브라우저로 바로 확인하실 수 있습니다.*

### 4. Git 커밋 템플릿 설정 (.gitmessage)
협업 규칙에 따른 일관된 커밋 작성을 위해 아래 명령어로 로컬 커밋 템플릿을 등록해 주세요. 등록 후 `git commit` 실행 시 버퍼 창에 템플릿 힌트가 자동으로 채워집니다.
```bash
git config --local commit.template .gitmessage
```

👉 [**로컬 개발 환경 및 자동화 도구 사용 가이드 바로가기 (docs/developer-env-guide.md)**](./docs/developer-env-guide.md)

---

## 📚 프로젝트 문서 인덱스 (Documentation Index)

프로젝트에 구축된 모든 개발 가이드라인과 기존 개발 기능들에 대한 요구사항 명세서 및 설계서의 전체 맵입니다. 
아래 링크를 클릭하여 해당하는 가이드 및 설계 내용을 확인하실 수 있습니다.

### 🏆 세계 최고 수준 개발 표준 & AI 행동 규범
* [**팀 개발 문화 & 일하는 방식 7대 철학 가이드 (docs/engineering-culture-and-working-style.md)**](./docs/engineering-culture-and-working-style.md) - 비난 없는 심리적 안전, 15분 질문 룰, 공감 코드 리뷰 7대 문화 🕊️
* [**프로젝트 착수 전 6대 사전 리스크 감사 & 거버넌스 가이드 (docs/pre-launch-risk-and-governance-guide.md)**](./docs/pre-launch-risk-and-governance-guide.md) - 개발자/리더/PM 관점 6대 리스크 방어 수칙 🛡️
* [**팀 리더십 & 프로젝트 운용 프레임워크 가이드 (docs/team-operating-model-guide.md)**](./docs/team-operating-model-guide.md) - 리더와 팀원이 협업할 때 준수할 5대 운용 기둥 및 문화 프레임워크 👑
* [**AI 에이전트 행동 지침 및 8대 수칙 (AGENTS.md)**](./AGENTS.md) - Google, Apple, Meta 수준의 최고 결과물을 도출하기 위한 AI/개발자 8대 무결점 작업 원칙 🏛️
* [**코딩 컨벤션 및 6대 개발 규칙 (code-convention.md)**](./code-convention.md) - 우리 프로젝트에서 개발자와 AI가 엄격히 준수해야 할 단일 원본 코딩 규약 📐

### 📜 아키텍처 의사결정 기록 (ADR - Architecture Decision Records)
* [**ADR-0001: JWT 기반 무상태 인증 체계 채택 (docs/adr/0001-stateless-jwt-authentication.md)**](./docs/adr/0001-stateless-jwt-authentication.md) - 왜 무상태 JWT 인증을 채택했는지 세션/Basic 인증과의 비교 분석 기록 📜

### 🛠️ 개발 가이드 및 협업 규칙
* [**AI 기반 초급자 페어 프로그래밍 & 차근차근 개발 가이드 (docs/ai-paired-development-guide.md)**](./docs/ai-paired-development-guide.md) - 초급자가 AI와 5단계 순차 워크플로우로 무결점 코드를 완성하는 가이드 🤖
* [**초급자 전용 CRUD & 레이어별 초상세 이슈 분할 가이드 (docs/beginner-crud-issue-template-guide.md)**](./docs/beginner-crud-issue-template-guide.md) - DTO, Entity, Service, Controller 단계별 디테일 가이드 템플릿 🔰
* [**학습 및 지식 공유형 GitHub Issue 분할 가이드 (docs/learning-oriented-issue-guide.md)**](./docs/learning-oriented-issue-guide.md) - 단순 개발을 넘어 팀 전체의 동반 성장을 이끄는 3단계 학습형 이슈 작성 규격 🎓
* [**초급 개발자를 위한 7단계 초상세 태스크 분할 가이드 (docs/junior-developer-task-guide.md)**](./docs/junior-developer-task-guide.md) - 입문자가 막연함 없이 100% 무결점 코드를 완성하도록 인도하는 Step-by-Step 가이드북 🔰
* [**Deliver Happiness 전체 기능 명세서 & 개발 로드맵 (docs/project-spec-and-task-breakdown.md)**](./docs/project-spec-and-task-breakdown.md) - 10대 모듈, 30+ REST API 및 6대 스프린트 파트별(BE/FE/DevOps) 태스크 할당서 📦
* [**단일 원본 관리(SSOT) 문서 정책 가이드 (docs/ssot-documentation-policy.md)**](./docs/ssot-documentation-policy.md) - 정보 중복을 방지하고 지식의 파편화를 차단하는 프로젝트 단일 원본 문서 관리 규격 🏛️
* [**테스트 하네스 구축 및 검증 항목 판단 정책 가이드 (docs/harness-decision-framework.md)**](./docs/harness-decision-framework.md) - 하네스에 추가할 검증 항목 4대 판단 프레임워크 및 무결점 결함 0개를 위한 초엄격 6대 통제 정책 🛡️
* [**PR 리뷰어 3분 족보 가이드 작성 규격 (docs/pr-review-guide.md)**](./docs/pr-review-guide.md) - 리뷰어의 검토 피로도를 낮추고 3분 만에 핵심 코드를 파악하게 돕는 PR 가이드 작성 규격 🗺️
* [**초보 개발자 온보딩 및 기능 개발 로드맵 (docs/onboarding-roadmap.md)**](./docs/onboarding-roadmap.md) - 입문자를 위한 필수 학습 순서 및 실전 기능 개발 7단계 흐름 가이드 🚀
* [**테스트 하네스 & LLM 피드백 루프 가이드 (docs/harness-and-llm-guide.md)**](./docs/harness-and-llm-guide.md) - 초보자와 AI 사용자를 위한 자동 검사 하네스 및 에러 자가 치유 활용법 🏗️
* [**LLM 기반 현대 SW 엔지니어링 방법론 가이드 (docs/llm-software-engineering-guide.md)**](./docs/llm-software-engineering-guide.md) - 프롬프트/하네스/컨텍스트 엔지니어링, 루프 엔지니어링, EDD 등 AI 활용 개발 방법론 입문서 🤖
* [**협업 문화 및 자동화 도구 도입 배경 가이드 (docs/development-culture-guide.md)**](./docs/development-culture-guide.md) - 왜 이런 협업 규칙과 DevOps 도구들을 도입했는지, 미도입 시 어떤 장애 참사가 발생하는지 설명해 주는 입문자 필독서 🎓
* [**Git Flow, 이슈 수칙 및 커밋 컨벤션 가이드 (docs/git-flow-guide.md)**](./docs/git-flow-guide.md) - 브랜치 운용 규칙, GitHub Issue 템플릿 태그 수칙, Conventional Commits 헤더 가이드 및 자동 코드 리뷰 연동 규칙 🔀
* [**CI/CD 파이프라인 및 GitHub Actions Step 해설 가이드 (docs/cicd-pipeline-guide.md)**](./docs/cicd-pipeline-guide.md) - 지속적 통합/배포 개념, JaCoCo Coverage Gate, Actions 동작 원리 및 워크플로우 각 Step별 해설 ⚙️
* [**기능 개발 전 설계 단계 프로세스 가이드 (docs/design-phase-guide.md)**](./docs/design-phase-guide.md) - 기능 개발에 착수하기 전 작성해야 할 4대 핵심 산출물 양식과 2단계 PR 전략
* [**RESTful API 설계 및 규격 가이드 (docs/rest-api-guide.md)**](./docs/rest-api-guide.md) - REST API 개념, 자원/행위 매핑 규칙, 초보자 안티패턴 및 HTTP 상태 코드 표준 응답 규칙 🌐
* [**전역 예외 처리 및 표준 에러 코드 가이드 (docs/exception-handling-guide.md)**](./docs/exception-handling-guide.md) - @RestControllerAdvice 작동 원리, ErrorCode Enum, ErrorResponse DTO 및 방어적 프로그래밍 수칙 🛡️
* [**로컬 개발 환경 및 자동화 도구 사용 가이드 (docs/developer-env-guide.md)**](./docs/developer-env-guide.md) - Spotless 포맷 자동 가공 명령어, Swagger UI, 로컬 테스트용 더미 데이터 설정
* [**Flyway 데이터베이스 마이그레이션 가이드 (docs/flyway-guide.md)**](./docs/flyway-guide.md) - Flyway 스크립트 작성 규칙, JPA Buddy 플러그인을 활용한 마이그레이션 방법
* [**로컬 개발 트러블슈팅 가이드 (docs/troubleshooting.md)**](./docs/troubleshooting.md) - Flyway 해시 충돌, 포트 선점, 데이터베이스 권한 에러 해결 가이드

### 📋 개발자 성장을 돕는 작성 양식 및 템플릿
* [**기능 개발 이슈 템플릿 (.github/ISSUE_TEMPLATE/feature_request.md)**](./.github/ISSUE_TEMPLATE/feature_request.md) - 신규 기능 등록 양식 🚀
* [**버그 조치 이슈 템플릿 (.github/ISSUE_TEMPLATE/bug_report.md)**](./.github/ISSUE_TEMPLATE/bug_report.md) - 버그 보고 및 조치 양식 🐛
* [**아키텍처 결정 레코드 (ADR) 템플릿 (docs/templates/adr-template.md)**](./docs/templates/adr-template.md) - 기술 의사결정의 배경, 대안, 장단점을 논리적으로 기록해 시니어 개발자로 발돋움하게 돕는 양식 🏛️
* [**트러블슈팅 및 장애 회고 템플릿 (docs/templates/troubleshooting-log-template.md)**](./docs/templates/troubleshooting-log-template.md) - 에러 발생 시 현상, 원인분석(5 Whys), 재발 방지책을 회고하여 성장을 가속화하는 일지 양식 💡

### 📝 기존 개발 기능 요구사항 명세서 및 설계서 (Reference)
* [**회원 및 인증 설계서 (docs/design/member-auth-design.md)**](./docs/design/member-auth-design.md) - 회원가입, 중복 가입 방지 예외, 암호화 저장, 상세 조회 API 명세 및 ERD
* [**차량 등록 및 조회 설계서 (docs/design/vehicle-design.md)**](./docs/design/vehicle-design.md) - 차량 사양 유효 검증, 소유주 정보 매핑, 가용성 조회 API 명세 및 ERD
* [**배송 요청 및 단건 조회 설계서 (docs/design/delivery-request-design.md)**](./docs/design/delivery-request-design.md) - 출발/목적지, 화물 무게 및 배송 상태 API 명세 및 ERD
* [**배송 매칭 설계서 (docs/design/matching-design.md)**](./docs/design/matching-design.md) - 수동 배송 매칭, 상태 수정/삭제에 따른 실시간 리소스 데이터 동기화 API 명세 및 ERD
* [**포인트 지갑 및 결제 설계서 (docs/design/point-wallet-design.md)**](./docs/design/point-wallet-design.md) - 지갑 개설, 충전, 차감 검증 및 잔액 에러 처리 API 명세 및 ERD
* [**실시간 위치 추적 설계서 (docs/design/tracking-design.md)**](./docs/design/tracking-design.md) - WebSocket(STOMP) 기반 실시간 위치 브로드캐스트, CONNECT/SUBSCRIBE 인증·인가 흐름 및 메시지 명세
* [**물품 카테고리 목록 조회 설계서 (docs/design/category-design.md)**](./docs/design/category-design.md) - 물품 카테고리 12종 마이그레이션 시딩 및 목록 조회 API 명세
* [**이미지 업로드 설계서 (docs/design/image-upload-design.md)**](./docs/design/image-upload-design.md) - Multipart 이미지 업로드, 확장자·크기 검증 및 정적 리소스 서빙 API 명세
* [**알림 목록 조회/읽음 처리 설계서 (docs/design/notification-design.md)**](./docs/design/notification-design.md) - 로그인 사용자 기준 알림 페이징 조회, 카테고리 필터링 및 읽음 처리 API 명세
