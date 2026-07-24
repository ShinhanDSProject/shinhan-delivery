# Design System

## 개요

Thymeleaf UI 개발 시 일관된 사용자 인터페이스를 제공하기 위한 디자인 시스템입니다.
공통 컴포넌트를 재사용하고 디자인 규칙을 통일하여 유지보수성과 개발 효율성을 높이는 것을 목표로 합니다.

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

서비스에서 사용하는 색상을 정의합니다.
버튼, 텍스트, 배경 등 UI 요소에 일관된 색상 규칙을 적용합니다.

orange-100 : #FA9200
blue-100 : #2C88FF
white-100 : #FFFFFF
yellow-100 : #F7E600
green-100 : #03C75A
black-100 : #000000
black-200 : #212121
grey-30: #EFEFEF
grey-500 : #555555
brown-30 : DEA66C
brown-50 : #B38251
brown-100 : #966239
beige-30 : #FFE0B2
beige-50 : #BF9F85

# 3. Design Components

## 설명
Design Components는 서비스에서 반복적으로 사용되는 UI 요소를 정의
버튼, 입력창, 카드, 아이콘 등 공통 컴포넌트를 동일한 디자인 규칙으로 관리하여 화면의 일관성을 유지
Thymeleaf Fragment를 활용한 재사용성 향상

버튼 : blue-100
카카오 버튼 : yellow-100
네이버 버튼 : green-100
애플 버튼 : black-100
구글 버튼 : grey-30


# 4. icons

# 설명
다양한 상황에 맞는 아이콘 세트를 제공하여 일관된 디자인 언어를 유지

icon-1-car-fill
icon-1-customer-fill


# 5. Typography

# 설명
일관된 타이포그래피 시스템을 제공하여 다양한 UI 구성 요소에 적용

| Style    | Font Weight | Font Size | Line-Height |
|-----------|-----------------|--------------|-------------|
| Header   | Extra Bold (800) | 40px | -5px        |
| Display1 | Semi Bold (600) | 20px | -5px        |
| Display2 | Medium (600) | 15px | -5px        |
| Display3 | Regular (600) | 15px | -5px        |
| Display4 | Bold (600)  | 20px | -5px        |

## 참고 사항

- 본 문서는 Thymeleaf UI 개발 시 공통 기준으로 사용합니다.
- 공통 UI는 Thymeleaf Fragment를 활용하여 구현합니다.
- 디자인 변경 사항이 발생하면 본 문서를 함께 수정합니다.