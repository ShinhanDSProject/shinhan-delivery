# 🌐 초보자를 위한 RESTful API 설계 및 표준 규격 가이드

안녕하세요, 교육생 여러분! 신한DS 배달 백엔드 개발팀에 오신 것을 환영합니다.

웹과 모바일 애플리케이션 개발에서 백엔드와 프론트엔드가 서로 데이터를 주고받을 때 가장 널리 쓰이는 표준 아키텍처 스타일이 바로 **REST API**입니다.

이 문서는 **REST API가 무엇인지, 왜 RESTful하게 API를 설계해야 하는지, 초보자가 가장 많이 저지르는 안티 패턴(Anti-pattern) 사례와 우리 프로젝트의 공식 API 설계 규칙**을 친절한 비유를 통해 해설해 드립니다. 🎓

---

## 📚 목차
* **Part 1. REST API란 무엇인가요? (개념과 식당 비유)**
* **Part 2. RESTful API의 4대 핵심 요소 (Resource & Verb)**
* **Part 3. 초보자가 자주 범하는 안티 패턴 vs 올바른 설계 예시**
* **Part 4. HTTP 상태 코드(Status Code) 표준 응답 규칙**
* **Part 5. [공식 규칙] 우리 프로젝트의 RESTful API 개발 수칙**

---

## Part 1. REST API란 무엇인가요? (개념과 식당 비유)

### 1. 💡 REST (Representational State Transfer)의 핵심 개념
*   **REST란?** 웹(HTTP)의 기존 인프라를 활용하여 **"자원(Resource)의 이름을 지정하고, HTTP 메서드(GET, POST, PUT, DELETE)를 통해 해당 자원에 대한 행위(CRUD)를 수행"**하도록 정의한 소프트웨어 아키텍처 스타일입니다.
*   **RESTful이란?** REST 아키텍처의 원칙과 스타일을 잘 준수하여 설계된 웹 서비스를 **"RESTful하다"**라고 표현합니다.

### 2. 🍽️ 식당 주문 시스템으로 이해하는 REST API
*   **Resource (자원):** 메뉴판의 메뉴 이름 (예: `불고기 덮밥`, `스테이크`) ➔ URL에서는 **명사**로 표현 (`/api/v1/orders`)
*   **Verb (행위):** 손님이 주방에 요청하는 동작 (예: `주문하기`, `조회하기`, `취소하기`) ➔ HTTP Method로 표현 (`GET`, `POST`, `DELETE`)
*   **Representation (표현):** 음식 접시 형태 ➔ 웹 통신에서는 **JSON** 데이터 형식

만약 REST를 따르지 않는다면 메뉴판에 *"불고기덮밥주문하기"*, *"불고기덮밥조회하기"*, *"불고기덮밥취소하기"*처럼 메뉴마다 동작을 지저분하게 나열해야 합니다. REST를 따르면 메뉴 이름(`불고기 덮밥`) 하나에 동작(`GET`, `POST`, `DELETE`)을 깔끔하게 결합할 수 있습니다!

---

## Part 2. RESTful API의 4대 핵심 요소 (Resource & Verb)

REST API는 URL에 행위(동사)를 적지 않고, **URL은 '무엇(명사)'인지 자원만 가리키고, '어떻게 할 것인지(동사)'는 HTTP 메서드로 표현**합니다.

| HTTP Method | 역할 (CRUD) | 멱등성 (Idempotent) | 설명 및 예시 |
| :--- | :--- | :--- | :--- |
| **GET** | Read (조회) | O (예) | 자원을 조회합니다. 서블릿/서버 데이터 상태를 변경하지 않습니다.<br>`GET /api/v1/members/1` |
| **POST** | Create (생성) | X (아니오) | 새로운 자원을 신규 등록합니다.<br>`POST /api/v1/members` |
| **PUT** | Update (전체 수정) | O (예) | 자원의 전체 정보를 덮어쓰거나 수정합니다.<br>`PUT /api/v1/members/1` |
| **PATCH** | Update (부분 수정) | X (아니오) | 자원의 일부 필드만 부분 수정합니다.<br>`PATCH /api/v1/members/1` |
| **DELETE** | Delete (삭제) | O (예) | 자원을 삭제 처리합니다.<br>`DELETE /api/v1/members/1` |

> 💡 **멱등성(Idempotency)이란?**
> 연산을 여러 번 적용하더라도 결과가 달라지지 않는 성질입니다. `GET`이나 `DELETE`는 100번을 호출해도 서버 데이터의 최종 결과가 같지만, `POST`는 호출할 때마다 새로운 레코드가 계속 생겨나므로 멱등하지 않습니다.

> 💡 **`PUT`과 `PATCH` 무엇을 써야 할까요?**  
> `PUT`은 회원 정보 객체 전체를 덮어쓰는(Replace) 개념이므로, 요청 본문(Body)에 빠진 필드가 있다면 기존 DB의 데이터가 `null`이나 기본값으로 오염될 위험이 있습니다. 프로필 이미지나 닉네임처럼 일부 필드만 안전하게 변경할 때는 **`PATCH` (부분 갱신)** 사용을 강력하게 권장합니다!

---

## Part 3. 초보자가 자주 범하는 안티 패턴 vs 올바른 설계 예시

입문 개발자가 가장 흔히 실수하는 안티 패턴 3가지를 정리해 드립니다.

### ❌ 안티 패턴 1: URL에 행위(동사)를 기재하는 경우
*   **나쁜 예시:** `GET /api/v1/getMemberInfo?id=1` 또는 `POST /api/v1/deleteVehicle`
*   **올바른 설계:** 
    *   회원 정보 조회: `GET /api/v1/members/1`
    *   차량 삭제: `DELETE /api/v1/vehicles/1`
*   **이유:** `get`, `delete`, `create` 같은 동사는 URL에 적는 것이 아니라 HTTP Method(`GET`, `DELETE`)가 담당합니다.

### ❌ 안티 패턴 2: 단수형(Singular) 명사나 언더바(`_`)를 사용하는 경우
*   **나쁜 예시:** `GET /api/v1/member/1` 또는 `GET /api/v1/member_list`
*   **올바른 설계:** `GET /api/v1/members/1` 또는 `GET /api/v1/members`
*   **이유:** 자원의 집합(Collection)을 나타내므로 URL 경로에는 **소문자 복수형 명사(Plural)**를 사용하고, 구분을 위해 언더바(`_`) 대신 하이픈(`-`)을 사용하는 것이 업계 표준입니다.

### ❌ 안티 패턴 3: 예외나 에러가 났는데도 HTTP `200 OK`로 응답하는 경우
*   **나쁜 예시:** 응답 바디에는 `{"code": 500, "message": "회원 없음"}`이라고 보내면서 HTTP 상태 코드는 `200 OK`로 응답함.
*   **올바른 설계:** HTTP 상태 코드 자체를 `404 Not Found` 또는 `400 Bad Request`로 전송.
*   **이유:** 프론트엔드나 API 게이트웨이가 HTTP Header 상태 코드를 보고 1차 브라우저 예외 처리를 수행하기 때문입니다.

---

## Part 4. HTTP 상태 코드(Status Code) 표준 응답 규칙

우리 프로젝트에서는 API 요청 처리 결과에 따라 아래의 HTTP 상태 코드를 엄격히 준수하여 응답합니다.

### 🟢 2xx: 성공 (Success)
*   **`200 OK`:** 조회(`GET`)나 성공적인 수정(`PUT`/`PATCH`)에 대한 일반적인 성공 응답
*   **`201 Created`:** 신규 리소스 생성(`POST`) 성공 시 응답 (Response Body에 생성된 객체 정보 포함)
*   **`204 No Content`:** 삭제(`DELETE`) 성공 시 응답 (Response Body가 비어있음)

### 🟡 4xx: 클라이언트 요청 오류 (Client Error)
*   **`400 Bad Request`:** 입력 값 검증 실패 (파라미터 누락, 유효하지 않은 포맷 등)
*   **`401 Unauthorized`:** 인증 실패 (로그인하지 않았거나 토큰이 만료됨)
*   **`403 Forbidden`:** 권한 없음 (일반 회원이 관리자 전용 API를 호출하려는 경우)
*   **`404 Not Found`:** 존재하지 않는 리소스 요청 (ID에 해당하는 데이터가 DB에 없음)
*   **`409 Conflict`:** 데이터 충돌 (이미 존재하는 이메일로 중복 가입 시도 등)

### 🔴 5xx: 서버 오류 (Server Error)
*   **`500 Internal Server Error`:** 백엔드 자바 코드 상의 예상치 못한 NullPointerException 등 Unhandled 예외 발생 시

---

## Part 5. [공식 규칙] 우리 프로젝트의 RESTful API 개발 수칙

앞으로 우리 프로젝트에서 컨트롤러(Controller) 및 DTO를 개발할 때는 다음 4대 규칙을 반드시 준수해야 합니다:

1.  **URL 자원 명명 규칙:**
    *   URL 경로는 모두 `/api/v1` 접두사 뒤에 **소문자 복수형 명사**로 작성합니다. (`/api/v1/members`, `/api/v1/vehicles`, `/api/v1/point-wallets`)
    *   단어 구분 시 언더바(`_`)나 카멜케이스(`memberId`) 대신 **하이픈(`-`)**을 사용합니다. (`/api/v1/delivery-requests`)
2.  **행위와 HTTP 메서드 매핑:**
    *   조회: `GET`, 생성: `POST`, 수정: `PUT`/`PATCH`, 삭제: `DELETE` 규칙을 엄격히 준수하며, URL에 `get`, `create`, `delete` 같은 동사를 섞지 않습니다.
3.  **Swagger API 문서화 필수:**
    *   모든 `@RestController` 클래스와 메서드에는 `@Tag`, `@Operation`, `@ApiResponse` 어노테이션을 통해 의미와 응답 규격을 명시해야 합니다.
4.  **PR 자가 체크리스트 필수 체크:**
    *   PR 제출 시 자가 체크리스트의 `[ ] (신규 API 개발 시) RESTful URI 및 HTTP 메서드 명명 규칙(docs/rest-api-guide.md)을 준수하셨나요?` 항목을 확인 후 제출해야 합니다.

RESTful한 API는 프론트엔드 개발자와 백엔드 개발자 사이의 가장 명확하고 아름다운 약속입니다. 규칙을 준수하여 가독성 높고 표준화된 API를 개발해 봅시다! 🚀
