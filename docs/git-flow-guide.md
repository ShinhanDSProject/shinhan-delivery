# Git Flow 협업 가이드라인

이 프로젝트는 효율적인 협업과 안정적인 배포를 위해 **Git Flow** 브랜치 전략과 **Conventional Commits** 규칙을 따릅니다. 

---

## 1. 브랜치 전략 (Branch Strategy)

프로젝트는 크게 5가지 유형의 브랜치로 나누어 관리합니다.

| 브랜치 유형 | 설명 | 이름 규칙 | 대상 상위 브랜치 |
| :--- | :--- | :--- | :--- |
| **`main`** | 제품으로 출시될 수 있는 가장 안정적인 배포용 브랜치 | `main` | - |
| **`develop`** | 다음 버전을 위한 기능들이 모이는 핵심 개발 브랜치 | `develop` | `main` |
| **`feature`** | 신규 기능 개발 또는 버그 수정을 진행하는 작업 브랜치 | `feature/<이슈번호>-<요약>` 또는 `feat/<요약>` | `develop` |
| **`release`** | 배포를 준비하며 버그 수정 및 최종 QA를 수행하는 브랜치 | `release/<버전>` (예: `release/1.0.0`) | `develop` |
| **`hotfix`** | 배포된 실서버(`main`)에 발생한 긴급 장애를 패치하는 브랜치 | `hotfix/<이슈번호>-<요약>` | `main` |

---

## 2. 기본 작업 프로세스 (Work Process)

신규 기능을 개발할 때 진행하는 표준 작업 순서입니다.

```mermaid
gitGraph
    commit id: "Init"
    branch develop
    checkout develop
    commit id: "v0.1.0"
    branch feature/login
    checkout feature/login
    commit id: "feat: UI"
    commit id: "feat: API"
    checkout develop
    merge feature/login id: "Merge PR"
    checkout main
    merge develop id: "Release v1.0.0"
```

1. **로컬 최신화**
   * 작업을 시작하기 전, `develop` 브랜치를 원격 저장소(`origin`) 기준으로 최신화합니다.
     ```bash
     git checkout develop
     git pull origin develop
     ```
2. **작업 브랜치 생성**
   * `develop` 브랜치로부터 작업 성격에 맞게 브랜치를 분기합니다.
     ```bash
     git checkout -b feature/123-user-login
     ```
3. **코드 개발 및 로컬 커밋**
   * 작업 단위별로 잘게 나누어 커밋 컨벤션에 맞춰 커밋합니다.
4. **원격 저장소 푸시 및 Pull Request(PR) 생성**
   * 작업을 마치면 원격에 푸시한 뒤, GitHub에서 **`develop` 브랜치를 대상**으로 PR을 생성합니다.
     ```bash
     git push -u origin feature/123-user-login
     ```
5. **코드 리뷰 및 머지(Merge)**
   * 팀원들의 코드 리뷰를 거친 뒤 승인(Approve)을 받으면 `develop` 브랜치로 머지합니다.
   * 머지 후 로컬 작업 브랜치는 삭제합니다.

---

## 3. 커밋 메시지 규칙 (Commit Message Convention)

명확하고 일관된 이력 관리를 위해 **Conventional Commits** 스타일을 따릅니다.

### 커밋 메시지 기본 구조
```text
<type>(<scope>): <subject>  # 제목 (최대 50자, 마침표 생략)

<body>                       # 본문 (생략 가능, 어떻게보다 '왜' 변경했는지 상세히 서술)

<footer>                     # 바닥글 (생략 가능, 연관된 이슈 번호 등 기재)
```

### 주요 태그 타입 (Type)

| 타입 | 의미 | 예시 |
| :--- | :--- | :--- |
| **`feat`** | 새로운 기능 추가 | `feat: 로그인 기능 구현` |
| **`fix`** | 버그 및 에러 수정 | `fix: 로그인 API 예외 처리 누락 수정` |
| **`docs`** | 문서 파일 수정 및 가이드 추가 | `docs: Git Flow 협업 가이드라인 문서 추가` |
| **`style`** | 코드 스타일 수정 (포맷팅, 세미콜론 누락 등 코드 변경 없음) | `style: 줄바꿈 및 인덴트 정리` |
| **`refactor`** | 기능 추가나 버그 수정이 없는 순수 코드 구조 리팩토링 | `refactor: 로그인 서비스 클래스 분리` |
| **`test`** | 테스트 코드 작성 및 수정 | `test: 로그인 API 단위 테스트 코드 추가` |
| **`chore`** | 빌드 설정, 의존성 패키지 관리, 설정 파일 변경 등 | `chore: build.gradle 의존성 버전 업데이트` |

### 좋은 예시
```text
feat(member): 이메일 중복 체크 API 구현

- 회원가입 프로세스에서 사용할 이메일 중복 검사 비즈니스 로직 추가
- 중복 발견 시 DuplicateMemberException 예외 발생 처리

Resolves: #45
```
