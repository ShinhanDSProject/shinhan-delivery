#!/usr/bin/env bash

# ==============================================================================
# UI 공통 디자인 시스템 준수 여부 자동 검증 하네스 (lint-design-system.sh)
# ==============================================================================
# 1. src/main/resources/static 하위 모든 HTML 파일의 /css/design-system.css 포함 여부 검사
# ==============================================================================

set -eo pipefail

BOLD="\033[1m"
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
RESET="\033[0m"

STATIC_DIR="src/main/resources/static"
ERRORS=0

echo -e "🔍 공통 디자인 시스템 린팅 검사 시작: ${STATIC_DIR}"

if [ ! -d "$STATIC_DIR" ]; then
    echo -e "${GREEN}  ✓ static 디렉토리가 존재하지 않으므로 검사를 통과합니다.${RESET}"
    exit 0
fi

# 1. static 하위 HTML 파일에 design-system.css 연동 여부 검사
while IFS= read -r -d '' html_file; do
    if ! grep -q "design-system.css" "$html_file"; then
        echo -e "${RED}  ❌ [디자인 시스템 미비] ${html_file} 파일에 'design-system.css' 연동이 누락되었습니다.${RESET}"
        ERRORS=$((ERRORS + 1))
    fi
done < <(find "$STATIC_DIR" -type f -name "*.html" -print0)

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}❌ 총 ${ERRORS}개의 디자인 시스템 규격 위반 사항이 발견되었습니다.${RESET}"
    exit 1
fi

echo -e "${GREEN}✅ 모든 HTML 화면이 공통 디자인 시스템 규격을 100% 준수하고 있습니다!${RESET}"
exit 0
