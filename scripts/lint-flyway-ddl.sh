#!/usr/bin/env bash

# ?ëŸ¬ ë°œìƒ ??ì¦‰ì‹œ ì¤‘ë‹¨
set -euo pipefail

MIGRATION_DIR="src/main/resources/db/migration"

echo "?” MariaDB ë¬´ì¤‘??Online DDL ê·œê²© ê²€???œì‘: $MIGRATION_DIR"

if [ ! -d "$MIGRATION_DIR" ]; then
  echo "? ï¸ ë§ˆì´ê·¸ë ˆ?´ì…˜ ?”ë ‰? ë¦¬ê°€ ì¡´ì¬?˜ì? ?ŠìŠµ?ˆë‹¤. ê²€?¬ë? ê±´ë„ˆ?ë‹ˆ??"
  exit 0
fi

INVALID_COUNT=0

# SQL ?Œì¼ ?ìƒ‰ (?Œì¼???†ì„ ê²½ìš° ?€ë¹„í•´ nullglob ?œì„±??
shopt -s nullglob
for file_path in "$MIGRATION_DIR"/*; do
  filename=$(basename "$file_path")
  
  if [ -d "$file_path" ] || [[ ! "$filename" =~ \.sql$ ]]; then
    continue
  fi

  # ?¸ë?ì½œë¡ (;)??êµ¬ë¶„?ë¡œ ?¬ìš©?˜ì—¬ ?¬ëŸ¬ ì¤„ë¡œ ?‘ì„±??ê°œë³„ ì¿¼ë¦¬ë¬??¨ìœ„ë¡?ë¶„í• ?˜ì—¬ ?½ì–´?¤ì…?˜ë‹¤.
  # ?´ë ‡ê²??˜ë©´ ì¤„ë°”ê¿?ì¿¼ë¦¬ë¬?ë°?ì¿¼ë¦¬ ?—ì¤„??ì£¼ì„???˜ë‚˜???¨ì¼ ë¬¸ì??ë²„í¼???¬í•¨?©ë‹ˆ??
  while IFS= read -r -d ';' statement || [[ -n "$statement" ]]; do
    # ê³µë°± ?œê±° ë°??€ë¬¸ì ë³€??
    upper_statement=$(echo "$statement" | tr '[:lower:]' '[:upper:]')

    # ?´ë‹¹ ì¿¼ë¦¬ ë¸”ë¡ ?´ì— ?°íšŒ ì£¼ì„???¬í•¨?˜ì–´ ?ˆë‹¤ë©?ê²€?¬ë? ê±´ë„ˆ?ë‹ˆ??
    if [[ "$upper_statement" == *"LINTER:IGNORE-ONLINE-DDL"* ]] || [[ "$upper_statement" == *"SKIP-DDL-CHECK"* ]]; then
      continue
    fi

    # ALTER TABLE, CREATE INDEX, DROP INDEX ?¤ì›Œ?œê? ?¬í•¨?˜ì–´ ?ˆëŠ”ì§€ ?•ë? ?ìƒ‰
    if [[ "$upper_statement" =~ ALTER[[:space:]]+TABLE || "$upper_statement" =~ CREATE[[:space:]]+INDEX || "$upper_statement" =~ DROP[[:space:]]+INDEX ]]; then
      # ALGORITHM=INPLACE?€ LOCK=NONE ?µì…˜??????ëª…ì‹œ?˜ì–´ ?ˆëŠ”ì§€ ?•ì¸
      if [[ "$upper_statement" == *"ALGORITHM=INPLACE"* ]] && [[ "$upper_statement" == *"LOCK=NONE"* ]]; then
        continue
      else
        echo "??LOCK RISK DETECTED: ë¬´ì¤‘??DDL ê·œê²© ?„ë°°"
        echo "   ?“ ?Œì¼ëª? $filename"
        # ì¶œë ¥ ???ˆë¬´ ê¸?ê³µë°±?´ë‚˜ ê°œí–‰?€ ?•ëˆ?˜ì—¬ ?œì¶œ
        cleaned_statement=$(echo "$statement" | xargs)
        echo "   ?“ ì¿¼ë¦¬ ?´ìš©: $cleaned_statement"
        echo "   ?’¡ ?´ê²° ê°€?´ë“œ:"
        echo "      ?€?©ëŸ‰ ?Œì´ë¸?DDL ????Lock)?¼ë¡œ ?¸í•œ ?œë¹„???¥ì• ë¥?ë§‰ê¸° ?„í•´ êµ¬ë¬¸ ?ì— ', ALGORITHM=INPLACE, LOCK=NONE;'??ì¶”ê???ì£¼ì„¸??"
        echo "      (ë§Œì•½ ?´ë‹¹ DDL ?‘ì—…??INPLACE ?Œê³ ë¦¬ì¦˜??ì§€?í•˜ì§€ ?ŠëŠ” ê²½ìš°, ì¿¼ë¦¬ ë°”ë¡œ ?—ì¤„?´ë‚˜ ?†ì— '-- linter:ignore-online-ddl' ì£¼ì„???¬ì•„ ??ê²½ê³ ë¥??°íšŒ?????ˆìŠµ?ˆë‹¤.)"
        echo ""
        INVALID_COUNT=$((INVALID_COUNT + 1))
      fi
    fi
  done < "$file_path"
done

if [ "$INVALID_COUNT" -gt 0 ]; then
  echo "??Online DDL Check Failed: ì´?$INVALID_COUNT ê°œì˜ ???„í—˜ êµ¬ë¬¸??ë°œê²¬?˜ì—ˆ?µë‹ˆ??"
  echo "?“ ?¬ë°”ë¥??ˆì‹œ:"
  echo "   - ALTER TABLE member ADD COLUMN age INT, ALGORITHM=INPLACE, LOCK=NONE;"
  echo "   - CREATE INDEX idx_member_email ON member(email), ALGORITHM=INPLACE, LOCK=NONE;"
  exit 1
else
  echo "??ëª¨ë“  DDL êµ¬ë¬¸??ë¬´ì¤‘??ê·œê²©??ì¤€?˜í•˜ê³??ˆìŠµ?ˆë‹¤!"
  exit 0
fi
