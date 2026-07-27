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

# 로컬 테스트용 더미 데이터(회원, 지갑, 차량 등) 자동 적재 여부 (true/false)
DATA_SEED_ENABLED=true
```

### 2. 애플리케이션 실행
아래 명령어를 사용하여 애플리케이션을 기동합니다.
```bash
./gradlew bootRun
```

💡 *실행 중 에러가 발생하거나 연결이 되지 않는다면 [**로컬 개발 트러블슈팅 가이드 (docs/troubleshooting.md)**](./docs/troubleshooting.md)를 참고해 주세요.*

### 3. Git 커밋 템플릿 설정 (.gitmessage)
협업 규칙에 따른 일관된 커밋 작성을 위해 아래 명령어로 로컬 커밋 템플릿을 등록해 주세요. 등록 후 `git commit` 실행 시 버퍼 창에 템플릿 힌트가 자동으로 채워집니다.
```bash
git config --local commit.template .gitmessage
```

👉 [**로컬 개발 환경 및 자동화 도구 사용 가이드 바로가기 (docs/developer-env-guide.md)**](./docs/developer-env-guide.md)

---

## 📚 프로젝트 문서 인덱스 (Documentation Index)

프로젝트에 구축된 모든 개발 가이드라인과 기존 개발 기능들에 대한 요구사항 명세서 및 설계서의 전체 맵입니다. 
아래 링크를 클릭하여 해당하는 가이드 및 설계 내용을 확인하실 수 있습니다.

### 🛠️ 개발 가이드 및 협업 규칙
* [**초보 개발자 온보딩 및 기능 개발 로드맵 (docs/onboarding-roadmap.md)**](./docs/onboarding-roadmap.md) - 입문자를 위한 필수 학습 순서 및 실전 기능 개발 7단계 흐름 가이드 🚀
* [**협업 문화 및 자동화 도구 도입 배경 가이드 (docs/development-culture-guide.md)**](./docs/development-culture-guide.md) - 왜 이런 협업 규칙과 DevOps 도구들을 도입했는지, 미도입 시 어떤 장애 참사가 발생하는지 설명해 주는 입문자 필독서 🎓
* [**Git Flow 및 커밋 컨벤션 가이드 (docs/git-flow-guide.md)**](./docs/git-flow-guide.md) - 브랜치 운용 규칙, Conventional Commits 커밋 헤더 태그 가이드 및 자동 코드 리뷰 연동 규칙
* [**기능 개발 전 설계 단계 프로세스 가이드 (docs/design-phase-guide.md)**](./docs/design-phase-guide.md) - 기능 개발에 착수하기 전 작성해야 할 4대 핵심 산출물 양식과 2단계 PR 전략
* [**로컬 개발 환경 및 자동화 도구 사용 가이드 (docs/developer-env-guide.md)**](./docs/developer-env-guide.md) - Spotless 포맷 자동 가공 명령어, Swagger UI, 로컬 테스트용 더미 데이터 설정
* [**Flyway 데이터베이스 마이그레이션 가이드 (docs/flyway-guide.md)**](./docs/flyway-guide.md) - Flyway 스크립트 작성 규칙, JPA Buddy 플러그인을 활용한 마이그레이션 방법
* [**로컬 개발 트러블슈팅 가이드 (docs/troubleshooting.md)**](./docs/troubleshooting.md) - Flyway 해시 충돌, 포트 선점, 데이터베이스 권한 에러 해결 가이드

### 📝 기존 개발 기능 요구사항 명세서 및 설계서 (Reference)
* [**회원 및 인증 설계서 (docs/design/member-auth-design.md)**](./docs/design/member-auth-design.md) - 회원가입, 중복 가입 방지 예외, 암호화 저장, 상세 조회 API 명세 및 ERD
* [**차량 등록 및 조회 설계서 (docs/design/vehicle-design.md)**](./docs/design/vehicle-design.md) - 차량 사양 유효 검증, 소유주 정보 매핑, 가용성 조회 API 명세 및 ERD
* [**배송 요청 및 단건 조회 설계서 (docs/design/delivery-request-design.md)**](./docs/design/delivery-request-design.md) - 출발/목적지, 화물 무게 및 배송 상태 API 명세 및 ERD
* [**배송 매칭 설계서 (docs/design/matching-design.md)**](./docs/design/matching-design.md) - 수동 배송 매칭, 상태 수정/삭제에 따른 실시간 리소스 데이터 동기화 API 명세 및 ERD
* [**포인트 지갑 및 결제 설계서 (docs/design/point-wallet-design.md)**](./docs/design/point-wallet-design.md) - 지갑 개설, 충전, 차감 검증 및 잔액 에러 처리 API 명세 및 ERD
