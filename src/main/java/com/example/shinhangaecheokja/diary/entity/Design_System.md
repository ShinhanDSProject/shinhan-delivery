# Design System

## 개요

Thymeleaf UI 개발 시 일관된 사용자 인터페이스를 제공하기 위한 디자인 시스템입니다.
공통 컴포넌트를 재사용하고 디자인 규칙을 통일하여 유지보수성과 개발 효율성을 높이는 것을 목표로 합니다.

<!-- th:replace/th:insert 가이드 -->
<button th:fragment="primaryBtn(text)" class="btn-blue-100" th:text="${text}"></button>

---

# 1. 목적

## 설명

디자인 시스템을 구축하는 이유를 정의합니다.

- 일관된 UI 제공
- 재사용 가능한 컴포넌트 구축
- 유지보수성 향상
- Thymeleaf Fragment 기반 공통 컴포넌트 사용

---

# 2. Color System

## 설명

서비스에서 사용하는 색상을 정의
버튼, 텍스트, 배경 등 UI 요소에 일관된 색상 규칙을 적용

orange-100 : #FA9200
white-100 : #FFFFFF
yellow-100 : #F7E600

blue-30 : #4A90FF
blue-50 : #EBF3FF
blue-100 : #2F73E0  -> .bg-blue-100 / var(--color-blue-100)
blue-500 : #2C88FF

green-50 : #D1FAE5
green-100 : #03C75A
green-200 : #10B981

black-100 : #000000
black-200 : #212121

red-30 : #FEE2E2
red-50 : #FCA5A5
red-100 : #EF4444

grey-30 : #EFEFEF
grey-50 : #E2E8F0
grey-100 : #94A3B8
grey-300 : #4A5568
grey-500 : #555555

brown-30 : #DEA66C
brown-50 : #B38251
brown-100 : #966239

beige-30 : #FFE0B2
beige-50 : #BF9F85

# 3. Design Components

## 설명
서비스에서 반복적으로 사용되는 UI 요소를 정의
버튼, 입력창, 카드, 아이콘 등 공통 컴포넌트를 동일한 디자인 규칙으로 관리하여 화면의 일관성을 유지
Thymeleaf Fragment를 활용한 재사용성 향상

버튼 : blue-100
카카오 버튼 : yellow-100
네이버 버튼 : green-100
애플 버튼 : black-100
구글 버튼 : grey-30
취소 버튼 : red-30, red-50, red-100
차트 : blue-30


# 4. icons

## 설명
다양한 상황에 맞는 아이콘 세트를 제공하여 일관된 디자인 언어를 유지

icon-1-arrow-fill -> <i class="icon-arrow-fill"></i>
icon-1-setting-fill
icon-1-dollar-fill
icon-1-wallet-fill
icon-1-car-fill
icon-1-customer-fill
icon-1-check-fill
icon-1-profile-fill
icon-1-call-fill
icon-1-time-fill
icon-1-camera-fill
icon-2-camera-fill
icon-1-kickboard-fill
icon-1-cup-fill
icon-1-clothes-fill
icon-1-file-fill
icon-2-file-fill
icon-1-box-fill
icon-1-home-fill
icon-1-light-fill
icon-1-book-fill
icon-1-line graph-fill
icon-1-heart-fill
icon-1-leaf-fill
icon-1-ellipses-fill
icon-1-motorcycle-fill
icon-1-bicycle-fill
icon-1-uav-fill
icon-1-compact car-fill
icon-1-midsize car-fill
icon-1-fullsize car-fill
icon-1-track-fill

# 5. Spacing

## 설명
컴포넌트와 요소 사이의 여백을 일관된 규칙으로 관리하기 위한 기준
동일한 간격 체계를 사용함으로써 화면의 가독성과 사용성 향상
디자이너와 개발자가 동일한 기준으로 UI를 구현

Button
Padding: 31px 0px
상하 31px, 좌우 0px의 내부 여백을 적용
Gap: 20px
버튼이 여러 개 배치될 경우 버튼 간 간격을 20px로 유지

Input
Padding: 24px
입력창 내부의 텍스트와 테두리 사이의 여백을 24px로 적용
Label → Input: 12px
Label과 Input 사이의 간격을 12px로 유지
Input → Input: 16px
여러 입력창을 배치할 경우 16px 간격을 유지

Card
Padding: 15px
카드 내부 콘텐츠와 테두리 사이의 여백을 15px로 적용
Gap: 25px
카드와 카드 사이의 간격을 25px로 유지

Navigation
Gap: 16px
메뉴 간 간격을 16px로 유지


# 6. Typography

## 설명
일관된 타이포그래피 시스템을 제공하여 다양한 UI 구성 요소에 적용

| Style    | Font Weight      | Font Size | Line-Height |
|-----------|------------------|--------------|-------------|
| Header   | Extra Bold (800) | 40px | 48px        |
| Display1 | Semi Bold (600)  | 20px | 22px        |
| Display2 | Medium (500)     | 15px | 20px        |
| Display3 | Regular (400)    | 15px | 20px        |
| Display4 | Bold (700)       | 20px | 24px        |

## 참고 사항

- 본 문서는 Thymeleaf UI 개발 시 공통 기준으로 사용합니다.
- 공통 UI는 Thymeleaf Fragment를 활용하여 구현합니다.
- 디자인 변경 사항이 발생하면 본 문서를 함께 수정합니다.
