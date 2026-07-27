# 신한 개척자 (shinhan-gaecheokja)

이 프로젝트는 Spring Boot 기반 백엔드 애플리케이션입니다.

---

## 🚀 빠른 시작 (Quick Start)

### 1. 로컬 환경 설정 (.env)
로컬 데이터베이스 연결 설정을 위해 프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 아래 내용을 입력합니다.
*(주의: `.env` 파일은 절대 Git에 커밋되지 않도록 제외처리 되어 있습니다.)*

```env
# 로컬 MariaDB 설정
DB_URL=jdbc:mariadb://localhost:3306/shinhan_gaecheokja
DB_USER=root
DB_PASSWORD=your_password_here
```

### 2. 애플리케이션 실행
아래 명령어를 사용하여 애플리케이션을 기동합니다.
```bash
./gradlew bootRun
```

💡 *실행 중 에러가 발생하거나 연결이 되지 않는다면 [**로컬 개발 트러블슈팅 가이드 (docs/troubleshooting.md)**](./docs/troubleshooting.md)를 참고해 주세요.*

👉 [**로컬 개발 환경 및 자동화 도구 사용 가이드 바로가기 (docs/developer-env-guide.md)**](./docs/developer-env-guide.md)

---

## 🗄️ 데이터베이스 형상 관리 (Database Migrations)

이 프로젝트는 데이터베이스 스키마 버전 관리를 위해 **Flyway**를 사용합니다. 
자세한 마이그레이션 적용 규칙, 네이밍 컨벤션 및 유의사항 등은 아래 상세 문서를 참고해 주세요.

👉 [**Flyway 데이터베이스 마이그레이션 가이드 바로가기 (docs/flyway-guide.md)**](./docs/flyway-guide.md)

---

## 🤝 협업 규칙 (Collaboration Rules)

원활한 공동 작업과 안정적인 배포 흐름을 위해 정의한 규칙들입니다. 작업 진행 전 아래 가이드를 준수해 주세요.

👉 [**Git Flow 및 커밋 컨벤션 가이드 바로가기 (docs/git-flow-guide.md)**](./docs/git-flow-guide.md)
👉 [**기능 개발 전 설계 단계 프로세스 가이드 바로가기 (docs/design-phase-guide.md)**](./docs/design-phase-guide.md)
