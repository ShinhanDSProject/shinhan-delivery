# AGENTS.md

이 저장소에서 코드를 작성하는 모든 AI 에이전트(Claude Code, Cursor, Copilot, Antigravity 등)는 아래 지침과 하네스 검증 규칙을 준수해야 합니다.

## 1. 코딩 컨벤션 지침
* 모든 코드 작성 및 수정 전 `code-convention.md`와 `CLAUDE.md`를 필독하세요.
* 의존성 방향은 항상 단방향(`Controller -> Service -> Repository`)을 유지해야 합니다.
* Controller는 DTO만 다루며 Entity를 직접 반환해서는 안 됩니다.

## 2. AI Pre-Flight Self-Review & Test Harness 자가 치유 피드백 루프
작업을 완료하거나 `./pr`을 구동하기 전에 **반드시 아래 2단계 사전 검토를 완료해야 합니다**:

1. **AI 사전 셀프 코드 리뷰 (Pre-Flight Self-Review):**
   - 수정한 코드에 미사용 import, 불필요한 `System.out.println` 콘솔 출력, 명명 규칙 위반, 주석 누락이 없는지 스스로 1차 검토 후 보정하세요.
2. **Test Harness 자가 치유 피드백 루프:**
   - `./scripts/verify.sh` (또는 `./pr`)를 실행하여 린트, 코드 포맷팅(Spotless), ArchUnit 아키텍처 규칙, 단위 테스트 실패 시 에러 로그를 읽고 수초 내로 자가 치유(Auto-Fix)하여 0 exit code 상태를 확보하세요.
3. **Multi-Pass Audit 피드백 루프:**
   - 1차 검증을 통과했더라도 "크로스 플랫폼 지원, 예외 케이스 처리, 가독성/확장성 측면에서 보완할 점이 없는지" 2차, 3차 다각도로 재검토하여 완성도를 100% 확보하세요.
