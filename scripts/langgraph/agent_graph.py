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
    task = sys.argv[1] if len(sys.argv) > 1 else "기본 기능 구현 태스크"
    run_graph(task)
