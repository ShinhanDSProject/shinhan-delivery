# 🕸️ GraphRAG 지식 그래프 검색 가이드 (GraphRAG Guide)

> **`shinhan-delivery` 프로젝트의 GraphRAG 지식 네트워크 탐색 및 검색 엔진 구현 가이드북**

---

## 📑 목차
- [1. 개요 및 왜 GraphRAG인가?](#1-개요-및-왜-graphrag인가)
- [2. 지식 그래프 노드 & 엣지 스키마](#2-지식-그래프-노드--엣지-스키마)
- [3. 다단계 추론(Multi-hop Reasoning) 검색 엔진](#3-다단계-추론multi-hop-reasoning-검색-엔진)
- [4. 실전 실행 명령어](#4-실전-실행-명령어)

---

## 1. 개요 및 왜 GraphRAG인가?

**GraphRAG**는 프로젝트의 문서, 아키텍처 계층, 하네스 린터, DB 스키마 간의 **엔티티(Entity: 노드)와 관계(Relationship: 엣지)**를 추출하여 구축된 **지식 그래프(Knowledge Graph)** 기반 검색 기술입니다.

기존 단어 임베딩 검색(Vector RAG)이 단편적인 텍스트만 찾는 것과 달리, GraphRAG는 **"A 모듈 수정 시 B, C 모듈 및 디자인 시스템에 미치는 다단계 종속성(Multi-hop Dependencies)"**을 명확히 추론할 수 있게 해줍니다.

---

## 2. 지식 그래프 노드 & 엣지 스키마

지식 데이터셋은 [`scripts/graphrag/knowledge_graph.json`](../scripts/graphrag/knowledge_graph.json)에 저장되며 아래와 같은 JSON 노드/엣지 스키마로 관리됩니다:

```json
{
  "nodes": [
    { "id": "MemberController", "type": "Controller", "description": "Handles member REST endpoints" },
    { "id": "DesignSystemCSS", "type": "DesignSystem", "description": "Vanilla CSS tokens (/css/design-system.css)" }
  ],
  "edges": [
    { "source": "MemberController", "target": "MemberService", "relation": "DELEGATES_TO" },
    { "source": "DesignSystemLinter", "target": "DesignSystemCSS", "relation": "VALIDATES_LINK" }
  ]
}
```

---

## 3. 다단계 추론(Multi-hop Reasoning) 검색 엔진

[`scripts/graphrag/graphrag_search.py`](../scripts/graphrag/graphrag_search.py) 검색 엔진은 특정 키워드를 검색 시, **단순 노드 정보뿐만 아니라 해당 노드로 들어오거나(Incoming Edges) 나가는(Outgoing Edges) 연관 지식 네트워크**를 함께 출력합니다.

---

## 4. 실전 실행 명령어

```bash
# GraphRAG 지식 네트워크 탐색 (예: DesignSystem 검색)
python3 scripts/graphrag/graphrag_search.py "DesignSystem"

# 백엔드 컨트롤러 종속성 탐색
python3 scripts/graphrag/graphrag_search.py "Controller"
```
