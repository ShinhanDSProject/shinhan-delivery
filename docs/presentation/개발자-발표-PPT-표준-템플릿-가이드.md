# 🎤 개발자 프로젝트 발표 PPT 표준 템플릿 & AI 프롬프트 가이드

> **문서 버전:** v2.0 (AI Promptable Spec)  
> **최종 수정일:** 2026-08-18  
> **대상:** 신한 딜리버리(shinhan-delivery) 프로젝트 참여 개발자 전체  
> **목적:** Gemini, Claude Code, Antigravity 등 AI 도구에 프롬프트를 전달했을 때 모든 개발자가 100% 일치하는 정교한 규격의 5-Slide PPT 콘텐츠를 자동 생성할 수 있도록 **AI 프롬프트 표준 규격**을 정의합니다.

---

## 🤖 AI 전용 PPT 프롬프트 템플릿 (Master AI Prompt)

팀원들은 아래 박스 안의 프롬프트 문구를 **그대로 복사(Copy & Paste)**한 뒤, 하단의 **`[발표 주제 및 본인이 개발한 내용]`** 부분만 본인의 작업 내용으로 채워서 AI(Gemini, Claude, ChatGPT 등)에 전송합니다.

```markdown
[System Instruction: PPT 발표 자료 작성 전문가]
당신은 신한 딜리버리(shinhan-delivery) 프로젝트의 수석 소프트웨어 엔지니어입니다.
아래 제공되는 [개발 내용 및 발표 주제]를 바탕으로, 프로젝트 발표용 5-Slide PPT 콘텐츠를 작성해주세요.

반드시 아래에 정의된 **[5대 슬라이드 표준 서식]**을 100% 엄격하게 준수하여 작성해야 합니다.
모든 슬라이드는 텍스트 서술을 최소화하고, 수치화된 지표, 비교표, Mermaid 다이어그램, P-R-S-I 표, KPT 회고 구조로 출력하세요.

---

### 📌 [Slide 1] 표지 & 담당 주제 요약 (Executive Summary)
- 슬라이드 제목: [주제명] 기술 성과 발표
- 발표자: [발표자 이름] / [담당 역할 및 도메인]
- 3대 핵심 성과 (Before → After 수치 기반 3가지 불렛):
  • 성과 1: [구체적 Before → After 수치 지표]
  • 성과 2: [구체적 Before → After 수치 지표]
  • 성과 3: [구체적 Before → After 수치 지표]

### 💡 [Slide 2] 추진 배경 & 주요 요구사항 (Background & Objectives)
- 슬라이드 제목: 추진 배경 & 주요 목표
- 배경 및 목표 비교표 (마크다운 표 형식):
  | 기존 문제점 (Before) | 개선 목표 & 기대 가치 (After) |
  |---|---|
  | [문제점 1] | [개선 목표 1] |
  | [문제점 2] | [개선 목표 2] |
  | [문제점 3] | [개선 목표 3] |
- 핵심 기대 가치 요약: "[1줄로 정의하는 엔지니어링 가치]"

### 🏗️ [Slide 3] 주요 시스템 설계 & 아키텍처 (Architecture & Structure)
- 슬라이드 제목: 시스템 구조 & 설계 아키텍처
- 시스템 핵심 설명 (3줄 불렛):
  • [설계 원칙 1]
  • [설계 원칙 2]
  • [설계 원칙 3]
- 시스템 다이어그램 (Mermaid flowchart 또는 sequenceDiagram 코드 블록 필수 포함)
- 적용된 주요 클래스/패턴 2가지: [패턴/클래스명 1], [패턴/클래스명 2]

### 🛠️ [Slide 4] 주제별 핵심 구현 & 기술 딥다이브 (Deep-Dive & Problem Solving)
- 슬라이드 제목: 핵심 기술 구현 & 트러블슈팅
- P-R-S-I 4단계 분석표 (마크다운 표 형식):
  | 구분 | 내용 |
  |---|---|
  | 1. 문제 상황 (Problem) | [발생한 핵심 기술적 문제] |
  | 2. 원인 진단 (Root Cause) | [문제의 구조적/기술적 원인] |
  | 3. 해결 방안 (Solution) | [적용한 핵심 기술 솔루션] |
  | 4. 정량 성과 (Impact) | [수치화된 결과 및 검증 지표] |
- 핵심 코드 스니펫 (3~5줄 이내의 핵심 소스코드 블록):
  ```java/css/javascript
  // 핵심 코드 3~5줄
  ```

### 🚀 [Slide 5] 회고 & Lessons Learned (Retrospective)
- 슬라이드 제목: 회고 & 향후 발전 방향
- KPT 프레임워크 회고표 (마크다운 표 형식):
  | 구분 | 항목 | 내용 |
  |---|---|---|
  | **Keep** | 잘한 점 | • [잘한 점 1]<br>• [잘한 점 2] |
  | **Problem** | 아쉬운 점 | • [아쉬운 점 1]<br>• [아쉬운 점 2] |
  | **Try** | 향후 시도 | • [향후 시도 1]<br>• [향후 시도 2] |
- 💡 Key Lesson: "[개발 과정을 통해 얻은 1줄 기술적 깨달음]"

---

[개발 내용 및 발표 주제]:
- 발표 주제: [여기에 본인의 발표 주제 입력]
- 담당 도메인/모듈: [여기에 모듈/도메인 입력]
- 구현된 주요 기능 및 상세 내용:
  1. [구현 내용 1]
  2. [구현 내용 2]
  3. [기술적 문제 해결 사례 및 지표]
```

---

## 📝 AI 입력 및 출력 생성 예시 (Example Output)

위 프롬프트에 개발 내용(예: "주요 웹 화면 FOUC 제거 및 WebAuthHelper 구축")을 전달하면 AI가 아래와 같이 **100% 동일한 규격의 마크다운 PPT 콘텐츠**를 출력합니다.

<details>
<summary><b>🔍 AI가 생성하는 표준 PPT 마크다운 출력 예시 (클릭하여 펼치기)</b></summary>

### 📌 [Slide 1] 주요 웹 화면 SSR 사전 바인딩 & FOUC 개선 성과 발표
- **발표자:** 홍길동 / 백엔드 & 프론트엔드 최적화 담당
- **3대 핵심 성과:**
  • 초기 화면 로딩 FOUC 지연: `0.5s` → `0ms` (100% 감축)
  • 컨트롤러 인증 널 체크 코드: `15+줄` → `1줄` 선언적 통합 (`WebAuthHelper`)
  • 중복 CSS 코드 감축: `332줄` 제거 및 3-Tier 모듈화 완료

### 💡 [Slide 2] 추진 배경 & 주요 목표
- **슬라이드 제목:** 추진 배경 & 주요 목표
- **배경 및 목표 비교표:**
  | 기존 문제점 (Before) | 개선 목표 & 기대 가치 (After) |
  |---|---|
  | 초기 HTML 미바인딩으로 0.5초 화면 빈 칸 현상(FOUC) 발생 | 서버 모델 사전 바인딩으로 접속 순간 0ms 즉시 DOM 렌더링 |
  | Web Controller 라우트마다 인증 널 체크 복사/붙여넣기 | `WebAuthHelper` 캡슐화로 선언적 1줄 인증 추출 |
  | CSS 규칙이 5개 이상 파일에 중복 작성되어 유지보수 난항 | `design-system.css` 3-Tier 모듈화로 중복 332줄 제거 |
- **핵심 기대 가치:** "FOUC 없는 0ms 즉시 렌더링 UX 및 캡슐화를 통한 생산성 극대화"

### 🏗️ [Slide 3] 시스템 구조 & 설계 아키텍처
- **슬라이드 제목:** 시스템 구조 & 설계 아키텍처
- **시스템 핵심 설명:**
  • **1단계 (SSR)**: Spring Web Controller에서 필수 초기 데이터를 `Model`에 바인딩하여 0ms 렌더링
  • **2단계 (CSR)**: 진입 후 실시간 WebSocket 알림 및 탭 전환은 Vanilla JS가 담당 (하이브리드 구조)
  • **ArchUnit 준수**: `Controller → Service → Repository` 단방향 의존성 100% 사수
- **시스템 다이어그램:**
  ```mermaid
  flowchart TD
      User([사용자 브라우저]) --> WebCtrl[Web Controller 라우트]
      WebCtrl --> WebAuth[WebAuthHelper - memberId 추출]
      WebAuth --> Service[도메인 Service 사전 데이터 조회]
      Service --> Model[Thymeleaf Model 바인딩]
      Model --> SSRHtml[완성된 SSR HTML 0ms 전송]
  ```
- **적용된 주요 클래스:** `WebAuthHelper`, `HomeWebController`

### 🛠️ [Slide 4] 핵심 기술 구현 & 트러블슈팅
- **슬라이드 제목:** 핵심 기술 구현 & 트러블슈팅
- **P-R-S-I 4단계 분석표:**
  | 구분 | 내용 |
  |---|---|
  | 1. 문제 상황 (Problem) | Web Controller에서 Repository를 직접 호출하여 ArchUnit 검증 실패 |
  | 2. 원인 진단 (Root Cause) | 뷰 컨트롤러라는 이유로 계층 구조 규칙(Controller -> Service -> Repository) 미준수 |
  | 3. 해결 방안 (Solution) | `PaymentService`에 읽기 전용 메서드를 신설하고 컨트롤러는 무조건 Service 경유 |
  | 4. 정량 성과 (Impact) | ArchUnit 계층 아키텍처 및 46개 전체 테스트 100% 패스 |
- **핵심 코드 스니펫:**
  ```java
  @GetMapping("/home")
  public String home(Model model) {
    webAuthHelper.getCurrentMemberId().ifPresent(memberId -> {
      model.addAttribute("activeDeliveries", deliveryService.getActiveDeliveries(memberId));
    });
    return "home";
  }
  ```

### 🚀 [Slide 5] 회고 & 향후 발전 방향
- **슬라이드 제목:** 회고 & 향후 발전 방향
- **KPT 프레임워크 회고표:**
  | 구분 | 항목 | 내용 |
  |---|---|---|
  | **Keep** | 잘한 점 | • 하이브리드 SSR+CSR 설계로 0ms FOUC 달성<br>• ArchUnit 하네스 검증을 통한 결함 0개 사수 |
  | **Problem** | 아쉬운 점 | • 초기 뷰 컨트롤러 설계 시 데이터 바인딩 고려 부족<br>• 예외 처리 범위의 추후 확장 필요성 |
  | **Try** | 향후 시도 | • Redis Caching 레이어 도입을 통한 SSR 모델 조회 최적화<br>• 공통 유틸리티 모듈 추가 확장 |
- **💡 Key Lesson:** "추측이 아닌 테스트 하네스 검증과 선언적 유틸리티 설계를 통한 소프트웨어 품질 사수"

</details>

---

## 🎨 PPT 디자인 변환 팁 (PPT Tools & AI Export)

1. **Gamma / Marp / ChatPPT 활용:**
   - 위 생성된 마크다운 텍스트를 [Marp](https://marp.app/) 또는 [Gamma.app](https://gamma.app/)에 그대로 붙여넣으면 즉시 5장의 고품질 PPT 슬라이드로 자동 변환됩니다.
2. **수치 표기 강조 규칙:**
   - 성과 표기 시 반드시 **`Before → After (개선율)`** 형태의 굵은 글씨(Bold)를 유지합니다.
