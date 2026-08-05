#!/usr/bin/env bash

# ==============================================================================
# ?ë™ PR ?ì„± ?ìŠ¤???¤í¬ë¦½íŠ¸ (create-pr.sh)
# ==============================================================================
# ?¬ìš©ë²? bash scripts/create-pr.sh "feat: ë³€ê²½ì‚¬??ì»¤ë°‹ ë°?PR ?œëª©"
# 1. ë¡œì»¬ ?µí•© ?ŒìŠ¤???˜ë„¤??(verify.sh) ?¤í–‰
# 2. ë³€ê²½ì‚¬???ë™ git add & commit
# 3. ë¸Œëœì¹?remote ?¸ì‹œ
# 4. gh CLIë¡?PR ?ë™ ?ì„±
# ==============================================================================

set -eo pipefail

COMMIT_MSG="${1:-}"

if [ -z "$COMMIT_MSG" ]; then
    echo "???ëŸ¬: ì»¤ë°‹ ë©”ì‹œì§€ ë°?PR ?œëª©???…ë ¥??ì£¼ì„¸??"
    echo "?¬ìš©?ˆì‹œ: bash scripts/create-pr.sh \"feat: ë°°ì†¡ ?íƒœ ë³€ê²?ê¸°ëŠ¥ ì¶”ê?\""
    exit 1
fi

echo "?? [1/4] ë¡œì»¬ ?ŒìŠ¤???˜ë„¤??ê²€ì¦??œì‘..."
if bash scripts/verify.sh; then
    echo "??ê²€ì¦??±ê³µ!"
else
    echo "???˜ë„¤??ê²€???¤íŒ¨ë¡??¸í•´ PR ?ì„±??ì¤‘ë‹¨?©ë‹ˆ??"
    exit 1
fi

CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" = "main" ]; then
    echo "??main ë¸Œëœì¹˜ì—?œëŠ” ì§ì ‘ ì»¤ë°‹/?¸ì‹œ?????†ìŠµ?ˆë‹¤. ??ë¸Œëœì¹˜ë? ?ì„±?˜ì„¸??"
    exit 1
fi

echo "?? [2/4] ë³€ê²½ì‚¬??ì»¤ë°‹ ë°??¸ì‹œ..."
git add .
git commit -m "$COMMIT_MSG" || true
git push -u origin "$CURRENT_BRANCH"

echo "?? [3/4] GitHub PR ?ë™ ?ì„±..."
gh pr create --title "$COMMIT_MSG" --body-file .github/pull_request_template.md --web || true

echo "?‰ ?‘ì—…???„ë²½?˜ê²Œ ì²˜ë¦¬?˜ì—ˆ?µë‹ˆ??"
