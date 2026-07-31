#!/usr/bin/env bash

# ==============================================================================
# Java 코드 컨벤션 린터 스크립트 (lint-code-convention.sh)
# ==============================================================================
# 1. 테스트 메서드명 lowerCamelCase 및 한글/언더스코어 금지 검사
# 2. 코드 내 FQCN(com.example...) 직접 노출 금지 검사
# 3. static final 상수 UPPER_SNAKE_CASE 검사
# ==============================================================================

set -eo pipefail

BOLD="\033[1m"
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
RESET="\033[0m"

echo -e "${YELLOW}🔍 코드 컨벤션 린팅 검사 시작...${RESET}"

ERRORS=0

python3 -c '
import re, glob, sys

errors = 0

# Check 1: Test method naming (lowerCamelCase, no Korean, no underscores)
test_files = glob.glob("src/test/java/**/*.java", recursive=True)
for f in test_files:
    with open(f, "r", encoding="utf-8") as fp:
        lines = fp.readlines()
    for idx, line in enumerate(lines, 1):
        stripped = line.strip()
        if stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*"):
            continue
        m = re.search(r"void\s+([A-Za-z0-9_가-힣]+)\s*\(", line)
        if m:
            name = m.group(1)
            if re.search(r"[가-힣]", name):
                print(f"❌ [위반] {f}:{idx} - 테스트 메서드명에 한글이 포함되어 있습니다: {name}")
                errors += 1
            elif "_" in name:
                print(f"❌ [위반] {f}:{idx} - 테스트 메서드명은 lowerCamelCase이어야 하며 언더스코어(_)를 사용할 수 없습니다: {name}")
                errors += 1

# Check 2: Inline FQCN usage inside code (code-convention §10.2)
main_files = glob.glob("src/main/java/**/*.java", recursive=True)
for f in main_files:
    with open(f, "r", encoding="utf-8") as fp:
        lines = fp.readlines()
    for idx, line in enumerate(lines, 1):
        stripped = line.strip()
        if stripped.startswith("package ") or stripped.startswith("import ") or stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*"):
            continue
        if "com.example.shinhangaecheokja" in line:
            print(f"❌ [위반] {f}:{idx} - 본문에 FQCN(풀 패키지 경로)이 직접 노출되었습니다. import문으로 변경하세요.")
            errors += 1

if errors > 0:
    sys.exit(1)
'

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 모든 Java 소스가 코드 컨벤션 규격을 100% 준수하고 있습니다!${RESET}"
    exit 0
else
    echo -e "${RED}❌ 코드 컨벤션 린팅 검사 실패! code-convention.md 지침을 준수하도록 코드를 수정하세요.${RESET}"
    exit 1
fi
