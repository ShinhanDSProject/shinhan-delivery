# 🎤 개발자 프로젝트 발표 PPT 표준 템플릿 가이드

> **문서 버전:** v1.1  
> **최종 수정일:** 2026-08-18  
> **대상:** 신한 딜리버리(shinhan-delivery) 프로젝트 참여 개발자 전체  
> **목적:** 팀원별 발표 주제(기능 개발, UI/디자인 시스템, 인증/보안, AI 개발 환경 등)에 상관없이 5분~10분 내에 자신의 기술적 성과와 설계를 유연하고 명확하게 전달할 수 있도록 PPT 슬라이드 템플릿을 표준화합니다.

---

## 📌 PPT 발표 템플릿 핵심 요약 (5-Slide Universal Deck)

발표 덱은 **총 5개 슬라이드(5분~10분 발표 분량)**로 구성되며, 단순 백엔드 기능 개발뿐만 아니라 **디자인 시스템, 인증 체계, AI 파이프라인/개발 환경 구축** 등 다양한 주제를 자유롭게 담을 수 있는 유연한 구조로 설계되었습니다.

```
[Slide 1] 📌 표지 & 담당 주제 요약 (Executive Summary)
   ↓
[Slide 2] 💡 추진 배경 & 주요 요구사항 (Background & Objectives)
   ↓
[Slide 3] 🏗️ 주요 시스템 설계 & 아키텍처 (Architecture & Structure)
   ↓
[Slide 4] 🛠️ 주제별 핵심 구현 & 기술 딥다이브 (Deep-Dive & Problem Solving)
   ↓
[Slide 5] 🚀 회고 (Keep / Problem / Try) & 기술적 Lessons Learned
```

---

## 📑 슬라이드별 상세 작성 양식 (Slide-by-Slide Template Specification)

### Slide 1. 📌 표지 & 담당 주제 요약 (Executive Summary)

* **슬라이드 제목:** `[발표 주제/도메인명] 성과 발표`
* **발표자 정보:** 소속 / 이름 / 담당 역할
* **핵심 요약 구성 요소:**
  1. **발표 주제:** (예: 공통 UI 디자인 시스템 구축 / 무상태 JWT 인증 체계 / AI LangGraph 기획 파이프라인 / 배송 매칭 기능)
  2. **핵심 성과 요약 (3대 지표/포인트):**
     - 예 (UI/디자인 시스템): 공통 디자인 토큰 적용으로 332줄 중복 CSS 감축 및 FOUC 0ms 달성
     - 예 (인증/보안): 무상태 JWT + `WebAuthHelper` 도입으로 컨트롤러 보일러플레이트 제거
     - 예 (AI 파이프라인): LangGraph 기반 기획 및 이슈 자동화 그래프 구축

---

### Slide 2. 💡 추진 배경 & 주요 요구사항 (Background & Objectives)

* **슬라이드 제목:** `추진 배경 & 주요 목표 (Objectives)`
* **콘텐츠 구성:**
  * **좌측 (개선 전 문제점 또는 도입 필요성):**
    - [ ] 문제점/필요성 1: (예: 화면마다 인라인 CSS/JS가 중복 작성되어 유지보수 난항)
    - [ ] 문제점/필요성 2: (예: 인증 정보 추출 코드가 컨트롤러마다 무분별하게 복사됨)
    - [ ] 문제점/필요성 3: (예: 이슈 기획 및 문서화 작업의 수동 오버헤드)
  * **우측 (핵심 목표 및 기대 효과):**
    - 목표 1, 2, 3 및 기대되는 비즈니스/엔지니어링 가치 시각화

---

### Slide 3. 🏗️ 주요 시스템 설계 & 아키텍처 (Architecture & Structure)

* **슬라이드 제목:** `시스템 구조 & 설계 아키텍처`
* **콘텐츠 구성 (주제에 맞는 다이어그램 활용):**
  * **기능/인증 발표 시**: 계층형 구조 (`Controller → Service → Repository`) & 시퀀스 다이어그램
  * **UI/디자인 시스템 발표 시**: 3단계 CSS 모듈화 구조도 (`design-system.css → components.html → pages/*.css`)
  * **AI/개발 환경 발표 시**: LangGraph 노드 파이프라인 흐름도 (`IssueAnalyzer → PlanGenerator → HarnessVerifier`)
  ```mermaid
  flowchart LR
      A[입력/요청] --> B[핵심 엔진/모듈]
      B --> C[공통 유틸리티/토큰]
      C --> D[최종 결과물/화면]
  ```

---

### Slide 4. 🛠️ 주제별 핵심 구현 & 기술 딥다이브 (Deep-Dive & Problem Solving)

* **슬라이드 제목:** `핵심 기술 구현 & 트러블슈팅`
* **콘텐츠 구성 (P-R-S-I 표 또는 핵심 코드 스니펫):**

| 구분 | 내용 |
|---|---|
| **1. 주요 문제/도전과제 (Problem/Challenge)** | 개발 중 발생한 핵심 문제 또는 기술적 도전 과제 |
| **2. 원인 진단 (Root Cause/Reason)** | 문제의 근본 원인 또는 기술 도입 이유 |
| **3. 해결/구현 내용 (Solution/Implementation)** | 핵심 기술 적용 내용 (코드 스니펫 또는 구조 설명) |
| **4. 성과 및 정량 지표 (Impact)** | 수치화된 개선 결과 및 효과 |

---

### Slide 5. 🚀 회고 & Lessons Learned (Retrospective)

* **슬라이드 제목:** `회고 & 향후 발전 방향`
* **콘텐츠 구성 (KPT 프레임워크):**
  * **Keep (잘한 점):** 성공적이었던 설계/구현 패턴 및 협업 방식
  * **Problem (아쉬운 점):** 개발 중 경험한 한계점 또는 보완이 필요한 부분
  * **Try (향후 시도):** 다음 단계에서 고도화하고 싶은 기술적 시도
  * **💡 Key Lesson:** 이번 개발 과정을 통해 얻은 기술적 성찰 및 깨달음

---

## 🎯 주제별 발표 구성 예시 (Topic Examples)

| 발표 주제 | Slide 2 (배경) | Slide 3 (아키텍처) | Slide 4 (기술 딥다이브) |
|---|---|---|---|
| **🎨 UI / 디자인 시스템** | FOUC 지연 및 인라인 CSS 파편화 문제 | 3-Tier CSS 모듈화 구조 | `design-system.css` 토큰화 및 332줄 중복 제거 |
| **🔐 인증 / 보안 아키텍처** | 컨트롤러 인증 보일러플레이트 중복 | JWT 무상태 인증 & Security Filter | `WebAuthHelper` 1줄 선언적 처리 및 Null-Safety |
| **🤖 AI 개발 환경 (LangGraph)** | 기획/문서화 작업 수동 오버헤드 | LangGraph Stateful Multi-Node 구조 | 이슈 자동 기획 파이프라인 및 자가 치유 피드백 |
| **📦 비즈니스 기능 (매칭/결제)** | 배송 매칭 및 포인트 결제 요구사항 | `Controller → Service → Repo` 흐름 | 비관적 락(Pessimistic Lock) 동시성 제어 |

---

## 🎨 발표 자료(PPT) 작성 꿀팁 (Presentation Guidelines)

1. **3초 훑어보기 규칙 (Scanning Rule):**
   - 슬라이드당 텍스트 서술을 줄이고, **요약표, 볼드체, 스크린샷, 다이어그램** 위주로 구성합니다.
2. **수치 중심 성과 강조:**
   - "성능이 좋아졌습니다" 대신 **"초기 로딩 지연 0.5s -> 0ms 감축", "중복 CSS 332줄 제거"**처럼 숫자로 표현합니다.
3. **코드 가독성:**
   - 코드를 슬라이드에 넣을 때는 배경 다크 모드 테마(VS Code / IntelliJ 폰트 크기 18pt 이상)로 핵심 3~5줄만 캡처하여 강조 상자(Highlight)를 칩니다.
