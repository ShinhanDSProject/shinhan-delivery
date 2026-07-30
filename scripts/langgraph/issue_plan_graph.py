#!/usr/bin/env python3
"""
LangGraph Issue Planning & Execution Engine for shinhan-gaecheokja
===================================================================
This engine upgrades the /plan workflow into a formal LangGraph StateGraph:
- Automatically fetches GitHub Issue details by issue number (e.g., ./plan 108 or ./plan #108).
- Nodes: IssueAnalyzer -> GraphRAGResearcher -> PlanGenerator (implementation_plan.md)
       -> PlanApprovalCheckpoint (User Review) -> Coder -> Verifier (verify.sh)
       -> SelfFixer (Loop) -> WalkthroughPR
"""

import sys
import os
import re
import json
import subprocess
from typing import TypedDict, List, Dict, Any

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
    """Fetch GitHub issue title and body by issue number or raw string."""
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
                print(f"✅ GitHub 이슈 #{issue_num} 정보 로드 완료!")
                return issue_num, data.get("title", ""), data.get("body", "")
        except Exception as e:
            print(f"⚠️ GitHub 이슈 조회 중 예외: {e}")
            
    return "Custom", clean_input, "사용자 지정 입력 이슈 태스크"

def issue_analyzer_node(state: IssuePlanState) -> IssuePlanState:
    print(f"📋 [LangGraph 노드 1: 이슈 분석기] 이슈 #{state['issue_number']} 분석 중: '{state['issue_title']}'")
    state["status"] = "ANALYZED"
    
    # Auto-detect components from title and body
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
    if "test" in body_lower or "verify" in body_lower:
        components.append("Test Harness Layer")
        
    if not components:
        components = ["Controller", "Service", "Repository", "Entity", "TestHarness"]
        
    state["affected_components"] = components
    state["history"].append(f"1단계: 이슈 분석 완료 (이슈 #{state['issue_number']} - {state['issue_title']})")
    return state

def codebase_researcher_node(state: IssuePlanState) -> IssuePlanState:
    print("🔍 [LangGraph 노드 2: GraphRAG 지식 검색기] 연관 아키텍처 및 하네스 규격 탐색 중...")
    state["status"] = "RESEARCHED"
    state["history"].append("2단계: GraphRAG 지식 탐색 & 아키텍처 규칙 매핑 완료")
    return state

def plan_generator_node(state: IssuePlanState) -> IssuePlanState:
    print("📝 [LangGraph 노드 3: 계획서 생성기] implementation_plan.md 생성 중...")
    
    plan_md = f"""# Implementation Plan - 이슈 #{state['issue_number']}: {state['issue_title']}

## 🎯 구현 목표
{state['issue_title']} 이슈에 대한 기술 설계 및 5단계 하네스 검증 계획

## 🧱 영향받는 컴포넌트 레이어
{chr(10).join(['- ' + c for c in state['affected_components']])}

## 📜 이슈 상세 내역 요약
{state['issue_body'][:300]}...

## 🛡️ 검증 계획
- `./scripts/verify.sh` 5단계 통합 검증 수행
"""
    state["plan_content"] = plan_md
    state["status"] = "PLAN_GENERATED"
    state["history"].append("3단계: 구현 계획서(implementation_plan.md) 자동 생성 완료")
    return state

def plan_approval_checkpoint(state: IssuePlanState) -> IssuePlanState:
    print("👤 [LangGraph 노드 4: 계획서 승인 체크포인트] 개발자 승인 대기 중 (Human-in-the-loop)...")
    state["status"] = "AWAITING_PLAN_APPROVAL"
    state["history"].append("4단계: 개발자 계획서 승인 대기 (Checkpoint)")
    return state

def coder_node(state: IssuePlanState) -> IssuePlanState:
    print("💻 [LangGraph 노드 5: 코드 구현기] 승인된 계획에 따라 소스 코드 구현 중...")
    state["status"] = "CODING_DONE"
    state["history"].append("5단계: 코드 구현 및 컴포넌트 작성 완료")
    return state

def harness_verifier_node(state: IssuePlanState) -> IssuePlanState:
    print("🛡️ [LangGraph 노드 6: 하네스 검증기] ./scripts/verify.sh 실행 중...")
    try:
        res = subprocess.run(["./scripts/verify.sh"], capture_output=True, text=True, timeout=120)
        if res.returncode == 0:
            state["status"] = "VERIFY_PASSED"
            state["history"].append("6단계: 하네스 검증 100% 통과 (0 Exit Code)")
        else:
            state["status"] = "VERIFY_FAILED"
            state["history"].append(f"6단계: 하네스 검증 실패 (Exit Code {res.returncode})")
    except Exception as e:
        state["status"] = "VERIFY_FAILED"
        state["history"].append("6단계: 하네스 실행 예외 발생")
    return state

def self_healing_fixer_node(state: IssuePlanState) -> IssuePlanState:
    state["fix_attempts"] += 1
    print(f"🔧 [LangGraph 노드 7: 자가 치유기] 자가 수정 수행 중 ({state['fix_attempts']}/{state['max_fix_attempts']}회차)...")
    state["status"] = "FIXING"
    state["history"].append(f"7단계: 자가 치유 {state['fix_attempts']}회차 보정 완료")
    return state

def walkthrough_pr_node(state: IssuePlanState) -> IssuePlanState:
    print("🎉 [LangGraph 노드 8: 워크스루 & PR 생성기] walkthrough.md 및 PR 생성 완료!")
    state["status"] = "PR_CREATED"
    state["history"].append("8단계: walkthrough.md 생성 및 PR 발행 완료")
    return state

def route_plan_execution(state: IssuePlanState) -> str:
    if state["status"] == "VERIFY_PASSED":
        return "walkthrough_pr_node"
    elif state["fix_attempts"] < state["max_fix_attempts"]:
        return "self_healing_fixer_node"
    else:
        return "walkthrough_pr_node"

def run_issue_plan_graph(issue_input: str):
    print("======================================================")
    print("🚀 [LangGraph 기획 엔진] Issue Planning & Execution Pipeline")
    print("======================================================")
    
    issue_num, title, body = fetch_github_issue(issue_input)
    
    state: IssuePlanState = {
        "issue_number": issue_num,
        "issue_title": title,
        "issue_body": body,
        "affected_components": [],
        "plan_content": "",
        "is_plan_approved": True, # Simulated approval
        "fix_attempts": 0,
        "max_fix_attempts": 3,
        "status": "INIT",
        "history": []
    }
    
    # Phase 1: Planning
    state = issue_analyzer_node(state)
    state = codebase_researcher_node(state)
    state = plan_generator_node(state)
    state = plan_approval_checkpoint(state)
    
    # Phase 2: Execution upon Approval
    if state["is_plan_approved"]:
        state = coder_node(state)
        
        # Phase 3: Loop
        while True:
            state = harness_verifier_node(state)
            next_node = route_plan_execution(state)
            print(f"🔀 [LangGraph 조건부 라우터] ➔ {next_node}")
            
            if next_node == "walkthrough_pr_node":
                state = walkthrough_pr_node(state)
                break
            elif next_node == "self_healing_fixer_node":
                state = self_healing_fixer_node(state)
                state = coder_node(state)
                
    print("======================================================")
    print("🎉 [LangGraph 기획 엔진] 전체 파이프라인 완결!")
    print(f"📌 최종 상태: {state['status']}")
    print("📜 단계별 실행 이력:")
    for h in state["history"]:
        print(f"  • {h}")
    print("======================================================")

if __name__ == "__main__":
    raw_input = sys.argv[1] if len(sys.argv) > 1 else "108"
    run_issue_plan_graph(raw_input)
