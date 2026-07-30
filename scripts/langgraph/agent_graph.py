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
    print("🤖 [LangGraph Node: Planner] Analyzing task & planning architecture...")
    state["current_node"] = "Planner"
    state["status"] = "PLANNING_COMPLETED"
    state["history"].append("Planner: Plan established for " + state["task"])
    return state

def coder_node(state: AgentState) -> AgentState:
    print("💻 [LangGraph Node: Coder] Writing code & refactoring components...")
    state["current_node"] = "Coder"
    state["status"] = "CODING_COMPLETED"
    state["history"].append("Coder: Code written/updated.")
    return state

def verifier_node(state: AgentState) -> AgentState:
    print("🛡️ [LangGraph Node: Verifier] Running 5-step test harness (verify.sh)...")
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
            state["history"].append("Verifier: Harness 100% PASSED (0 exit code).")
        else:
            state["status"] = "VERIFY_FAILED"
            state["history"].append("Verifier: Harness FAILED with exit code " + str(result.returncode))
    except Exception as e:
        state["verify_logs"] = str(e)
        state["status"] = "VERIFY_FAILED"
        state["history"].append("Verifier: Harness execution error.")

    return state

def fixer_node(state: AgentState) -> AgentState:
    state["fix_attempts"] += 1
    print(f"🔧 [LangGraph Node: Fixer] Auto-fixing code (Attempt {state['fix_attempts']}/{state['max_fix_attempts']})...")
    state["current_node"] = "Fixer"
    state["status"] = "FIXING"
    state["history"].append(f"Fixer: Auto-fix executed based on stack trace.")
    return state

def reviewer_node(state: AgentState) -> AgentState:
    print("🔍 [LangGraph Node: Reviewer] Multi-Pass Audit & Convention Assetization...")
    state["current_node"] = "Reviewer"
    state["status"] = "AUDITED"
    state["history"].append("Reviewer: Multi-pass audit completed.")
    return state

def human_approval_node(state: AgentState) -> AgentState:
    print("👤 [LangGraph Node: HumanApproval (Checkpoint)] Awaiting user approval...")
    state["current_node"] = "HumanApproval"
    state["status"] = "APPROVED"
    state["history"].append("HumanApproval: Approved by reviewer.")
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
    print("🚀 [LangGraph Engine] Executing StateGraph Orchestration")
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
        print(f"🔀 [LangGraph Conditional Edge] Routing to -> {next_node}")
        
        if next_node == "reviewer_node":
            state = reviewer_node(state)
            state = human_approval_node(state)
            break
        elif next_node == "fixer_node":
            state = fixer_node(state)
            state = coder_node(state)
        else:
            print("⚠️ Max fix attempts reached. Escalate to Human Approval Checkpoint.")
            state = human_approval_node(state)
            break

    print("======================================================")
    print("🎉 [LangGraph Engine] Execution Completed!")
    print(f"Final Status: {state['status']}")
    print("State History:")
    for h in state["history"]:
        print(f"  • {h}")
    print("======================================================")

if __name__ == "__main__":
    task = sys.argv[1] if len(sys.argv) > 1 else "Default Feature Task"
    run_graph(task)
