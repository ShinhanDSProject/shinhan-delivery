# 🎤 개발자 프로젝트 발표 PPT 표준 템플릿 & AI 프롬프트 가이드

> **문서 버전:** v2.1 (Easy Fill-in & Topic Presets)  
> **최종 수정일:** 2026-08-18  
> **대상:** 신한 딜리버리(shinhan-delivery) 프로젝트 참여 개발자 전체  
> **목적:** 모든 팀원이 막연함 없이 1분 만에 괄호만 채우거나(Fill-in-the-blanks), 주제별 프리셋(Presets) 및 1줄 AI 명령어("내 PR 읽어서 PPT 만들어줘")를 활용해 동일한 고품질 PPT 텍스트를 손쉽게 완성할 수 있도록 지원합니다.

---

## ⚡ 1초 완성 팁: AI에게 "내 PR 읽어서 PPT 만들어줘" (Single Prompt Magic)

AI 도구(Gemini, Claude, Antigravity 등)에 본인이 작성한 **Git 커밋/PR 번호**와 함께 아래 1줄 명령어만 전송하면, AI가 저장소 코드를 직접 읽어 PPT 양식으로 100% 채워줍니다!

```markdown
AI야, 내 PR #336 (또는 git log 최근 커밋) 내용을 직접 읽어보고, 
docs/presentation/개발자-발표-PPT-표준-템플릿-가이드.md 의 [Master AI Prompt] 양식에 맞춰서 
5-Slide PPT 발표 콘텐츠를 자동으로 작성해줘!
```

---

## 📋 괄호만 채우는 빈칸 채우기 양식 (Fill-in-the-Blanks Form)

팀원들은 아래 텍스트를 복사하여 `[ ]` 괄호 속 내용만 간단히 채운 뒤 AI에 전송하면 됩니다.

```markdown
[System Instruction: PPT 발표 자료 작성 전문가]
당신은 신한 딜리버리 프로젝트의 수석 엔지니어입니다.
아래 전달된 정보를 바탕으로 5-Slide PPT 콘텐츠를 마크다운 서식으로 작성해주세요.

[Slide 1] 표지 & 담당 주제 요약 (Executive Summary)
- 제목: [발표 주제 입력] 기술 성과 발표
- 발표자: [이름] / [역할]
- 3대 핵심 성과:
  • [Before → After 수치 성과 1]
  • [Before → After 수치 성과 2]
  • [Before → After 수치 성과 3]

[Slide 2] 추진 배경 & 주요 목표
- 비교표:
  | 기존 문제점 (Before) | 개선 목표 & 기대 가치 (After) |
  |---|---|
  | [문제점 1] | [개선 목표 1] |
  | [문제점 2] | [개선 목표 2] |
  | [문제점 3] | [개선 목표 3] |
- 핵심 기대 가치: "[1줄 정의]"

[Slide 3] 시스템 구조 & 설계 아키텍처
- 핵심 설명 3줄
- Mermaid 구조도 (flowchart 또는 sequenceDiagram) 포함
- 주요 적용 클래스/패턴 2가지: [클래스/패턴 1], [클래스/패턴 2]

[Slide 4] 핵심 기술 구현 & 트러블슈팅
- P-R-S-I 표 (Problem / Root Cause / Solution / Impact)
- 핵심 소스코드 스니펫 3~5줄 포함

[Slide 5] 회고 & Lessons Learned
- KPT 표 (Keep 2개 / Problem 2개 / Try 2개)
- 💡 Key Lesson: "[1줄 배움]"

---

[개발 내용 정보]:
- 발표 주제: [예: 주요 웹 화면 FOUC 제거 및 WebAuthHelper 구축]
- 주요 담당 파트: [예: Web Controller & Thymeleaf SSR]
- 세부 구현 내용:
  1. [구현 내용 1]
  2. [구현 내용 2]
  3. [해결한 에러/이슈]
```

---

## 🎯 주제별 바로 쓰는 4대 프리셋 (Topic Presets)

본인이 발표할 주제에 해당하는 프리셋의 `[내용 입력]` 부분만 간단히 채워서 활용하세요.

### 🎨 프리셋 A. UI & 디자인 시스템 발표용
```markdown
- 발표 주제: 공통 UI 디자인 시스템 구축 및 뷰 성능 최적화
- 주요 담당 파트: static/css/design-system.css, Thymeleaf SSR
- 세부 구현 내용:
  1. design-system.css 3-Tier 모듈화 (공통 셸, 탭, 뒤로가기 버튼)
  2. 332줄 중복 CSS 제거 및 static 정적 파일 캐싱 적용
  3. 초기 진입 시 FOUC(화면 깜빡임) 0ms 즉시 렌더링 달성
```

### 🔐 프리셋 B. 인증 & 보안 아키텍처 발표용
```markdown
- 발표 주제: 무상태 JWT 인증 및 WebAuthHelper 캡슐화
- 주요 담당 파트: Spring Security, WebAuthHelper
- 세부 구현 내용:
  1. SecurityContextHolder 널 체크 보일러플레이트 제거를 위한 WebAuthHelper 신설
  2. 컨트롤러 1줄 선언적 인증 정보 추출 및 Null-Safety 확보
  3. ArchUnit 단방향 계층 규칙(Controller -> Service -> Repository) 100% 준수
```

### 🤖 프리셋 C. AI 파이프라인 & 개발 환경 발표용
```markdown
- 발표 주제: LangGraph 기반 이슈 기획 자동화 파이프라인 구축
- 주요 담당 파트: Python LangGraph, GraphRAG, Test Harness
- 세부 구현 내용:
  1. /plan <이슈번호> 입력 시 이슈 분석부터 implementation_plan.md 자동 생성
  2. GraphRAG 지식 탐색 및 ./scripts/verify.sh 자가 치유 피드백 루프 구축
  3. 기획 오버헤드 감축 및 결함 0개 자동 검증 파이프라인 완성
```

### 📦 프리셋 D. 비즈니스 기능 개발 발표용 (배송/결제/매칭)
```markdown
- 발표 주제: 배송 매칭 및 포인트 지갑 비관적 락 동시성 제어
- 주요 담당 파트: DeliveryService, PaymentService, MariaDB
- 세부 구현 내용:
  1. 동시 배송 요청 시 PESSIMISTIC_WRITE 락 적용으로 잔액/매칭 정확성 사수
  2. static factory method .from() 및 Lombok 100% 가이드 준수
  3. 46개 전체 테스트 및 JaCoCo 60%+ 커버리지 패스
```

---

## 🎨 PPT 자동 생성 및 변환 팁

1. **AI 생성**: 위 템플릿에 괄호 몇 개만 채워 AI에 전송하면 100% 동일한 포맷의 5-Slide 마크다운 텍스트가 5초 만에 작성됩니다.
2. **PPT 변환**: 완성된 마크다운 텍스트를 [Gamma.app](https://gamma.app/) 또는 [Marp](https://marp.app/)에 그대로 붙여넣으면 고품질 발표 PPT로 완성됩니다.
