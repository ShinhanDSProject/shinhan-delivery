# 🗺️ 신한 배달 전체 프로젝트 지식 그래프 인덱스 (Project Knowledge Graph)

> **프로젝트의 모든 문서(Docs), 아키텍처 계층, 하네스 검증기 및 DB 스키마 간의 종속성과 연관 관계를 시각화한 지식 네트워크 지도**

---

## 📑 목차
- [1. 전체 지식 그래프 시각화 (Overview Graph)](#1-전체-지식-그래프-시각화-overview-graph)
- [2. 영역별 노드 & 문서 연결 가이드](#2-영역별-노드--문서-연결-가이드)
- [3. 개발자 & AI 에이전트를 위한 컨텍스트 탐색법](#3-개발자--ai-에이전트를-위한-컨텍스트-탐색법)

---

## 1. 전체 지식 그래프 시각화 (Overview Graph)

```mermaid
graph TD
    subgraph Governance ["🏛️ 거버넌스 & 규칙"]
        Rule_Agents["AGENTS.md (AI 수칙)"]
        Rule_Convention["code-convention.md (코딩규약)"]
        Rule_Claude["CLAUDE.md (실행지침)"]
    end

    subgraph Architecture ["🏗️ 백엔드 계층 구조"]
        Controller["Controller 계층"]
        Service["Service 계층"]
        Repository["Repository 계층"]
        Entity["JPA Entity"]
        DTO["DTO (Req/Res)"]
    end

    subgraph TestHarness ["🛡️ 5단계 품질 하네스"]
        VerifyScript["./scripts/verify.sh"]
        FlywayLint["Flyway 파일명/DDL 린터"]
        DesignLint["lint-design-system.sh"]
        Spotless["Spotless 포맷터"]
        ArchUnit["ArchUnit 계층검증"]
    end

    subgraph DesignSystem ["🎨 프론트엔드 디자인 시스템"]
        CSS["design-system.css"]
        Fragments["components.html"]
        StyleGuide["style-guide.html"]
        Doc_DesignSys["docs/design-system.md"]
    end

    subgraph KnowledgeDocs ["📚 주제별 문서 자산 (Docs)"]
        Doc_Harness["docs/harness-decision-framework.md"]
        Doc_GraphArch["docs/graph-engineering-architecture.md"]
        Doc_DeliveryState["docs/design/delivery-state-graph.md"]
        Doc_Risk["docs/pre-launch-risk-and-governance-guide.md"]
    end

    %% 노드 간 관계 엣지
    Rule_Agents --> Rule_Convention
    Rule_Convention --> Controller
    Rule_Convention --> Service
    
    Controller --> DTO
    Controller --> Service
    Service --> Repository
    Repository --> Entity

    VerifyScript --> FlywayLint
    VerifyScript --> DesignLint
    VerifyScript --> Spotless
    VerifyScript --> ArchUnit

    DesignLint --> CSS
    CSS --> Fragments
    CSS --> StyleGuide
    Doc_DesignSys --> CSS

    Doc_GraphArch --> Doc_DeliveryState
    Doc_Harness --> VerifyScript
```

---

## 2. 영역별 노드 & 문서 연결 가이드

### 1) 백엔드 아키텍처 노드 ➔ 연관 문서
- `Controller` / `Service` / `Repository` / `Entity`:
  - 📖 [**코딩 컨벤션 가이드 (code-convention.md)**](../code-convention.md)
  - 📜 [**ADR-0001: JWT 기반 무상태 인증 체계 (docs/adr/0001-stateless-jwt-authentication.md)**](./adr/0001-stateless-jwt-authentication.md)

### 2) 품질 및 검증 하네스 노드 ➔ 연관 문서
- `./scripts/verify.sh` 및 5대 린터:
  - 🛡️ [**테스트 하네스 구축 및 6대 통제 정책 가이드 (docs/harness-decision-framework.md)**](./harness-decision-framework.md)
  - 🤖 [**LLM 소프트웨어 엔지니어링 방법론 가이드 (docs/llm-software-engineering-guide.md)**](./llm-software-engineering-guide.md)

### 3) 디자인 시스템 & UI 노드 ➔ 연관 문서
- `/css/design-system.css`, `style-guide.html`:
  - 🎨 [**공통 디자인 시스템 가이드 (docs/design-system.md)**](./design-system.md)

### 4) 그래프 엔지니어링 & 도메인 상태 노드 ➔ 연관 문서
- 배송 상태 전이 및 에이전트 오케스트레이션:
  - 🕸️ [**그래프 엔지니어링 아키텍처 가이드 (docs/graph-engineering-architecture.md)**](./graph-engineering-architecture.md)
  - 📋 [**배송 도메인 상태 전이 그래프 명세서 (docs/design/delivery-state-graph.md)**](./design/delivery-state-graph.md)

---

## 3. 개발자 & AI 에이전트를 위한 컨텍스트 탐색법

신무 기능 개발 시 위 지식 그래프 노드를 따라 아래 순서로 탐색하시면 단 1개의 부작용(Side-effect) 없이 안전하게 구현을 완료할 수 있습니다:

1. **설계 단계:** `docs/design/` 및 `docs/graph-engineering-architecture.md` 참조
2. **코드 구현:** `code-convention.md` 단방향 의존성(`Controller -> Service -> Repository`) 사수
3. **UI 개발:** `design-system.css` 토큰 및 `components.html` Thymeleaf 프래그먼트 활용
4. **검증 단계:** `./scripts/verify.sh` 5단계 하네스 검증 실행 후 0 exit code 달성
