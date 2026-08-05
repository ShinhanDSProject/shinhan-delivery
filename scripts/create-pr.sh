#!/usr/bin/env bash

# ==============================================================================
# 자동 PR 생성 원스톱 스크립트 (create-pr.sh)
# ==============================================================================
# 사용법: bash scripts/create-pr.sh "feat: 변경사항 커밋 및 PR 제목"
# 1. 로컬 통합 테스트 하네스 (verify.sh) 실행
# 2. 변경사항 자동 git add & commit
# 3. 브랜치 remote 푸시
# 4. gh CLI로 PR 자동 생성
# ==============================================================================

set -eo pipefail

COMMIT_MSG="${1:-}"

if [ -z "$COMMIT_MSG" ]; then
    echo "❌ 에러: 커밋 메시지 및 PR 제목을 입력해 주세요."
    echo "사용예시: bash scripts/create-pr.sh \"feat: 배송 상태 변경 기능 추가\""
    exit 1
fi

echo "🚀 [1/4] 로컬 테스트 하네스 검증 시작..."
if bash scripts/verify.sh; then
    echo "✅ 검증 성공!"
else
    echo "❌ 하네스 검사 실패로 인해 PR 생성을 중단합니다."
    exit 1
fi

CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" = "main" ]; then
    echo "❌ main 브랜치에서는 직접 커밋/푸시할 수 없습니다. 새 브랜치를 생성하세요."
    exit 1
fi

echo "🚀 [2/4] 변경사항 커밋 및 푸시..."
git add .
if git diff --cached --quiet; then
    echo "ℹ️ 커밋할 변경사항이 없어 커밋 단계를 건너뜁니다."
else
    git commit -m "$COMMIT_MSG"
fi
git push -u origin "$CURRENT_BRANCH"

echo "🚀 [3/4] GitHub PR 자동 생성..."
gh pr create --title "$COMMIT_MSG" --body-file .github/pull_request_template.md --web

echo "🎉 작업이 완벽하게 처리되었습니다!"
