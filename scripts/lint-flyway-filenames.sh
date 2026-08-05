#!/usr/bin/env bash

# 에러 발생 시 즉시 중단
set -euo pipefail

MIGRATION_DIR="src/main/resources/db/migration"

echo "🔍 Flyway 마이그레이션 파일명 규격 검사 시작: $MIGRATION_DIR"

if [ ! -d "$MIGRATION_DIR" ]; then
  echo "⚠️ 마이그레이션 디렉토리가 존재하지 않습니다. 검사를 건너뜁니다."
  exit 0
fi

INVALID_COUNT=0

# SQL 파일 탐색 (파일이 없을 경우 대비해 nullglob 활성화)
shopt -s nullglob
for file_path in "$MIGRATION_DIR"/*; do
  filename=$(basename "$file_path")
  
  if [ -d "$file_path" ]; then
    continue
  fi

  # 정규식 검사 규칙:
  # 1. 버전 관리형: V{숫자}.{숫자}__{설명}.sql (대문자 V로 시작, 숫자가 오고, 반드시 언더바 2개 '__'가 포함되어야 하며, 영어/숫자/언더바 설명 뒤 .sql로 마쳐야 함)
  # 2. 반복형: R__{설명}.sql (대문자 R로 시작, 언더바 2개 '__' 필수)
  if [[ "$filename" =~ ^V[0-9]+(\.[0-9]+)*__[a-zA-Z0-9_]+\.sql$ ]] || [[ "$filename" =~ ^R__[a-zA-Z0-9_]+\.sql$ ]]; then
    continue
  else
    echo "❌ 올바르지 않은 파일명 감지: $filename"
    echo "   📍 파일 경로: $file_path"
    
    # 교육생을 위한 친절한 원인 분석 서비스
    if [[ ! "$filename" =~ \.sql$ ]]; then
      echo "   💡 원인 분석: 확장자는 반드시 소문자 '.sql' 이어야 합니다."
    elif [[ "$filename" =~ ^[vV][0-9] ]]; then
      if [[ "$filename" =~ ^v ]]; then
        echo "   💡 원인 분석: 접두사 V는 반드시 대문자이어야 합니다. (소문자 v 불허)"
      fi
      if [[ ! "$filename" =~ __ ]]; then
        echo "   💡 원인 분석: 언더바가 부족합니다. Flyway 규격상 버전 번호와 설명 사이에는 반드시 TWO underscores ('__')가 들어가야 합니다."
      fi
    else
      echo "   💡 원인 분석: 파일명은 반드시 'V{버전}__{설명}.sql' 또는 'R__{설명}.sql' 형식으로 작성되어야 합니다."
    fi
    echo ""
    INVALID_COUNT=$((INVALID_COUNT + 1))
  fi
done

if [ "$INVALID_COUNT" -gt 0 ]; then
  echo "❌ Flyway 파일명 규칙 검사 실패: 총 $INVALID_COUNT 개의 잘못된 파일명이 발견되었습니다!"
  echo "📝 올바른 예시:"
  echo "   - V1__init_schema.sql (버전 1)"
  echo "   - V2.1__add_new_column.sql (버전 2.1)"
  echo "   - R__recreate_view.sql (반복 마이그레이션)"
  exit 1
else
  echo "✅ 모든 Flyway 마이그레이션 파일명 규격이 올바릅니다!"
  exit 0
fi
