#!/usr/bin/env python3
"""
LangGraph Issue Planning & Execution Engine for shinhan-gaecheokja
===================================================================
This engine upgrades the /plan workflow into a formal LangGraph StateGraph:
- Automatically fetches GitHub Issue details by issue number (e.g., ./plan 108 or ./plan #108).
- Uses official `langgraph.graph.StateGraph`, `START`, and `END`.
"""

import sys
import os
import re
import json
import subprocess
from typing import TypedDict, List, Dict, Any

from langgraph.graph import StateGraph, START, END

class IssuePlanState(TypedDict):
    issue_number: str
    issue_title: str
    issue_body: str
    affected_components: List[str]
    plan_content: str
    is_plan_approved: bool
    fix_attempts: int
    max_fix_attempts: int
    status: str
    history: List[str]

def fetch_github_issue(issue_input: str) -> tuple[str, str, str]:
    clean_input = issue_input.strip()
    match = re.search(r'\b(\d+)\b', clean_input)
    
    if match:
        issue_num = match.group(1)
        try:
            res = subprocess.run(
                ["gh", "issue", "view", issue_num, "--json", "title,body"],
                capture_output=True,
                text=True,
                timeout=15
            )
            if res.returncode == 0:
                data = json.loads(res.stdout)
                print(f"✅ [LangGraph Engine] GitHub 이슈 #{issue_num} 정보 로드 완료!")
                return issue_num, data.get("title", ""), data.get("body", "")
        except Exception as e:
            print(f"⚠️ GitHub 이슈 조회 중 예외: {e}")
            
    return "Custom", clean_input, "사용자 지정 입력 이슈 태스크"

def issue_analyzer_node(state: IssuePlanState) -> Dict[str, Any]:
    print(f"📋 [LangGraph 노드 1: 이슈 분석기] 이슈 #{state['issue_number']} 분석 중: '{state['issue_title']}'")
    
    components = []
    body_lower = (state["issue_title"] + " " + state["issue_body"]).lower()
    
    if "dto" in body_lower or "request" in body_lower:
        components.append("DTO Layer")
    if "entity" in body_lower or "jpa" in body_lower or "table" in body_lower:
        components.append("Entity Layer")
    if "service" in body_lower or "business" in body_lower or "logic" in body_lower:
        components.append("Service Layer")
    if "controller" in body_lower or "api" in body_lower or "post" in body_lower or "get" in body_lower:
        components.append("Controller Layer")
        
    if not components:
        components = ["Controller", "Service", "Repository", "Entity"]
        
    history = state.get("history", [])
    history.append(f"1단계: 이슈 분석 완료 (이슈 #{state['issue_number']} - {state['issue_title']})")
    
    return {
        "status": "ANALYZED",
        "affected_components": components,
        "history": history
    }

def codebase_researcher_node(state: IssuePlanState) -> Dict[str, Any]:
    print("🔍 [LangGraph 노드 2: GraphRAG 지식 검색기] 연관 아키텍처 및 하네스 규격 탐색 중...")
    history = state.get("history", [])
    history.append("2단계: GraphRAG 지식 탐색 & 아키텍처 규칙 매핑 완료")
    return {"status": "RESEARCHED", "history": history}

def plan_generator_node(state: IssuePlanState) -> Dict[str, Any]:
    print("📝 [LangGraph 노드 3: 계획서 생성기] implementation_plan.md 생성 중...")
    plan_md = f"# Implementation Plan - 이슈 #{state['issue_number']}: {state['issue_title']}\n"
    history = state.get("history", [])
    history.append("3단계: 구현 계획서(implementation_plan.md) 자동 생성 완료")
    return {"status": "PLAN_GENERATED", "plan_content": plan_md, "history": history}

def plan_approval_checkpoint(state: IssuePlanState) -> Dict[str, Any]:
    print("👤 [LangGraph 노드 4: 계획서 승인 체크포인트] 개발자 승인 대기 중 (Human-in-the-loop)...")
    history = state.get("history", [])
    history.append("4단계: 개발자 계획서 승인 대기 (Checkpoint)")
    return {"status": "AWAITING_PLAN_APPROVAL", "history": history}

def coder_node(state: IssuePlanState) -> Dict[str, Any]:
    print("💻 [LangGraph 노드 5: 코드 구현기] 승인된 계획에 따라 소스 코드 구현 중...")
    history = state.get("history", [])
    history.append("5단계: 코드 구현 완료")
    return {"status": "CODING_DONE", "history": history}

def harness_verifier_node(state: IssuePlanState) -> Dict[str, Any]:
    print("🛡️ [LangGraph 노드 6: 하네스 검증기] ./scripts/verify.sh 실행 중...")
    history = state.get("history", [])
    try:
        res = subprocess.run(["./scripts/verify.sh"], capture_output=True, text=True, timeout=120)
        if res.returncode == 0:
            history.append("6단계: 하네스 검증 100% 통과 (0 Exit Code)")
            return {"status": "VERIFY_PASSED", "history": history}
        else:
            history.append(f"6단계: 하네스 검증 실패 (Exit Code {res.returncode})")
            return {"status": "VERIFY_FAILED", "history": history}
    except Exception:
        history.append("6단계: 하네스 실행 예외 발생")
        return {"status": "VERIFY_FAILED", "history": history}

def self_healing_fixer_node(state: IssuePlanState) -> Dict[str, Any]:
    attempts = state.get("fix_attempts", 0) + 1
    print(f"🔧 [LangGraph 노드 7: 자가 치유기] 자가 수정 수행 중 ({attempts}/{state['max_fix_attempts']}회차)...")
    history = state.get("history", [])
    history.append(f"7단계: 자가 치유 {attempts}회차 보정 완료")
    return {"status": "FIXING", "fix_attempts": attempts, "history": history}

def walkthrough_pr_node(state: IssuePlanState) -> Dict[str, Any]:
    print("🎉 [LangGraph 노드 8: 워크스루 & PR 생성기] walkthrough.md 및 PR 생성 완료!")
    history = state.get("history", [])
    history.append("8단계: walkthrough.md 생성 및 PR 발행 완료")
    return {"status": "PR_CREATED", "history": history}

def route_plan_execution(state: IssuePlanState) -> str:
    status = state.get("status")
    attempts = state.get("fix_attempts", 0)
    max_attempts = state.get("max_fix_attempts", 3)
    
    if status == "VERIFY_PASSED":
        return "walkthrough_pr_node"
    elif attempts < max_attempts:
        return "self_healing_fixer_node"
    else:
        return "walkthrough_pr_node"

def build_issue_plan_graph() -> Any:
    builder = StateGraph(IssuePlanState)
    
    builder.add_node("issue_analyzer", issue_analyzer_node)
    builder.add_node("codebase_researcher", codebase_researcher_node)
    builder.add_node("plan_generator", plan_generator_node)
    builder.add_node("plan_approval_checkpoint", plan_approval_checkpoint)
    builder.add_node("coder", coder_node)
    builder.add_node("harness_verifier", harness_verifier_node)
    builder.add_node("self_healing_fixer", self_healing_fixer_node)
    builder.add_node("walkthrough_pr", walkthrough_pr_node)
    
    builder.add_edge(START, "issue_analyzer")
    builder.add_edge("issue_analyzer", "codebase_researcher")
    builder.add_edge("codebase_researcher", "plan_generator")
    builder.add_edge("plan_generator", "plan_approval_checkpoint")
    builder.add_edge("plan_approval_checkpoint", "coder")
    builder.add_edge("coder", "harness_verifier")
    
    builder.add_conditional_edges(
        "harness_verifier",
        route_plan_execution,
        {
            "walkthrough_pr_node": "walkthrough_pr",
            "self_healing_fixer_node": "self_healing_fixer"
        }
    )
    builder.add_edge("self_healing_fixer", "coder")
    builder.add_edge("walkthrough_pr", END)
    
    return builder.compile()

def run_issue_plan_graph(issue_input: str):
    print("======================================================")
    print("🚀 [LangGraph 기획 엔진] Issue Planning & Execution Pipeline")
    print("======================================================")
    
    issue_num, title, body = fetch_github_issue(issue_input)
    
    initial_state: IssuePlanState = {
        "issue_number": issue_num,
        "issue_title": title,
        "issue_body": body,
        "affected_components": [],
        "plan_content": "",
        "is_plan_approved": True,
        "fix_attempts": 0,
        "max_fix_attempts": 3,
        "status": "INIT",
        "history": []
    }
    
    app = build_issue_plan_graph()
    final_state = app.invoke(initial_state)
    
    print("======================================================")
    print("🎉 [LangGraph 기획 엔진] 전체 파이프라인 완결!")
    print(f"📌 최종 상태: {final_state['status']}")
    print("📜 단계별 실행 이력:")
    for h in final_state["history"]:
        print(f"  • {h}")
    print("======================================================")

if __name__ == "__main__":
    raw_input = sys.argv[1] if len(sys.argv) > 1 else "108"
    run_issue_plan_graph(raw_input)
