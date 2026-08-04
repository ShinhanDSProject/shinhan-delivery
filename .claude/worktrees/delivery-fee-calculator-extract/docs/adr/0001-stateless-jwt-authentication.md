# ADR-0001: JWT 기반 무상태(Stateless) 인증 및 인가 체계 채택

* **상태 (Status):** 🟢 ACCEPTED (채택됨)
* **날짜 (Date):** 2026-07-28
* **결정자 (Deciders):** Shinhan DS 배달 아키텍처 팀
* **연관 이슈 (Related Issue):** [Issue #70](https://github.com/ShinhanDSProject/shinhan-delivery/issues/70)
* **연관 PR (Related PR):** [PR #84](https://github.com/ShinhanDSProject/shinhan-delivery/pull/84)

---

## 📌 1. 배경 및 문제 제기 (Context & Problem Statement)

`shinhan-delivery` 프로젝트는 고객, 배달원(쿠리어), 관리자가 실시간으로 배송 및 차량, 결제 데이터를 주고받는 대용량 RESTful API 서비스입니다.

REST API 서버를 구축함에 있어 다음 아키텍처 요구사항을 만족하는 최적의 사용자 인증(Authentication) 및 권한 인가(Authorization) 방식을 결정해야 했습니다:
1. **서버 무상태성(Stateless):** 서버 세션 저장소 부하 없이 수평적 확장(Scale-out)이 가능해야 함.
2. **다종 클라이언트 지원:** 웹 브라우저뿐만 아니라 모바일 앱, 타사 제휴 API 환경에서도 일관된 인증 헤더 제공 필요.
3. **보안 무결성:** 사용자 비밀번호 유출 방지 및 토큰 위변조 차단(Defense-in-Depth).

---

## 💡 2. 비교 및 검토된 대안들 (Alternatives Considered)

### Option 1: 세션/쿠키 기반 인증 (Session-Cookie Authentication)
* **동작 방식:** 서버 메모리/Redis에 세션 ID를 저장하고 클라이언트는 Cookie에 `JSESSIONID`를 담아 전송.
* **장점:** 서버에서 세션을 즉시 파기(로그아웃/강제 만료)할 수 있어 보안 통제가 용이함.
* **단점:** 
  - 서버를 여러 대 증설할 경우 세션 동기화(Redis 등 별도 인프라) 비용 발생 (Scale-out 장애 요소).
  - 모바일 앱 환경이나 Cross-Domain 환경에서 쿠키 처리 및 CSRF 방어가 복잡함.

### Option 2: Basic Authentication (매 요청 ID/PW 전송)
* **동작 방식:** HTTP 요청 헤더에 Base64로 인코딩된 이메일과 비밀번호를 매번 첨부하여 전송.
* **장점:** 구현이 매우 간단함.
* **단점:** 매 요청마다 비밀번호가 네트워크를 통해 노출되며, 탈취 시 치명적인 보안 참사 발생.

### Option 3: JWT 기반 무상태 토큰 인증 (Stateless JWT Authentication) - ⭐ [CHOSEN]
* **동작 방식:** 로그인 성공 시 서명(Signature)된 JWT 토큰을 발급하고, 클라이언트는 `Authorization: Bearer <TOKEN>` 헤더로 요청.
* **장점:** 
  - **서버 무상태성 100% 사수:** 서버에 세션 상태를 저장하지 않으므로 서버를 N대로 늘려도 추가 설정 없이 즉시 무한 확장(Scale-out) 가능.
  - **모바일/웹/제휴 API 범용성:** 표준 HTTP Bearer 헤더를 사용하여 모든 클라이언트 환경에서 동일한 방식으로 통신 가능.
  - **서명 검증을 통한 위변조 차단:** Secret Key 기반 HMAC-SHA 서명으로 클라이언트 측의 권한 조작 차단.
* **단점 및 보완책:**
  - 토큰이 한번 발급되면 만료 전까지 강제 파기가 어려움 ➔ **Access Token 유효기간을 1시간으로 단기 설정**하고, **Refresh Token(14일)**을 함께 운용하여 위험 최소화.

---

## 🎯 3. 최종 의사결정 (Architectural Decision)

**Option 3 (JWT 기반 무상태 인증 체계)**를 우리 프로젝트의 표준 보안 아키텍처로 최종 채택합니다.

### 🔒 인가 통제 범위 정책 (Security Authorization Scope):
1. **Default-Deny 원칙:** 프로젝트 내 모든 API 엔드포인트는 로그인(Valid JWT Token)을 거친 요청만 허용 (`.anyRequest().authenticated()`).
2. **최소 퍼블릭 허용 목록 (Explicit Public Allowlist):**
   - `POST /api/members/login` (로그인 토큰 발급)
   - `POST /api/members` (신규 회원가입)
   - `/swagger-ui/**`, `/v3/api-docs/**` (개발자 API 명세서)
   - `/actuator/health`, `/error` (서버 생사 확인 헬스체크 및 에러 서블릿)

---

## 🟢 4. 예상되는 효과 및 결과 (Consequences)

- **개발자/운영자 경험:** 서버 수평 확장 시 Redis 세션 서버 구축 비용 0원 절감.
- **보안성:** 비밀번호는 가입 시 BCrypt로 암호화 저장되어 평문 유출 가능성 차단.
- **하위 호환성:** 기존에 작성된 API 및 응답 DTO 규격 파괴 없이 보안 필터 체인만 투명하게 적용됨.
