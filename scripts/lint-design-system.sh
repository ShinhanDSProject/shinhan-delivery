#!/usr/bin/env bash

# ==============================================================================
# UI ê³µí†µ ?”ì???œìŠ¤??ì¤€???¬ë? ?ë™ ê²€ì¦??˜ë„¤??(lint-design-system.sh)
# ==============================================================================
# 1. src/main/resources/static ?˜ìœ„ ëª¨ë“  HTML ?Œì¼??/css/design-system.css ?¬í•¨ ?¬ë? ê²€??
# ==============================================================================

set -eo pipefail

BOLD="\033[1m"
GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
RESET="\033[0m"

STATIC_DIR="src/main/resources/static"
ERRORS=0

echo -e "?” ê³µí†µ ?”ì???œìŠ¤??ë¦°íŒ… ê²€???œì‘: ${STATIC_DIR}"

if [ ! -d "$STATIC_DIR" ]; then
    echo -e "${GREEN}  ??static ?”ë ‰? ë¦¬ê°€ ì¡´ì¬?˜ì? ?Šìœ¼ë¯€ë¡?ê²€?¬ë? ?µê³¼?©ë‹ˆ??${RESET}"
    exit 0
fi

# 1. static ?˜ìœ„ HTML ?Œì¼??design-system.css ?°ë™ ?¬ë? ê²€??
for html_file in $(find "$STATIC_DIR" -type f -name "*.html"); do
    if ! grep -q "design-system.css" "$html_file"; then
        echo -e "${RED}  ??[?”ì???œìŠ¤??ë¯¸ë¹„] ${html_file} ?Œì¼??'design-system.css' ?°ë™???„ë½?˜ì—ˆ?µë‹ˆ??${RESET}"
        ERRORS=$((ERRORS + 1))
    fi
done

if [ $ERRORS -gt 0 ]; then
    echo -e "${RED}??ì´?${ERRORS}ê°œì˜ ?”ì???œìŠ¤??ê·œê²© ?„ë°˜ ?¬í•­??ë°œê²¬?˜ì—ˆ?µë‹ˆ??${RESET}"
    exit 1
fi

echo -e "${GREEN}??ëª¨ë“  HTML ?”ë©´??ê³µí†µ ?”ì???œìŠ¤??ê·œê²©??100% ì¤€?˜í•˜ê³??ˆìŠµ?ˆë‹¤!${RESET}"
exit 0
