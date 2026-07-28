# 🏛️ 프로젝트 문서 단일 원본 관리(SSOT) 정책 가이드북

이 문서는 `shinhan-gaecheokja` 프로젝트의 **모든 지식 자산, 가이드북, 규격 문서를 작성할 때 적용되는 단일 원본 관리(SSOT - Single Source of Truth) 시스템화 규격**입니다.

---

## 📌 1. SSOT (Single Source of Truth)란 무엇이며 왜 필수인가요?

프로젝트 규모가 커지고 문서의 양이 많아질수록 **동일한 수칙이 여러 문서에 중복 작성(Duplicate Copy)**되는 현상이 발생합니다.  
규칙이 변경되었을 때 일부 문서만 수정되면 **문서 간 내용 불일치(Documentation Drift)**가 발생하여 개발자와 AI가 어떤 규칙이 진짜인지 혼란을 겪게 됩니다.

💡 **해결책 (SSOT 철칙):**
* 프로젝트 내 **모든 지식/규칙은 오직 단 하나의 전담 문서만 원본(Primary Owner)으로 갖는다.**
* 파생 문서(가이드북, 체크리스트, README 등)에서는 동일 내용을 재작성하지 않고, **원본 문서의 마크다운 링크로 참조(Cross-Reference)만 집행**한다.

---

## 📐 2. SSOT 문서 작성 3대 철칙 (Core Rules)

1. **Rule 1 (Single Primary Owner):** 지식 영역별로 원본을 관리하는 단 하나의 정식 문서만 지정을 허용합니다.
2. **Rule 2 (Zero Redundancy):** 파생 가이드북이나 PR 본문에 원본 규약의 구체적 내용을 2번 이상 복사/재작성(Duplicate Copy)하는 행위를 금지합니다.
3. **Rule 3 (Cross-Link Reference Only):** 세부 규약이 필요한 경우 반드시 원본 문서의 상대 경로 마크다운 링크(`[code-convention.md](../code-convention.md)`)로 참조시킵니다.

---

## 🗺️ 3. 우리 프로젝트의 SSOT 문서 지도 (Primary SSOT Registry)

프로젝트 내 지식 영역별 **유일한 단일 원본 문서 매핑 테이블**입니다:

| 지식 영역 (Knowledge Domain) | 단일 원본 문서 (Single Primary Owner) | 파생 참조 문서 (Referencing Docs) |
| :--- | :--- | :--- |
| **코딩 규약 & 아키텍처 규칙** | [`code-convention.md`](../code-convention.md) | `AGENTS.md`, `harness-decision-framework.md` |
| **AI 에이전트 작업 8대 원칙** | [`AGENTS.md`](../AGENTS.md) | `README.md`, `code-convention.md` |
| **아키텍처 의사결정 기록 (ADR)** | [`docs/adr/README.md`](./adr/README.md) | `docs/security-jwt-guide.md`, `README.md` |
| **PR 리뷰 & 인라인 댓글 작성 규격** | [`docs/pr-review-guide.md`](./pr-review-guide.md) | `code-convention.md`, `AGENTS.md` |
| **테스트 하네스 판단 & 통제 정책** | [`docs/harness-decision-framework.md`](./harness-decision-framework.md) | `AGENTS.md`, `README.md` |
| **기능 개발 전 설계 2단계 PR 절차** | [`docs/design-phase-guide.md`](./design-phase-guide.md) | `README.md` |
| **Git Flow & 커밋 컨벤션** | [`docs/git-flow-guide.md`](./git-flow-guide.md) | `README.md` |
| **REST API 설계 규격** | [`docs/rest-api-guide.md`](./rest-api-guide.md) | `README.md` |
| **전역 예외 처리 규격** | [`docs/exception-handling-guide.md`](./exception-handling-guide.md) | `README.md` |

---

## 🛠️ 4. 신규 문서 작성 및 PR 검토 시 SSOT 체크리스트

모든 개발자와 AI 에이전트는 신규 문서를 생성하거나 수정할 때 다음 사항을 반드시 검증합니다:

- [ ] 작성하려는 내용의 단일 원본(Primary Owner) 문서가 이미 존재하지 않는가?
- [ ] 기존 원본 문서가 존재할 경우, 내용을 중복 기술하지 않고 마크다운 링크로 참조시켰는가?
- [ ] 새로운 지식 영역일 경우 `docs/ssot-documentation-policy.md` 단일 원본 지도 테이블에 정식 등록했는가?
