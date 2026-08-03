# 📝 프로젝트 회의록 관리 가이드 (Meeting Minutes)

본 디렉토리(`docs/meeting-minutes/`)는 **신한 개척자** 프로젝트의 회의록 및 아키텍처/비즈니스 결정 사항을 **날짜 기반**으로 관리하는 공간입니다.

---

## 📌 1. 파일명 작성 규격 (Naming Convention)

모든 회의록 파일은 다음과 같은 **날짜 기반 파일명**으로 작성합니다.

- **형식:** `YYYY-MM-DD-<주제-키워드>.md`
- **예시:**
  - `2026-08-03-project-progress-meeting.md`
  - `2026-08-10-sprint-3-retrospective.md`
  - `2026-08-17-architecture-review.md`

---

## 📋 2. 회의록 표준 양식 (Meeting Template)

회의록 작성 시 아래 양식을 복사하여 사용합니다.

```markdown
# 📝 [YYYY-MM-DD] <회의 주제>

> **회의 일시:** YYYY년 MM월 DD일 HH:MM ~ HH:MM
> **참석자:** 
> **작성자 / 기록:** 
> **문서 위치:** `docs/meeting-minutes/YYYY-MM-DD-<주제>.md`

---

## 📌 1. 회의 목적 (Purpose)

---

## 💬 2. 주요 논의 내용 (Discussion Highlights)

---

## 💡 3. 결정 사항 및 액션 아이템 (Decisions & Action Items)

| 항목 | 내용 | 담당자 | 기한 |
| :--- | :--- | :--- | :--- |
| **Action 1** | [할 일 내용] | [담당자] | YYYY-MM-DD |

---

## 📎 4. 관련 이슈 & PR 링크
* 관련 이슈: #이슈번호
```

---

## 🛡️ 3. 관리 원칙 (Rules)

1. **SSOT (Single Source of Truth) 준수:** 중복 기록 없이 회의록 디렉토리에 일원화하여 관리합니다.
2. **Git 마이크로 커밋:** 회의 종료 후 즉시 본 디렉토리에 마크다운으로 작성하여 커밋 및 PR을 올립니다.
