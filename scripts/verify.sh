#!/usr/bin/env bash

# ==============================================================================
# 로컬 CI ?�합 검�??�드�??�네???�크립트 (verify.sh)
# ==============================================================================
# ???�크립트??CI(GitHub Actions) ?�경�??�일???�서�?검증을 ?�행?�니??
# 1. Flyway 마이그레?�션 ?�일�?규격 검??
# 2. Flyway 마이그레?�션 DDL 규칙 (Online DDL) 검??
# 3. UI 공통 ?�자???�스??규격 검??
# 4. Checkstyle ?�적 분석 �?Spotless 코드 ?�맷???��???검??
# 5. Gradle ?�스?? JaCoCo 커버리�? �??�체 빌드 검??
# ==============================================================================

set -eo pipefail

BOLD="\033[1m"
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
BLUE="\033[34m"
RESET="\033[0m"

echo -e "${BLUE}======================================================"${RESET}
echo -e "${BLUE} ?? [Test Harness] 로컬 CI ?�합 검�??�드�?루프 ?�작"${RESET}
echo -e "${BLUE}======================================================"${RESET}

# ?�행??gradle 명령???�택 (gradlew ?�크립트 존재 ?�인)
GRADLE_CMD="./gradlew"
if [ ! -f "$GRADLE_CMD" ]; then
    GRADLE_CMD="gradle"
fi

# Step 1: Flyway ?�일�?린팅
echo -e "\n${YELLOW}?�� [1/5] Flyway 마이그레?�션 ?�일�?규격 검??..${RESET}"
if bash scripts/lint-flyway-filenames.sh; then
    echo -e "${GREEN}  ??Flyway ?�일�?규격 검???�과${RESET}"
else
    echo -e "${RED}  ??[?�드�? Flyway ?�일�?규격 ?�반! 'src/main/resources/db/migration/' ?�일명을 ?�정?�세??${RESET}"
    exit 1
fi

# Step 2: Flyway DDL 린팅
echo -e "\n${YELLOW}?�� [2/5] Flyway 마이그레?�션 DDL 규격 검??..${RESET}"
if bash scripts/lint-flyway-ddl.sh; then
    echo -e "${GREEN}  ??Flyway DDL 규격 검???�과${RESET}"
else
    echo -e "${RED}  ??[?�드�? Flyway DDL 규격 ?�반! SQL ??불리???�약조건/ALTER 문법???�인?�세??${RESET}"
    exit 1
fi

# Step 3: UI ?�자???�스??린팅
echo -e "\n${YELLOW}?�� [3/5] UI 공통 ?�자???�스??규격 검??..${RESET}"
if bash scripts/lint-design-system.sh; then
    echo -e "${GREEN}  ??UI 공통 ?�자???�스??검???�과${RESET}"
else
    echo -e "${RED}  ??[?�드�? UI ?�자???�스??규격 ?�반! HTML ?�일 ??design-system.css ?�동 ?��?�??�인?�세??${RESET}"
    exit 1
fi

# Step 4: Checkstyle ?�적 분석 �?Spotless ?�맷??검??
echo -e "\n${YELLOW}?�� [4/5] Checkstyle ?�적 분석 �?Spotless 코드 ?�맷??검??..${RESET}"
if $GRADLE_CMD checkstyleMain checkstyleTest spotlessCheck --quiet; then
    echo -e "${GREEN}  ??Checkstyle ?�적 분석 �?코드 ?�맷??Spotless) 검???�과${RESET}"
else
    echo -e "${RED}  ??[?�드�? Checkstyle ?�적 분석 ?�는 Spotless ?�맷???�반! 코드 컨벤?�을 준?�하?�록 ?�제?�세??${RESET}"
    exit 1
fi

# Step 5: Gradle ?�스?? JaCoCo 커버리�? & 빌드
echo -e "\n${YELLOW}?�� [5/5] Gradle ?�스?�·커버리지 �??�키?�처/?�위 검??..${RESET}"
if $GRADLE_CMD test jacocoTestReport jacocoTestCoverageVerification; then
    echo -e "${GREEN}  ???�스?? 커버리�? �?빌드 검???�과${RESET}"
else
    echo -e "${RED}  ??[?�드�? ?�스???�는 JaCoCo 커버리�? 검�??�패!${RESET}"
    echo -e "${RED}  ?�� AI ?�이?�트 ?��? 치유 ?? ???�택 ?�레?�스 ?�러 로그�??�고 코드�?보정?????�시 구동?�세??${RESET}"
    exit 1
fi

echo -e "\n${GREEN}======================================================"${RESET}
echo -e "${GREEN} ?�� [Test Harness] 모든 검�??�과! ?�전?�게 커밋/PR 가?�합?�다."${RESET}
echo -e "${GREEN}======================================================"${RESET}
exit 0
