#!/usr/bin/env bash

# ==============================================================================
# 로컬 CI 통합 검증 피드백 하네스 스크립트 (verify.sh)
# ==============================================================================
# 이 스크립트는 CI(GitHub Actions) 환경과 동일한 순서로 검증을 수행합니다.
# 1. Flyway 마이그레이션 파일명 규격 검사
# 2. Flyway 마이그레이션 DDL 규칙 (Online DDL) 검사
# 3. Spotless 코드 포맷팅 스타일 검사
# 4. Gradle 테스트 및 전체 빌드 검사
# ==============================================================================

set -eo pipefail

BOLD="\033[1m"
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
BLUE="\033[34m"
RESET="\033[0m"

echo -e "${BLUE}======================================================"${RESET}
echo -e "${BLUE} 🚀 [Test Harness] 로컬 CI 통합 검증 피드백 루프 시작"${RESET}
echo -e "${BLUE}======================================================"${RESET}

# 실행할 gradle 명령어 선택 (gradlew 스크립트 존재 확인)
GRADLE_CMD="./gradlew"
if [ ! -f "$GRADLE_CMD" ]; then
    GRADLE_CMD="gradle"
fi

# Step 1: Flyway 파일명 린팅
echo -e "\n${YELLOW}📌 [1/4] Flyway 마이그레이션 파일명 규격 검사...${RESET}"
if bash scripts/lint-flyway-filenames.sh; then
    echo -e "${GREEN}  ✓ Flyway 파일명 규격 검사 통과${RESET}"
else
    echo -e "${RED}  ❌ [피드백] Flyway 파일명 규격 위반! 'src/main/resources/db/migration/' 파일명을 수정하세요.${RESET}"
    exit 1
fi

# Step 2: Flyway DDL 린팅
echo -e "\n${YELLOW}📌 [2/4] Flyway 마이그레이션 DDL 규격 검사...${RESET}"
if bash scripts/lint-flyway-ddl.sh; then
    echo -e "${GREEN}  ✓ Flyway DDL 규격 검사 통과${RESET}"
else
    echo -e "${RED}  ❌ [피드백] Flyway DDL 규격 위반! SQL 내 불리언/제약조건/ALTER 문법을 확인하세요.${RESET}"
    exit 1
fi

# Step 3: Spotless 포맷팅 검사
echo -e "\n${YELLOW}📌 [3/4] Spotless 코드 포맷팅 스타일 검사...${RESET}"
if $GRADLE_CMD spotlessCheck --quiet; then
    echo -e "${GREEN}  ✓ 코드 포맷팅(Spotless) 검사 통과${RESET}"
else
    echo -e "${RED}  ❌ [피드백] 코드 포맷팅 규칙 위반! '${GRADLE_CMD} spotlessApply' 명령어를 실행하여 포맷팅을 정리를 하세요.${RESET}"
    exit 1
fi

# Step 4: Gradle 테스트 & 빌드
echo -e "\n${YELLOW}📌 [4/4] Gradle 테스트 실행 및 아키텍처/단위 검사...${RESET}"
if $GRADLE_CMD test; then
    echo -e "${GREEN}  ✓ 테스트 및 빌드 검사 통과${RESET}"
else
    echo -e "${RED}  ❌ [피드백] 테스트 실패! 위 스택트레이스를 참고하여 실패한 테스트 코드 또는 비즈니스 로직을 수정하세요.${RESET}"
    exit 1
fi

echo -e "\n${GREEN}======================================================"${RESET}
echo -e "${GREEN} 🎉 [Test Harness] 모든 검증 통과! 안전하게 커밋/PR 가능합니다."${RESET}
echo -e "${GREEN}======================================================"${RESET}
exit 0
