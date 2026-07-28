---
name: 🐛 Bug Report (버그 조치)
about: 발생한 에러 또는 이상 동작을 보고하고 수정하기 위한 표준 이슈 템플릿입니다.
title: '[Bug] '
labels: 'bug'
assignees: ''
---

## 📌 버그 현상 요약
* 어떤 작업 중 어떠한 에러/이상 동작이 터졌는지 명확히 기재합니다.

---

## 🔬 재현 절차 (Steps to Reproduce)
1. 요청 API: `POST /api/...`
2. 전송 Payload: `{ ... }`
3. 발생 에러 메시지 / 스택트레이스

---

## 🛠️ 조치 및 수정 방향
- [ ] 원인 분석 및 실패 테스트 작성
- [ ] 소스 코드 수정 및 하위 호환성 유지 확인

---

## ✅ 완료 정의 (Definition of Done)
- [ ] 실패 테스트 재생성 및 검증 성공
- [ ] `./scripts/verify.sh` 100% 그린 패스
