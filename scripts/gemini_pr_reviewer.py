#!/usr/bin/env python3
"""
Custom Unlimited Gemini PR Code Reviewer for shinhan-gaecheokja
==============================================================
Directly calls Google Gemini API via official SDK / REST API,
performs beginner-friendly tutoring review in Korean, and posts PR comments.
"""

import sys
import os
import json
import urllib.request
import urllib.parse
import subprocess

GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")
PR_NUMBER = os.environ.get("PR_NUMBER")
REPO = os.environ.get("GITHUB_REPOSITORY")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN")

SYSTEM_PROMPT = """
당신은 소프트웨어 개발을 처음 배우는 초급 교육생들을 위한 친절하고 다정한 수석 AI 멘토입니다.
제시된 PR 소스 코드 변경 내역(diff)을 심층 분석하고, 다음 4가지 규칙을 준수하여 리뷰 댓글을 작성해 주세요.

[리뷰 4대 수칙]
1. **타겟 대상 & 다정한 튜터링 톤앤매너:**
   - 주 대상자는 초급 개발 교육생입니다. 단순히 지적만 하지 말고 왜 그렇게 수정해야 하는지 작동 원리와 핵심 비하인드 이유를 친절하고 다정한 한국어(ko-KR)로 설명해 주세요.
2. **프로젝트 아키텍처 수칙 검증:**
   - 의존성 방향은 단방향(`Controller -> Service -> Repository`)을 유지해야 합니다.
   - Getter, Setter는 수동 코딩을 금하고 Lombok(`@Getter`, `@Setter`) 100% 사용을 권장해 주세요.
   - `System.out.println` 콘솔 출력이 있다면 `log.info` 또는 `log.error` 변경을 다정하게 안내해 주세요.
3. **초급자 문서 동기화 권고:**
   - 신규 기능/개념 도입 시 `docs/` 가이드나 `code-convention.md` 보완을 권장해 주세요.
4. **리뷰 마크다운 서식 규격:**
   - 상단 타이틀은 `# 🤖 AI Code Review by Gemini`로 시작해 주세요.
   - 1분 퀵 요약, 칭찬할 점, 개선 피드백(코드 위치 및 추천 코드 블록 포함) 순으로 깔끔하게 마크다운으로 구성해 주세요.
"""

def get_pr_diff() -> str:
    """GitHub API로 PR의 diff 내용을 수집합니다."""
    url = f"https://api.github.com/repos/{REPO}/pulls/{PR_NUMBER}"
    req = urllib.request.Request(url, headers={
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3.diff",
        "User-Agent": "Gemini-PR-Reviewer"
    })
    try:
        with urllib.request.urlopen(req) as response:
            diff_text = response.read().decode('utf-8')
            return diff_text[:30000] # 토큰 절약을 위한 슬라이싱
    except Exception as e:
        print(f"⚠️ Diff 수집 실패: {e}")
        return ""

def call_gemini_api(diff_text: str) -> str:
    """Google Gemini 2.0 Flash / 1.5 Pro REST API를 직접 호출합니다."""
    # REST API Endpoint for Gemini
    api_url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={GEMINI_API_KEY}"
    
    payload = {
        "contents": [
            {
                "role": "user",
                "parts": [
                    {"text": SYSTEM_PROMPT},
                    {"text": f"### PR #{PR_NUMBER} Code Diff:\n```diff\n{diff_text}\n```"}
                ]
            }
        ],
        "generationConfig": {
            "temperature": 0.2,
            "maxOutputTokens": 2048
        }
    }
    
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(api_url, data=data, headers={"Content-Type": "application/json"})
    
    try:
        with urllib.request.urlopen(req) as response:
            res_data = json.loads(response.read().decode('utf-8'))
            candidates = res_data.get("candidates", [])
            if candidates:
                text = candidates[0].get("content", {}).get("parts", [{}])[0].get("text", "")
                return text
    except Exception as e:
        print(f"⚠️ Gemini API 호출 예외: {e}")
        # Fallback to gemini-1.5-flash
        try:
            fallback_url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={GEMINI_API_KEY}"
            req_fb = urllib.request.Request(fallback_url, data=data, headers={"Content-Type": "application/json"})
            with urllib.request.urlopen(req_fb) as fb_res:
                res_data = json.loads(fb_res.read().decode('utf-8'))
                return res_data["candidates"][0]["content"]["parts"][0]["text"]
        except Exception as fb_err:
            print(f"⚠️ Fallback Gemini API 호출 실패: {fb_err}")
            
    return ""

def post_github_comment(review_text: str):
    """생성된 리뷰를 GitHub PR 코멘트로 등록합니다."""
    if not review_text:
        print("⚠️ 리뷰 텍스트가 비어있어 코멘트를 게시하지 않습니다.")
        return

    url = f"https://api.github.com/repos/{REPO}/issues/{PR_NUMBER}/comments"
    payload = {"body": review_text}
    data = json.dumps(payload).encode('utf-8')
    
    req = urllib.request.Request(url, data=data, headers={
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json",
        "Content-Type": "application/json",
        "User-Agent": "Gemini-PR-Reviewer"
    })
    
    try:
        with urllib.request.urlopen(req) as response:
            if response.status in (200, 201):
                print(f"🎉 성공적으로 PR #{PR_NUMBER}에 Gemini AI 리뷰 코멘트를 등록했습니다!")
    except Exception as e:
        print(f"⚠️ PR 코멘트 게시 실패: {e}")

def main():
    if not GEMINI_API_KEY:
        print("❌ GEMINI_API_KEY 환경변수가 설정되지 않았습니다.")
        sys.exit(1)
        
    print(f"🚀 [Custom Gemini Reviewer] PR #{PR_NUMBER} 분석 시작...")
    diff_text = get_pr_diff()
    if not diff_text:
        print("ℹ️ 변경된 Diff 내용이 없어 리뷰를 종료합니다.")
        return
        
    print("🤖 Gemini AI 모델에 코드 분석 요청 중...")
    review_output = call_gemini_api(diff_text)
    
    print("📝 GitHub 코멘트 게시 중...")
    post_github_comment(review_output)

if __name__ == "__main__":
    main()
