#!/usr/bin/env bash

# 에러 발생 시 즉시 중단
set -euo pipefail

MIGRATION_DIR="src/main/resources/db/migration"

echo "🔍 MariaDB 무중단 Online DDL 규격 검사 시작: $MIGRATION_DIR"

if [ ! -d "$MIGRATION_DIR" ]; then
  echo "⚠️ 마이그레이션 디렉토리가 존재하지 않습니다. 검사를 건너뜁니다."
  exit 0
fi

INVALID_COUNT=0

# SQL 파일 탐색 (파일이 없을 경우 대비해 nullglob 활성화)
shopt -s nullglob
for file_path in "$MIGRATION_DIR"/*; do
  filename=$(basename "$file_path")
  
  if [ -d "$file_path" ] || [[ ! "$filename" =~ \.sql$ ]]; then
    continue
  fi

  LINE_NUMBER=0
  # 파일 내용 라인 단위 순회
  while IFS= read -r line || [[ -n "$line" ]]; do
    LINE_NUMBER=$((LINE_NUMBER + 1))
    
    # 공백 제거 및 대문자 변환 후 DDL 관련 키워드 탐색
    upper_line=$(echo "$line" | tr '[:lower:]' '[:upper:]')
    
    # 우회 주석이 한 줄에 들어있는 경우 건너뜀 (예: -- linter:ignore-online-ddl)
    if [[ "$upper_line" == *"--"*"LINTER:IGNORE-ONLINE-DDL"* ]] || [[ "$upper_line" == *"--"*"SKIP-DDL-CHECK"* ]]; then
      continue
    fi

    # ALTER TABLE, CREATE INDEX, DROP INDEX 키워드가 들어있는 라인 검출
    if [[ "$upper_line" =~ ALTER[[:space:]]+TABLE || "$upper_line" =~ CREATE[[:space:]]+INDEX || "$upper_line" =~ DROP[[:space:]]+INDEX ]]; then
      # ALGORITHM=INPLACE와 LOCK=NONE 옵션이 둘 다 명시되어 있는지 확인
      if [[ "$upper_line" == *"ALGORITHM=INPLACE"* ]] && [[ "$upper_line" == *"LOCK=NONE"* ]]; then
        # 통과
        continue
      else
        echo "❌ LOCK RISK DETECTED: 무중단 DDL 규격 위배"
        echo "   📍 파일명: $filename (Line: $LINE_NUMBER)"
        echo "   📝 쿼리 내용: $line"
        echo "   💡 해결 가이드:"
        echo "      대용량 테이블 DDL 시 락(Lock)으로 인한 서비스 장애를 막기 위해 구문 끝에 ', ALGORITHM=INPLACE, LOCK=NONE;'을 추가해 주세요."
        echo "      (만약 해당 DDL 작업이 INPLACE 알고리즘을 지원하지 않는 경우, 쿼리 윗줄이나 옆에 '-- linter:ignore-online-ddl' 주석을 달아 이 경고를 우회할 수 있습니다.)"
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
