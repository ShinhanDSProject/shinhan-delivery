# 🎨 신한 개척자 공통 디자인 시스템 (Design System Guide)

> **HTML5, Vanilla CSS & Thymeleaf UI 개발을 위한 통합 디자인 토큰 및 공통 컴포넌트 가이드북**  
> 프로젝트 전체에서 일관된 UI/UX 경험을 제공하고 컴포넌트 재사용성을 극대화하기 위해 구축된 표준 가이드입니다.

---

## 📑 목차
- [1. 개요 및 사용법](#1-개요-및-사용법)
- [2. 라이브 스타일 가이드 (Live Style Guide)](#2-라이브-스타일-가이드-live-style-guide)
- [3. Color System (색상 토큰)](#3-color-system-색상-토큰)
- [4. Typography System (타이포그래피)](#4-typography-system-타이포그래피)
- [5. Spacing & Layout Tokens (여백 규격)](#5-spacing--layout-tokens-여백-규격)
- [6. Button Components (버튼 컴포넌트)](#6-button-components-버튼-컴포넌트)
- [7. Form & Input Components (입력창 컴포넌트)](#7-form--input-components-입력창-컴포넌트)
- [8. Card & Badge Components (카드 및 배지)](#8-card--badge-components-카드-및-배지)
- [9. Thymeleaf Fragment 사용법 (공통 컴포넌트)](#9-thymeleaf-fragment-사용법-공통-컴포넌트)

---

## 1. 개요 및 사용법

모든 HTML 및 Thymeleaf 템플릿 파일 상단 `<head>`에 디자인 시스템 CSS를 포함해 주세요:

```html
<link rel="stylesheet" href="/css/design-system.css">
```

---

## 2. 라이브 스타일 가이드 (Live Style Guide)

서버 기동 (`./gradlew bootRun`) 후 브라우저에서 아래 URL로 접속하면 모든 컬러, 타이포그래피, 버튼, 입력창, 아이콘을 인터랙티브하게 직접 확인하실 수 있습니다:

👉 **[http://localhost:8080/style-guide.html](http://localhost:8080/style-guide.html)**

---

## 3. Color System (색상 토큰)

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **blue-100** | `var(--color-blue-100)` | `#2F73E0` | **Primary Brand Color** (기본 주요 버튼 및 하이라이트) |
| **blue-500** | `var(--color-blue-500)` | `#2C88FF` | Primary Hover / Active 배경색 |
| **blue-50** | `var(--color-blue-50)` | `#EBF3FF` | Primary 연한 배경 및 선택 칩 |
| **blue-30** | `var(--color-blue-30)` | `#4A90FF` | 차트 및 보조 강조 색상 |
| **yellow-100** | `var(--color-yellow-100)` | `#F7E600` | 카카오 로그인 버튼 (`.btn-kakao`) |
| **green-100** | `var(--color-green-100)` | `#03C75A` | 네이버 로그인 버튼 (`.btn-naver`) |
| **black-100** | `var(--color-black-100)` | `#000000` | 애플 로그인 버튼 (`.btn-apple`) |
| **black-200** | `var(--color-black-200)` | `#212121` | 기본 본문 텍스트 색상 |
| **grey-30** | `var(--color-grey-30)` | `#EFEFEF` | 구글 로그인 버튼 (`.btn-google`) 및 배경 |
| **red-100** | `var(--color-red-100)` | `#EF4444` | Danger / 경고 / 삭제 버튼 |
| **red-30** | `var(--color-red-30)` | `#FEE2E2` | 취소 버튼 배경 (`.btn-cancel`) |
| **orange-100** | `var(--color-orange-100)` | `#FA9200` | 보조 브랜드 포인트 색상 |

---

## 4. Typography System (타이포그래피)

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-header` | Extra Bold (800) | 40px | 48px | 메인 타이틀, 스플래시 대형 문구 |
| `.typo-display1` | Semi Bold (600) | 20px | 22px | 카드 제목, 섹션 타이틀 |
| `.typo-display2` | Medium (500) | 15px | 20px | 부제목, 라벨 텍스트 |
| `.typo-display3` | Regular (400) | 15px | 20px | 일반 본문 텍스트 |
| `.typo-display4` | Bold (700) | 20px | 24px | 강조용 서브헤딩 |

---

## 5. Spacing & Layout Tokens (여백 규격)

- **Button Padding:** 상하 `16px`, 좌우 `24px` (컨테이너 여백: `31px`)
- **Button Gap:** 버튼 간 간격 `20px`
- **Input Padding:** 내부 여백 `16px 20px`
- **Label → Input Gap:** 라벨과 입력창 사이 `12px`
- **Input → Input Gap:** 입력창과 입력창 사이 `16px`
- **Card Padding:** 카드 내부 여백 `20px` (간격 `25px`)

---

## 6. Button Components (버튼 컴포넌트)

```html
<!-- Primary 버튼 -->
<button class="btn btn-primary">확인</button>

<!-- 소셜 로그인 버튼 세트 -->
<button class="btn btn-kakao">카카오로 시작하기</button>
<button class="btn btn-naver">네이버로 시작하기</button>
<button class="btn btn-apple">Apple로 로그인</button>
<button class="btn btn-google">Google로 로그인</button>

<!-- 위험/취소 버튼 -->
<button class="btn btn-danger">삭제하기</button>
<button class="btn btn-cancel">취소</button>
```

---

## 7. Form & Input Components (입력창 컴포넌트)

```html
<div class="input-group">
  <label for="user-email" class="input-label">이메일 주소</label>
  <input type="email" id="user-email" class="input-field" placeholder="user@example.com">
</div>

<!-- 에러 상태 -->
<div class="input-group has-error">
  <label for="user-password" class="input-label">비밀번호</label>
  <input type="password" id="user-password" class="input-field">
  <span class="input-error-text">비밀번호가 일치하지 않습니다.</span>
</div>
```

---

## 8. Card & Badge Components (카드 및 배지)

```html
<div class="card-box">
  <span class="badge badge-primary">CUSTOMER</span>
  <h3 class="typo-display1">배송 요청 정보</h3>
  <p class="typo-display3 text-grey-300">서울특별시 중구 -> 강남구 역삼동</p>
</div>
```

---

## 9. Thymeleaf Fragment 사용법 (공통 컴포넌트)

Thymeleaf 템플릿에서 공통 프래그먼트를 불러와 손쉽게 조립할 수 있습니다:

```html
<!-- 1. Primary 버튼 프래그먼트 -->
<div th:replace="~{fragments/components :: primaryBtn('로그인하기')}"></div>

<!-- 2. 소셜 버튼 프래그먼트 -->
<div th:replace="~{fragments/components :: socialBtn('kakao', '카카오 로그인')}"></div>

<!-- 3. Input 필드 프래그먼트 -->
<div th:replace="~{fragments/components :: inputField('email', '이메일', 'email', 'email', '이메일을 입력하세요')}"></div>

<!-- 4. 상태 배지 프래그먼트 -->
<span th:replace="~{fragments/components :: badge('매칭완료', 'success')}"></span>
```
