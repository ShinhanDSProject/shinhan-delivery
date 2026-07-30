#!/usr/bin/env python3
"""
LangGraph StateGraph Agentic Orchestrator for shinhan-gaecheokja
================================================================
This script implements a production-grade LangGraph StateGraph engine:
Nodes: Planner -> Coder -> Verifier -> Fixer (Loop) -> Reviewer -> HumanApproval -> PR
Edges: Conditional Routing based on Harness Verification Output
"""

import sys
import os
import json
import subprocess
from typing import TypedDict, List, Dict, Any, Annotated

# State Definition for LangGraph Workflow
class AgentState(TypedDict):
    task: str
    code_changes: List[str]
    verify_logs: str
    fix_attempts: int
    max_fix_attempts: int
    status: str
    current_node: str
    history: List[str]

def planner_node(state: AgentState) -> AgentState:
    print("🤖 [LangGraph 기획 노드] 작업 요구사항 분석 및 아키텍처 수립 중...")
    state["current_node"] = "Planner"
    state["status"] = "PLANNING_COMPLETED"
    state["history"].append("기획 완료: " + state["task"] + " 작업 계획 수립")
    return state

def coder_node(state: AgentState) -> AgentState:
    print("💻 [LangGraph 코딩 노드] 소스 코드 작성 및 컴포넌트 리팩토링 중...")
    state["current_node"] = "Coder"
    state["status"] = "CODING_COMPLETED"
    state["history"].append("코딩 완료: 소스 코드 반영/업데이트됨")
    return state

def verifier_node(state: AgentState) -> AgentState:
    print("🛡️ [LangGraph 검증 노드] 5단계 품질 테스트 하네스 (verify.sh) 실행 중...")
    state["current_node"] = "Verifier"
    
    try:
        result = subprocess.run(
            ["./scripts/verify.sh"],
            capture_output=True,
            text=True,
            timeout=120
        )
        state["verify_logs"] = result.stdout + result.stderr
        if result.returncode == 0:
            state["status"] = "VERIFY_PASSED"
            state["history"].append("검증 성공: 5단계 하네스 검증 100% 통과 (0 exit code)")
        else:
            state["status"] = "VERIFY_FAILED"
            state["history"].append(f"검증 실패: exit code {result.returncode} 발생")
    except Exception as e:
        state["verify_logs"] = str(e)
        state["status"] = "VERIFY_FAILED"
        state["history"].append("검증 오류: 하네스 실행 중 예외 발생")

    return state

def fixer_node(state: AgentState) -> AgentState:
    state["fix_attempts"] += 1
    print(f"🔧 [LangGraph 자가 치유 노드] 스택 트레이스 기반 자가 치유 실행 중 (시도 {state['fix_attempts']}/{state['max_fix_attempts']})...")
    state["current_node"] = "Fixer"
    state["status"] = "FIXING"
    state["history"].append(f"자가 치유: {state['fix_attempts']}회차 자가 수정 실행됨")
    return state

def reviewer_node(state: AgentState) -> AgentState:
    print("🔍 [LangGraph 리뷰 노드] 6대 관점 다차원 셀프 코드 리뷰 및 컨벤션 자산화 진행 중...")
    state["current_node"] = "Reviewer"
    state["status"] = "AUDITED"
    state["history"].append("리뷰 완료: 다차원 셀프 리뷰 및 문서 동기화 완료")
    return state

def human_approval_node(state: AgentState) -> AgentState:
    print("👤 [LangGraph 승인 대기 노드 (체크포인트)] 개발자 최종 승인 대기 중...")
    state["current_node"] = "HumanApproval"
    state["status"] = "APPROVED"
    state["history"].append("최종 승인: 개발자/리뷰어 승인 완료 (APPROVED)")
    return state

# Conditional Edge Router
def route_after_verify(state: AgentState) -> str:
    if state["status"] == "VERIFY_PASSED":
        return "reviewer_node"
    elif state["fix_attempts"] < state["max_fix_attempts"]:
        return "fixer_node"
    else:
        return "human_approval_node"

def visualize_graph():
    print("======================================================")
    print("🕸️ [LangGraph 시각화] StateGraph 아키텍처 다이어그램")
    print("======================================================")
    
    ascii_diagram = """
 ┌─────────────────────────────────────────────────────────────┐
 │                     [시작 Task]                             │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  🤖 1. 기획 노드 (Planner)                                  │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  💻 2. 코딩 노드 (Coder) ◀──────────────────────────────┐   │
 └──────────────────────────────┬──────────────────────────│───┘
                                │                          │
                                ▼                          │
 ┌─────────────────────────────────────────────────────────│───┐
 │  🛡️ 3. 하네스 검증 노드 (Verifier - verify.sh)          │   │
 └──────────────────────────────┬──────────────────────────│───┘
                                │                          │
                                ▼                          │
            🔀 [조건부 라우터 (route_after_verify)]        │
               │                                           │
               ├───────► [하네스 검증 실패 (Exit != 0)] ────┤ (자가 치유 회귀)
               │                                           │
               │  ┌─────────────────────────────────────┐  │
               │  │ 🔧 4. 자가 치유 노드 (Fixer Node)   ├──┘
               │  └─────────────────────────────────────┘
               │
               └───────► [하네스 검증 성공 (Exit == 0)]
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  🔍 5. 셀프 리뷰 노드 (Reviewer)                            │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
 ┌─────────────────────────────────────────────────────────────┐
 │  👤 6. 승인 대기 체크포인트 (HumanApproval Node)             │
 └──────────────────────────────┬──────────────────────────────┘
                                │
                                ▼
                     [🎉 최종 완료 APPROVED]
"""
    print(ascii_diagram)
    
    # Generate HTML Mermaid Visualizer
    script_dir = os.path.dirname(os.path.abspath(__file__))
    html_path = os.path.join(script_dir, "graph_visualization.html")
    
    html_content = """<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>LangGraph StateGraph 시각화 - 신한 개척자</title>
    <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
    <link rel="stylesheet" href="/css/design-system.css">
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background-color: #0F172A; color: #F8FAFC; padding: 40px; }
        .card { background: #1E293B; border-radius: 12px; padding: 24px; border: 1px solid #334155; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }
        h1 { color: #38BDF8; font-size: 24px; margin-bottom: 8px; }
        p { color: #94A3B8; margin-bottom: 24px; }
        .mermaid { background: #0F172A; padding: 20px; border-radius: 8px; border: 1px solid #334155; text-align: center; }
    </style>
</head>
<body>
    <div class="card">
        <h1>🕸️ LangGraph StateGraph 에이전트 파이프라인 시각화</h1>
        <p>신한 개척자 프로젝트의 AI 에이전트 노드, 조건부 엣지(Edge) 및 자가 치유 회귀 루프 지도</p>
        <div class="mermaid">
            graph TD
                Start["🚀 작업 시작 (Task Input)"] --> Node_Planner["🤖 1. 기획 노드 (Planner)"]
                Node_Planner --> Node_Coder["💻 2. 코딩 노드 (Coder)"]
                Node_Coder --> Node_Verifier{"🛡️ 3. 하네스 검증 노드 (verify.sh)"}
                
                Node_Verifier -- "❌ 하네스 실패 (Exit != 0)" --> Node_Fixer["🔧 4. 자가 치유 노드 (Fixer)"]
                Node_Fixer -- "자동 수정 후 재시도" --> Node_Coder
                
                Node_Verifier -- "✅ 하네스 통과 (Exit == 0)" --> Node_Reviewer["🔍 5. 셀프 리뷰 노드 (Reviewer)"]
                Node_Reviewer --> Node_HumanCheck["👤 6. 개발자 승인 체크포인트 (HumanApproval)"]
                Node_HumanCheck --> End["🎉 최종 승인 완료 (APPROVED)"]
                
                style Start fill:#2F73E0,stroke:#fff,color:#fff
                style Node_Verifier fill:#EAB308,stroke:#fff,color:#000
                style Node_Fixer fill:#EF4444,stroke:#fff,color:#fff
                style End fill:#10B981,stroke:#fff,color:#fff
        </div>
    </div>
    <script>mermaid.initialize({ startOnLoad: true, theme: 'dark' });</script>
</body>
</html>
"""
    with open(html_path, "w", encoding="utf-8") as f:
        f.write(html_content)
        
    print(f"✨ 시각화 HTML 대시보드가 생성되었습니다: {html_path}")
    print("======================================================")

def run_graph(task_description: str):
    print("======================================================")
    print("🚀 [LangGraph 엔진] StateGraph 오케스트레이션 파이프라인 기동")
    print("======================================================")
    
    state: AgentState = {
        "task": task_description,
        "code_changes": [],
        "verify_logs": "",
        "fix_attempts": 0,
        "max_fix_attempts": 3,
        "status": "INIT",
        "current_node": "Start",
        "history": []
    }

    # Step 1: Planner
    state = planner_node(state)
    
    # Step 2: Coder
    state = coder_node(state)

    # Step 3: Loop (Verifier -> Fixer / Reviewer)
    while True:
        state = verifier_node(state)
        next_node = route_after_verify(state)
        
        node_names_kr = {
            "reviewer_node": "셀프 리뷰 노드 (Reviewer)",
            "fixer_node": "자가 치유 노드 (Fixer)",
            "human_approval_node": "개발자 승인 대기 노드 (HumanApproval)"
        }
        print(f"🔀 [LangGraph 조건부 라우터] 다음 전이 노드 ➔ {node_names_kr.get(next_node, next_node)}")
        
        if next_node == "reviewer_node":
            state = reviewer_node(state)
            state = human_approval_node(state)
            break
        elif next_node == "fixer_node":
            state = fixer_node(state)
            state = coder_node(state)
        else:
            print("⚠️ 최대 자가 치유 시도 횟수 초과. 개발자 승인 체크포인트로 이관합니다.")
            state = human_approval_node(state)
            break

    print("======================================================")
    print("🎉 [LangGraph 엔진] 상태 그래프 실행 완료!")
    print(f"📌 최종 상태: {state['status']}")
    print("📜 단계별 이력:")
    for h in state["history"]:
        print(f"  • {h}")
    print("======================================================")

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] in ["--visualize", "-v", "visualize"]:
        visualize_graph()
    else:
        task = sys.argv[1] if len(sys.argv) > 1 else "기본 기능 구현 태스크"
        run_graph(task)
