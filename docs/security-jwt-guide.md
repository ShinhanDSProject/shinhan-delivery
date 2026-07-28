# 🔒 Spring Security & JWT 인증/인가 체계 가이드

이 문서는 `shinhan-gaecheokja` 프로젝트에 구축된 **Spring Security + JWT (JSON Web Token) 무상태(Stateless) 인증 및 인가 시스템**의 아키텍처 구조와 사용법을 초보 개발자의 눈높이에 맞춰 설명하는 가이드북입니다.

---

## 📌 1. 아키텍처 핵심 구조 (Architecture Overview)

우리 프로젝트는 세션 기반 인증 대신 **무상태(Stateless) JWT 기반 인증**을 채택하여 서버 메모리 부담을 없애고 세션 훔치기 공격을 차단합니다.

```mermaid
sequenceDiagram
    autonumber
    actor Client as 사용자 (Web/App)
    participant MemberCtrl as MemberController
    participant MemberSvc as MemberService
    participant Provider as JwtProvider
    participant Filter as JwtAuthenticationFilter
    
    Note over Client, Provider: 1. 회원가입 & 로그인 (공개 API)
    Client->>MemberCtrl: POST /api/members/login (email, password)
    MemberCtrl->>MemberSvc: login(LoginRequest)
    MemberSvc->>MemberSvc: BCrypt 비밀번호 검증
    MemberSvc->>Provider: createAccessToken / createRefreshToken
    Provider-->>Client: TokenResponse (accessToken, refreshToken, "Bearer")
    
    Note over Client, Filter: 2. 인가 필요한 API 요청 (보안 헤더 동반)
    Client->>Filter: GET /api/deliveries (Header: Authorization: Bearer <token>)
    Filter->>Provider: validateToken & getAuthentication
    Provider-->>Filter: SecurityContextHolder에 Authentication 주입
    Filter->>MemberCtrl: 컨트롤러로 요청 전달
```

---

## ⚙️ 2. 핵심 구성요소 및 파일 역할

| 파일명 | 경로 | 핵심 역할 |
| :--- | :--- | :--- |
| **`SecurityConfig.java`** | `common/security/SecurityConfig.java` | Spring Security 필터 체인 설정, CSRF disable, Session STATELESS, 공개 엔드포인트(`permitAll()`) 지정 |
| **`JwtProvider.java`** | `common/security/JwtProvider.java` | Access Token / Refresh Token 생성, 서명 검증, Claims 파싱 및 Authentication 객체 추출 |
| **`JwtAuthenticationFilter.java`** | `common/security/JwtAuthenticationFilter.java` | HTTP 요청의 `Authorization: Bearer` 헤더를 감지하여 SecurityContextHolder에 인증 객체 자동 주입 |
| **`CustomUserDetails.java`** | `common/security/CustomUserDetails.java` | Spring Security의 `UserDetails` 구현체. 회원 ID, 이메일, Role 권한 관리 |
| **`CustomUserDetailsService.java`** | `common/security/CustomUserDetailsService.java` | 이메일 기반 회원 조회 후 `CustomUserDetails` 생성 서비스 |

---

## 🔑 3. 초보 개발자 실전 활용법

### 1) 회원가입 및 비밀번호 암호화
회원가입 시 비밀번호는 평문(Plain text)으로 저장되지 않고 **BCrypt 단방향 해시 알고리즘**으로 암호화되어 DB에 저장됩니다.
```java
// MemberService.java
member.setPassword(passwordEncoder.encode(request.getPassword()));
```

### 2) 로그인 API 및 토큰 발급
로그인 요청 (`POST /api/members/login`) 성공 시 Access Token (만료 1시간)과 Refresh Token (만료 14일)이 응답으로 전달됩니다.
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

### 3) API 호출 시 HTTP Authorization 헤더 부착
인증이 필요한 API를 호출할 때는 HTTP 요청 헤더에 아래와 같이 `Bearer ` 접두사와 함께 토큰을 실어 보내야 합니다:
```http
GET /api/deliveries HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 🧪 4. 검증 및 자가 치유 (Self-Healing)

보안 모듈 추가 후 전체 빌드 무결성은 아래 명령어로 100% 검증됩니다:
```bash
./scripts/verify.sh
# 또는 원클릭 커밋/PR: ./pr
```
