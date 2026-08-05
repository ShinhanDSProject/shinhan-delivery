#!/usr/bin/env bash

# 에러 발생 시 즉시 중단
set -euo pipefail

MIGRATION_DIR="src/main/resources/db/migration"
BASE_REF="${GITHUB_BASE_REF:-main}"

echo "🔍 MariaDB 무중단 Online DDL 규격 검사 시작: $MIGRATION_DIR"

if [ ! -d "$MIGRATION_DIR" ]; then
  echo "⚠️ 마이그레이션 디렉토리가 존재하지 않습니다. 검사를 건너뜁니다."
  exit 0
fi

# 이미 base 브랜치에 병합되어 적용된 마이그레이션은 검사 대상에서 제외합니다.
# (Flyway는 이미 적용된 마이그레이션 파일의 수정을 금지하므로, 뒤늦게 온라인 DDL
#  옵션을 추가하려고 파일을 고치면 체크섬이 깨져 오히려 배포가 막힙니다.)
NEW_FILES_ONLY=1
if ! git fetch --depth=1 origin "$BASE_REF" >/dev/null 2>&1; then
  echo "⚠️ origin/$BASE_REF 를 가져올 수 없어 전체 마이그레이션 파일을 검사합니다."
  NEW_FILES_ONLY=0
fi

declare -A TARGET_FILES
if [ "$NEW_FILES_ONLY" -eq 1 ]; then
  while IFS= read -r added_file; do
    [ -n "$added_file" ] && TARGET_FILES["$(basename "$added_file")"]=1
  done < <(git diff --name-only --diff-filter=A "origin/$BASE_REF...HEAD" -- "$MIGRATION_DIR" 2>/dev/null || true)
fi

INVALID_COUNT=0

# SQL 파일 탐색 (파일이 없을 경우 대비해 nullglob 활성화)
shopt -s nullglob
for file_path in "$MIGRATION_DIR"/*; do
  filename=$(basename "$file_path")

  if [ -d "$file_path" ] || [[ ! "$filename" =~ \.sql$ ]]; then
    continue
  fi

  if [ "$NEW_FILES_ONLY" -eq 1 ] && [ -z "${TARGET_FILES[$filename]:-}" ]; then
    continue
  fi

  # 세미콜론(;)을 구분자로 사용하여 여러 줄로 작성된 개별 쿼리문 단위로 분할하여 읽어들입의다.
  # 이렇게 하면 줄바꿈 쿼리문 및 쿼리 윗줄의 주석이 하나의 단일 문자열 버퍼에 포함됩니다.
  while IFS= read -r -d ';' statement || [[ -n "$statement" ]]; do
    # 공백 제거 및 대문자 변환
    upper_statement=$(echo "$statement" | tr '[:lower:]' '[:upper:]')

    # 해당 쿼리 블록 내에 우회 주석이 포함되어 있다면 검사를 건너뜁니다.
    if [[ "$upper_statement" == *"LINTER:IGNORE-ONLINE-DDL"* ]] || [[ "$upper_statement" == *"SKIP-DDL-CHECK"* ]]; then
      continue
    fi

    # ALTER TABLE, CREATE INDEX, DROP INDEX 키워드가 포함되어 있는지 정밀 탐색
    if [[ "$upper_statement" =~ ALTER[[:space:]]+TABLE || "$upper_statement" =~ CREATE[[:space:]]+INDEX || "$upper_statement" =~ DROP[[:space:]]+INDEX ]]; then
      # ALGORITHM=INPLACE와 LOCK=NONE 옵션이 둘 다 명시되어 있는지 확인
      if [[ "$upper_statement" == *"ALGORITHM=INPLACE"* ]] && [[ "$upper_statement" == *"LOCK=NONE"* ]]; then
        continue
      else
        echo "❌ LOCK RISK DETECTED: 무중단 DDL 규격 위배"
        echo "   📍 파일명: $filename"
        # 출력 시 너무 긴 공백이나 개행은 정돈하여 표출
        cleaned_statement=$(echo "$statement" | xargs)
        echo "   📝 쿼리 내용: $cleaned_statement"
        echo "   💡 해결 가이드:"
        echo "      대용량 테이블 DDL 시 락(Lock)으로 인한 서비스 장애를 막기 위해 구문 끝에 ', ALGORITHM=INPLACE, LOCK=NONE;'을 추가해 주세요."
        echo "      (만약 해당 DDL 작업이 INPLACE 알고리즘을 지원하지 않는 경우, 쿼리 바로 윗줄이나 옆에 '-- linter:ignore-online-ddl' 주석을 달아 이 경고를 우회할 수 있습니다.)"
        echo ""
        INVALID_COUNT=$((INVALID_COUNT + 1))
      fi
    fi
  done < "$file_path"
done

if [ "$INVALID_COUNT" -gt 0 ]; then
  echo "❌ Online DDL Check Failed: 총 $INVALID_COUNT 개의 락 위험 구문이 발견되었습니다!"
  echo "📝 올바른 예시:"
  echo "   - ALTER TABLE member ADD COLUMN age INT, ALGORITHM=INPLACE, LOCK=NONE;"
  echo "   - CREATE INDEX idx_member_email ON member(email), ALGORITHM=INPLACE, LOCK=NONE;"
  exit 1
else
  echo "✅ 모든 DDL 구문이 무중단 규격을 준수하고 있습니다!"
  exit 0
fi
