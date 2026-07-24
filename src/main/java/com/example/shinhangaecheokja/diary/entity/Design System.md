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
버튼, 텍스트, 배경 등 UI 요소에 동일한 색상 규칙을 적용합니다.

### Login Button

#### Kakao

- 배경 : Yellow
- 글자 : Black
- 크기 : 350 × 60px

#### Apple

- 배경 : Black
- 글자 : White
- 크기 : 350 × 60px

#### Naver

- 배경 : Green
- 글자 : White
- 크기 : 350 × 60px

---

# 3. Typography

## 설명

서비스에서 사용하는 글꼴의 크기와 굵기를 정의합니다.

| 구분 | Font Size | Font Weight | Line Height |
|------|-----------|-------------|-------------|
| Main | 40px | Extra Bold | |
| Title | 20px | Semi Bold | |
| Body | 15px | Medium | |
| Button | 20px | Bold | |

---

# 4. Spacing

## 설명

컴포넌트 간 간격과 내부 여백을 정의합니다.

| 항목 | 값 |
|------|----|
| 배송지 입력 위 | 137px |
| 배송지 입력 아래 | 495px |
| 배송지 입력 옆 | 26px |

---

# 5. Border Radius

## 설명

버튼과 입력창 등의 모서리 둥근 정도를 정의합니다.

| Component | Radius |
|-----------|--------|
| Button | 12px |
| Input | 12px |

---

# 6. Button

## 설명

버튼의 크기와 디자인 규칙을 정의합니다.

### Login Button

#### Kakao

- Background : Yellow
- Text : Black
- Size : 350 × 60px

#### Apple

- Background : Black
- Text : White
- Size : 350 × 60px

#### Naver

- Background : Green
- Text : White
- Size : 350 × 60px

---

# 7. Input

## 설명

입력창의 디자인과 상태를 정의합니다.

- Default
- Focus
- Error
- Disabled

### Input 규칙

- Width
- Height
- Border
- Radius
- Placeholder

---

# 8. Icon

## 설명

서비스에서 사용하는 아이콘의 크기를 정의합니다.

| 항목 | 값 |
|------|----|
| Width | 60px |
| Height | 53px |

---

# 9. Card

## 설명

카드 컴포넌트의 디자인을 정의합니다.

- Background
- Shadow
- Radius
- Padding

---

# 10. Thymeleaf Component

## 설명

공통 UI를 Fragment로 분리하여 재사용합니다.

### Fragment 구조

```text
templates/
└── fragments/
    ├── header.html
    ├── footer.html
    ├── home.html
    ├── login.html
    ├── delivery-destination.html
    └── my-page.html
```

---

# 11. 페이지별 적용

## Home

- Header
- Login Button
- Icon

## Login

- Login Button
- Icon

## Delivery Destination

- Input
- Button

## My Page

- Card
- Button

---

## 참고 사항

- 본 문서는 Thymeleaf UI 개발 시 공통 기준으로 사용합니다.
- 공통 UI는 Thymeleaf Fragment를 활용하여 구현합니다.
- 디자인 변경 사항이 발생하면 본 문서를 함께 수정합니다.