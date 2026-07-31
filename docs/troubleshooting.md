# 로컬 개발 트러블슈팅 가이드 (Troubleshooting)

이 가이드는 교육생들이 로컬 개발 환경을 세팅하고 실행할 때 흔히 겪는 에러 유형과 그에 대한 해결책을 정리해 둔 문서입니다. 오류가 발생하면 질문하기 전에 아래 내용을 먼저 확인해 보세요.

---

## 1. Flyway Checksum 불일치 오류
### 🚨 현상 (Error message)
```text
Migration checksum mismatch for migration version X.X
-> Applied to database : -XXXXXXXXXX
-> Resolved locally    :  YYYYYYYYYY
```

### 💡 원인 (Cause)
이미 본인 로컬 DB에 적용(실행)된 Flyway 마이그레이션 SQL 파일(예: `V1__init_schema.sql` 등)의 내용을 임의로 수정했을 때 발생합니다. Flyway는 한번 실행된 SQL 파일의 원본 해시값(Checksum)을 검증하여 파일 조작 여부를 판단하기 때문입니다.

### 🛠️ 해결책 (Solution)
* **방법 A (권장):** 마이그레이션 파일 내용을 로컬 테스트 중에 임의로 바꾸었다면, 로컬 DB의 스키마 및 데이터를 초기화(Drop & Recreate)하고 애플리케이션을 재구동하면 됩니다.
  ```sql
  -- MariaDB CLI 또는 DBeaver 등 툴에서 실행
  DROP DATABASE shinhan_gaecheokja;
  CREATE DATABASE shinhan_gaecheokja;
  ```
* **주의 사항:** 이미 협업 브랜치(`main`)에 병합되어 배포된 마이그레이션 파일은 절대 직접 수정하지 마세요. 변경 사항은 반드시 새로운 버전 번호(예: `V8__add_new_column.sql`)를 붙여 새 파일로 작성해야 합니다.

---

## 2. 8080 포트 선점 오류
### 🚨 현상 (Error message)
```text
Web server failed to start. Port 8080 was already in use.
```

### 💡 원인 (Cause)
이전 애플리케이션 프로세스가 비정상 종료되었거나, 다른 로컬 프로그램이 8080 포트를 점유하고 있는 경우입니다.

### 🛠️ 해결책 (Solution)
포트를 선점하고 있는 프로세스 ID(PID)를 찾아 종료시켜 줍니다.

#### 🍎 macOS
1. 터미널을 열고 8080 포트를 점유 중인 프로세스 조회:
   ```bash
   lsof -i :8080
   ```
2. 출력되는 결과 중 `PID` 번호를 확인한 뒤 종료 처리 (예: PID가 1234인 경우):
   ```bash
   kill -9 1234
   ```

#### 🪟 Windows (Powershell / CMD)
1. 8080 포트를 점유 중인 프로세스 조회:
   ```cmd
   netstat -ano | findstr 8080
   ```
2. 출력 줄 맨 오른쪽에 표시되는 `PID` 번호를 확인한 뒤 종료 처리 (예: PID가 5678인 경우):
   ```cmd
   taskkill /F /PID 5678
   ```

---

## 3. DB 권한/접속 실패 오류
### 🚨 현상 (Error message)
```text
java.sql.SQLNonTransientConnectionException: Could not connect to address=(host=localhost)(port=3306)
또는
java.sql.SQLException: Access denied for user 'root'@'localhost' (using password: YES)
```

### 💡 원인 (Cause)
로컬에 MariaDB가 설치되어 구동되고 있지 않거나, `.env` 파일에 기록된 DB 접속 사용자 정보(username/password)와 로컬 DB의 패스워드가 다를 때 발생합니다.

### 🛠️ 해결책 (Solution)
1. **DB 가동 여부 확인:** 로컬 시스템에서 MariaDB 서비스가 켜져 있는지 확인합니다.
2. **패스워드 동기화:** `.env` 파일의 `DB_PASSWORD` 설정값과 로컬 MariaDB 설치 시 생성했던 `root` 계정의 비밀번호가 일치하는지 대조합니다.
   * 예: 로컬 DB 패스워드가 `1234`이면 `.env` 파일 내용도 `DB_PASSWORD=1234`로 일치시켜야 합니다.
