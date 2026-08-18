# 🎤 개발자 프로젝트 발표 PPT 표준 템플릿 가이드

> **문서 버전:** v1.0  
> **최종 수정일:** 2026-08-18  
> **대상:** 신한 딜리버리(shinhan-delivery) 프로젝트 참여 개발자 전체  
> **목적:** 팀원별 개발 성과와 기술적 깊이를 통일된 규격으로 명확하게 전달하고, 리뷰어 및 청중이 5분 내에 핵심 가치를 이해할 수 있도록 PPT 슬라이드 템플릿을 표준화합니다.

---

## 📌 PPT 발표 템플릿 핵심 요약 (Standard Slide Deck)

발표 덱은 **총 6개 슬라이드(5분~10분 발표 분량)**로 구성되며, 개발자가 "무엇을 만들었는가"를 넘어 **"어떤 기술적 문제를 어떻게 깔끔하게 해결했는가"**를 보여주는 데 초점을 맞춥니다.

```
[Slide 1] 📌 표지 & 담당 도메인 요약 (Executive Summary)
   ↓
[Slide 2] 💡 비즈니스 요구사항 & 유저 플로우 (Feature & UI Flow)
   ↓
[Slide 3] 🏗️ 시스템 아키텍처 & 설계 구조 (Architecture & Sequence)
   ↓
[Slide 4] 🛠️ 핵심 트러블슈팅 (Problem → Root Cause → Solution → Impact)
   ↓
[Slide 5] 🛡️ 품질 검증 & 테스트 하네스 지표 (Verify Harness & JaCoCo)
   ↓
[Slide 6] 🚀 회고 (Keep / Problem / Try) & 기술적 Lessons Learned
```

---

## 📑 슬라이드별 상세 작성 양식 (Slide-by-Slide Template Specification)

### Slide 1. 📌 표지 & 담당 도메인 요약 (Executive Summary)

* **슬라이드 제목:** `[도메인명/기능명] 개발 성과 발표`
* **발표자 정보:** 소속 / 이름 / 담당 역할
* **핵심 요약 구성 요소:**
  1. **담당 도메인:** (예: 예약·매칭 / 포인트 결제 / 배송 이력 및 알림 / static 추출 및 뷰 최적화)
  2. **주요 성과 지표 (Before vs After):**
     - 예: 초기 화면 로딩 FOUC 지연 `0.5s` → `0ms` (100% 감축)
     - 예: 중복 CSS 감축 `332줄` 제거, 공통 JS 유틸리티 모듈 3종 신설
     - 예: 단위/통합 테스트 `46개` 100% 그린 빌드 패스

---

### Slide 2. 💡 비즈니스 요구사항 & 유저 플로우 (Feature & UI Flow)

* **슬라이드 제목:** `주요 기능 명세 & 사용자 여정 (User Journey)`
* **콘텐츠 구성:**
  * **좌측 (비즈니스 요구사항 3가지):**
    - [ ] 유스케이스 1: (예: 사용자 배송 신청 및 픽업 가이드 입력)
    - [ ] 유스케이스 2: (예: 배송원 매칭 및 실시간 상태 변경 알림)
    - [ ] 유스케이스 3: (예: 배송 완료 후 자동 포인트 정산)
  * **우측 (UI 화면 스크린샷 또는 Mermaid 유저 플로우):**
    - 실제 구동되는 웹 화면 이미지 첨부 또는 프론트-백엔드 렌더링 순서도 명시

---

### Slide 3. 🏗️ 시스템 아키텍처 & 설계 구조 (Architecture & Sequence)

* **슬라이드 제목:** `기술 아키텍처 & 클래스/데이터 흐름`
* **콘텐츠 구성:**
  * **단방향 의존성 레이어 명시:** `Web Controller → Service → Repository → Entity`
  * **핵심 다이어그램 (Mermaid / 데이터 흐름도):**
    ```mermaid
    sequenceDiagram
        autonumber
        actor User as 사용자 (Web Browser)
        participant Controller as Web/API Controller
        participant Helper as WebAuthHelper / Service
        participant Repo as JPA Repository / DB

        User->>Controller: GET /delivery-history (SSR 요청)
        Controller->>Helper: memberId extraction (Null-Safety)
        Controller->>Service: getMyDeliveryRequests(memberId, status, pageable)
        Service->>Repo: findByMemberIdOrderByCreatedAtDesc()
        Repo-->>Service: Page<DeliveryRequest> Entity
        Service-->>Controller: List<DeliveryResponse> DTO
        Controller-->>User: Thymeleaf Model SSR HTML 0ms 전송
    ```
  * **주요 설계 적용 패턴:** (예: 정적 팩토리 메서드 `.from()`, Lombok 100% 적용, DTO Builder 활용 등)

---

### Slide 4. 🛠️ 핵심 트러블슈팅 (Troubleshooting)

* **슬라이드 제목:** `기술적 문제 해결 (Problem-Solving Deep Dive)`
* **콘텐츠 구성 (P-R-S-I 4단계 구성표):**

| 구분 | 내용 |
|---|---|
| **1. 문제 상황 (Problem)** | 진입 시 화면이 0.5초간 비어있거나 FOUC 지연 발생 / 컨트롤러마다 널 체크 중복 |
| **2. 원인 분석 (Root Cause)** | 초기 데이터를 서버 `Model`에 사전 바인딩하지 않고 클라이언트 JS `fetch()`에만 100% 의존함 |
| **3. 해결 방안 (Solution)** | Spring Web Controller SSR 사전 바인딩 (`th:each`) + `WebAuthHelper` 신설로 1줄 선언적 처리 |
| **4. 검증 성과 (Impact)** | 초기 접속 지연 `0ms` 달성, ArchUnit 레이어 규칙 통과, 네트워크 추가 RTT 제거 |

---

### Slide 5. 🛡️ 품질 검증 & 테스트 하네스 지표 (Quality Gate & Test Harness)

* **슬라이드 제목:** `품질 검증 & Test Harness 성과`
* **콘텐츠 구성:**
  * **`./scripts/verify.sh` 5대 게이트 통과 지표:**
    1. **Flyway 마이그레이션**: DDL 무중단 규격 100% 준수
    2. **UI Design System Linting**: `design-system.css` 100% 연동
    3. **Spotless & Checkstyle**: 정적 분석 및 코드 포맷팅 0 이슈
    4. **ArchUnit 아키텍처**: 단방향 의존성 및 Layered 규칙 사수
    5. **JaCoCo 테스트 커버리지**: 본인 담당 도메인 커버리지 `XX%` 달성 (기준: 60% 이상)

---

### Slide 6. 🚀 회고 & Lessons Learned (Retrospective)

* **슬라이드 제목:** `개발 회고 & 향후 발전 방향`
* **콘텐츠 구성 (KPT 프레임워크):**
  * **Keep (잘한 점):** 테스트 하네스 검증을 통한 결함 0개 사수, 3단계 CSS/JS 모듈화로 유지보수성 향상
  * **Problem (아쉬운 점):** 초기 비동기 파이프라인 구성 시 오버헤드 경험, 예외 핸들링 범위 추후 보완 필요
  * **Try (향후 시도):** Redis 캐싱 레이어 도입을 통한 SSR 조회 성능 추가 향상
  * **💡 Key Lesson:** "추측 기반 수정이 아닌 실증 데이터와 테스트 하네스 기반 자가 치유 피드백 루프의 중요성"

---

## 🎨 발표 자료(PPT) 디자인 & 작성 꿀팁 (Presentation Guidelines)

1. **3초 훑어보기 규칙 (Scanning Rule):**
   - 슬라이드당 텍스트 서술을 줄이고, **볼드(Bold) 강조, 요약표, 다이어그램, 코드 스니펫** 위주로 구상합니다.
2. **코드 캡처 가독성:**
   - 코드를 슬라이드에 넣을 때는 배경 다크 모드 테마(VS Code / IntelliJ 폰트 크기 18pt 이상)로 핵심 3~5줄만 캡처하여 강조 상자(Highlight)를 칩니다.
3. **실증 데이터 강조:**
   - "성능이 좋아졌습니다" 대신 **"FOUC 지연시간 0.5s -> 0ms (100% 감축)", "중복 CSS 332줄 제거"**처럼 숫자로 표현합니다.

---

## 📁 템플릿 마크다운 원본 공유

팀원들은 위 서칙을 기본 가이드라인으로 활용하여 presentation 자료를 작성하고, 본 문서(`docs/presentation/개발자-발표-PPT-표준-템플릿-가이드.md`)를 단일 원본(SSOT)으로 참조합니다.
