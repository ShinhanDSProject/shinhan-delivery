# 🎨 Shinhan Delivery 공통 디자인 시스템 (Design System Guide)

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

## 3. Color System - 색상 토큰 (124개)

## Blue 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **blue-100** | `var(--color-blue-100)` | `#0D47A1` | 진한 파란 텍스트 / 정보 강조 라벨 |
| **blue-90** | `var(--color-blue-90)` | `#2F73E0` | Primary Brand Color (기본 주요 버튼 및 하이라이트) |
| **blue-80** | `var(--color-blue-80)` | `#2C88FF` | Primary Hover / Active 배경색 / 메인 강조 색상 / CTA 버튼 배경 (`.btn-primary`) |
| **blue-70** | `var(--color-blue-70)` | `#3F51B5` | 인디고 계열 정보 라벨 텍스트 / 카테고리 태그 |
| **blue-60** | `var(--color-blue-60)` | `#3B82F6` | 보조 링크 텍스트 / 인라인 하이퍼링크 |
| **blue-50** | `var(--color-blue-50)` | `#4A90FF` | 차트 데이터 시각화 / 보조 강조 색상 / 진행률 바 |
| **blue-bg-100** | `var(--color-blue-bg-100)` | `#E8EAF6` | 인디고 카테고리 태그 배경 / 정보 배지 배경 |
| **blue-bg-90** | `var(--color-blue-bg-90)` | `#E3F2FD` | 정보 알림 카드 배경 / 안내 메시지 배경 |
| **blue-bg-80** | `var(--color-blue-bg-80)` | `#E5F3FF` | 선택된 리스트 아이템 배경 |
| **blue-bg-70** | `var(--color-blue-bg-70)` | `#EAF3FF` | 활성 탭 배경 / 선택 상태 배경 |
| **blue-bg-60** | `var(--color-blue-bg-60)` | `#E9F3FF` | 정보 배너 배경 / 알림 영역 배경 |
| **blue-bg-50** | `var(--color-blue-bg-50)` | `#EBF3FF` | Primary 연한 배경 / 선택된 카드 배경 / 활성 필터 배경 |
| **blue-bg-40** | `var(--color-blue-bg-40)` | `#EBF5FF` | 정보 안내 섹션 배경 / 툴팁 배경 |
| **blue-bg-30** | `var(--color-blue-bg-30)` | `#F0F6FF` | 가장 연한 파란 배경 / 호버 상태 배경 |
| **blue-bg-20** | `var(--color-blue-bg-20)` | `#F1F5F9` | 취소 버튼 배경 (`.btn-cancel`) / 비활성 영역 배경 |
| **blue-bg-10** | `var(--color-blue-bg-10)` | `#F4F6FA` | 화면 전체 배경 / 사이드바 배경 / 페이지 기본 배경 |
| **blue-alpha-10** | `var(--color-blue-alpha-10)` | `#2C88FF @ 5%` | 투명 파란 배경 / 호버 오버레이 |
| **blue-alpha-20** | `var(--color-blue-alpha-20)` | `#2C88FF @ 6%` | 투명 파란 배경 / 포커스 링 배경 |
| **blue-alpha-30** | `var(--color-blue-alpha-30)` | `#2C88FF @ 7%` | 투명 파란 배경 / 선택 상태 오버레이 |
| **blue-alpha-40** | `var(--color-blue-alpha-40)` | `#2C88FF @ 8%` | 투명 파란 배경 / 활성 상태 오버레이 / 버튼 pressed 배경 |
| **blue-alpha-50** | `var(--color-blue-alpha-50)` | `#2C88FF @ 10%` | 투명 파란 배경 / 강조 영역 오버레이 |
| **blue-alpha-60** | `var(--color-blue-alpha-60)` | `#3B82F6 @ 10%` | 투명 파란 배경 변형 / 보조 강조 오버레이 |

---

## Green 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **green-100** | `var(--color-green-100)` | `#03C75A` | 네이버 로그인 버튼 (`.btn-naver`) |
| **green-90** | `var(--color-green-90)` | `#10B981` | 완료 버튼 배경 (`.btn-success`) / 성공 상태 아이콘 |
| **green-80** | `var(--color-green-80)` | `#22C55E` | 성공 상태 텍스트 / 승인 완료 아이콘 |
| **green-70** | `var(--color-green-70)` | `#2EA44F` | 승인 완료 라벨 텍스트 / 활성 상태 표시 |
| **green-60** | `var(--color-green-60)` | `#34C759` | iOS 스타일 토글 활성 상태 / 성공 border |
| **green-bg-50** | `var(--color-green-bg-50)` | `#D1FAE5` | 성공 상태 연한 배경 / 완료 배지 배경 |
| **green-bg-40** | `var(--color-green-bg-40)` | `#ECFDF5` | 성공 알림 카드 배경 / 승인 완료 영역 배경 |
| **green-bg-30** | `var(--color-green-bg-30)` | `#E8F8F5` | 성공 배너 배경 / 안내 메시지 배경 |
| **green-alpha-20** | `var(--color-green-alpha-20)` | `#10B981 @ 6%` | 성공 상태 호버 오버레이 |
| **green-alpha-30** | `var(--color-green-alpha-30)` | `#10B981 @ 8%` | 성공 버튼 pressed 배경 / 완료 상태 오버레이 |
| **green-alpha-40** | `var(--color-green-alpha-40)` | `#10B981 @ 10%` | 성공 강조 영역 오버레이 |
| **green-alpha-50** | `var(--color-green-alpha-50)` | `#22C55E @ 10%` | 승인 상태 배경 오버레이 |
| **green-alpha-60** | `var(--color-green-alpha-60)` | `#2EA44F @ 8%` | 활성 상태 배경 오버레이 |
| **green-alpha-70** | `var(--color-green-alpha-70)` | `#34C759 @ 8%` | iOS 스타일 성공 배경 오버레이 |

---

## Red 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **red-100** | `var(--color-red-100)` | `#C62828` | 심각한 에러 텍스트 / 결제 실패 메시지 |
| **red-90** | `var(--color-red-90)` | `#D32F2F` | 에러 상태 텍스트 / 유효성 검사 실패 메시지 |
| **red-80** | `var(--color-red-80)` | `#EF4444` | Danger / 삭제 버튼 (`.btn-danger`) / 경고 아이콘 |
| **red-70** | `var(--color-red-70)` | `#EA4335` | Google 브랜드 Red / 소셜 로그인 에러 |
| **red-60** | `var(--color-red-60)` | `#FF0000` | 필수 입력 표시 (`*`) / 긴급 알림 텍스트 |
| **red-50** | `var(--color-red-50)` | `#FF3B30` | iOS 스타일 에러 / 알림 뱃지 배경 / 삭제 스와이프 |
| **red-40** | `var(--color-red-40)` | `#FF4D4D` | 밝은 경고 아이콘 / 실시간 에러 표시 |
| **red-30** | `var(--color-red-30)` | `#E91E63` | 핑크 계열 강조 텍스트 / 좋아요 아이콘 |
| **red-border-80** | `var(--color-red-border-80)` | `#FCA5A5` | 에러 입력 필드 연한 border |
| **red-border-60** | `var(--color-red-border-60)` | `#FFCDD2` | 에러 카드 border / 경고 영역 외곽선 |
| **red-bg-50** | `var(--color-red-bg-50)` | `#FCE4EC` | 핑크 태그 배경 / 좋아요 상태 배경 |
| **red-bg-40** | `var(--color-red-bg-40)` | `#FEE2E2` | 에러 알림 연한 배경 / 유효성 에러 필드 배경 |
| **red-bg-30** | `var(--color-red-bg-30)` | `#FFEBEE` | 에러 배너 배경 / 경고 메시지 카드 배경 |
| **red-bg-20** | `var(--color-red-bg-20)` | `#FFECEC` | 에러 토스트 배경 |
| **red-alpha-40** | `var(--color-red-alpha-40)` | `#EF4444 @ 10%` | 에러 상태 호버 오버레이 |
| **red-alpha-30** | `var(--color-red-alpha-30)` | `#FF3B30 @ 6%` | 삭제 버튼 pressed 배경 |

---

## Orange 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **orange-100** | `var(--color-orange-100)` | `#E65100` | 진한 경고 텍스트 / 긴급 배송 라벨 |
| **orange-80** | `var(--color-orange-80)` | `#FA9200` | 배송 포인트 색상 / 경고 아이콘 / 진행 중 상태 표시 |
| **orange-bg-50** | `var(--color-orange-bg-50)` | `#FFE0B2` | 경고 배지 배경 / 배송 상태 카드 배경 |
| **orange-bg-40** | `var(--color-orange-bg-40)` | `#FFF3E0` | 경고 알림 배너 배경 / 주의 메시지 배경 |
| **orange-bg-30** | `var(--color-orange-bg-30)` | `#FFF4E5` | 경고 안내 영역 배경 |
| **orange-alpha-10** | `var(--color-orange-alpha-10)` | `#FA9200 @ 4%` | 경고 호버 오버레이 (약) |
| **orange-alpha-20** | `var(--color-orange-alpha-20)` | `#FA9200 @ 6%` | 경고 호버 오버레이 |
| **orange-alpha-30** | `var(--color-orange-alpha-30)` | `#FA9200 @ 7%` | 경고 선택 상태 오버레이 |
| **orange-alpha-40** | `var(--color-orange-alpha-40)` | `#FA9200 @ 8%` | 경고 버튼 pressed 배경 / 배송 상태 오버레이 |
| **orange-alpha-50** | `var(--color-orange-alpha-50)` | `#FA9200 @ 10%` | 경고 강조 영역 오버레이 |

---

## Yellow 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **yellow-100** | `var(--color-yellow-100)` | `#F7E600` | 카카오 로그인 버튼 (`.btn-kakao`) |
| **yellow-80** | `var(--color-yellow-80)` | `#FFE500` | 별점 / 즐겨찾기 아이콘 활성 상태 |
| **yellow-60** | `var(--color-yellow-60)` | `#F4E865` | 하이라이트 마커 배경 / 연한 강조 표시 |

---

## Purple 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **purple-100** | `var(--color-purple-100)` | `#9C27B0` | 보라 카테고리 라벨 텍스트 / VIP 등급 표시 |
| **purple-80** | `var(--color-purple-80)` | `#8B5CF6` | 프리미엄 배지 / 특별 태그 배경 |
| **purple-bg-30** | `var(--color-purple-bg-30)` | `#F3E5F5` | 보라 카테고리 태그 배경 / VIP 영역 배경 |

---

## Black 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **black-100** | `var(--color-black-100)` | `#000000` | 애플 로그인 버튼 (`.btn-apple`) / 메인 헤딩 텍스트 |
| **black-90** | `var(--color-black-90)` | `#111111` | 다크 모드 배경 / 풋터 배경 |
| **black-80** | `var(--color-black-80)` | `#111827` | 페이지 메인 타이틀 / 모달 헤딩 |
| **black-70** | `var(--color-black-70)` | `#1A202C` | 회원가입 화면 메인 강조 색상 / 서브헤딩 텍스트 |
| **black-60** | `var(--color-black-60)` | `#212121` | 기본 본문 텍스트 색상 / 카드 타이틀 |
| **black-50** | `var(--color-black-50)` | `#222222` | 본문 텍스트 변형 / 리스트 아이템 텍스트 |
| **black-40** | `var(--color-black-40)` | `#333333` | 보조 헤딩 텍스트 / 네비게이션 메뉴 텍스트 |
| **black-30** | `var(--color-black-30)` | `#3C1E1E` | 갈색 계열 강조 텍스트 |
| **black-alpha-0** | `var(--color-black-alpha-0)` | `#000000 @ 0%` | 완전 투명 fill (레이아웃 스페이서) |
| **black-alpha-1** | `var(--color-black-alpha-1)` | `#000000 @ 1%` | 거의 투명 fill (터치 영역 확장) |
| **black-alpha-2** | `var(--color-black-alpha-2)` | `#000000 @ 2%` | 미세 그림자 / 카드 구분 배경 |
| **black-alpha-6** | `var(--color-black-alpha-6)` | `#000000 @ 6%` | 연한 스크림 오버레이 / 섹션 구분 배경 |
| **black-alpha-20** | `var(--color-black-alpha-20)` | `#000000 @ 20%` | 모달 딤 배경 (약) / 이미지 위 텍스트 스크림 |
| **black-alpha-50** | `var(--color-black-alpha-50)` | `#000000 @ 50%` | 모달 딤 배경 / 풀스크린 오버레이 |

---

## Grey 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **grey-100** | `var(--color-grey-100)` | `#4A5568` | 회원가입 화면 메인 텍스트 색상 / 배경 보조 색상 |
| **grey-90** | `var(--color-grey-90)` | `#555555` | 로그인 화면 보조 텍스트 색상 / 설명 텍스트 |
| **grey-80** | `var(--color-grey-80)` | `#616161` | 보조 설명 텍스트 / 캡션 |
| **grey-70** | `var(--color-grey-70)` | `#666666` | 비활성 메뉴 텍스트 / 날짜 표시 |
| **grey-60** | `var(--color-grey-60)` | `#888888` | 비활성 텍스트 / 타임스탬프 / caption |
| **grey-50** | `var(--color-grey-50)` | `#94A3B8` | 회원가입 화면 보조 텍스트 색상 / placeholder 텍스트 |
| **grey-45** | `var(--color-grey-45)` | `#999999` | 비활성 아이콘 stroke / disabled 상태 텍스트 |
| **grey-40** | `var(--color-grey-40)` | `#9CA3AF` | 힌트 텍스트 / 입력 필드 placeholder |
| **grey-35** | `var(--color-grey-35)` | `#A0AEC0` | 연한 보조 텍스트 / 메타 정보 |
| **grey-30** | `var(--color-grey-30)` | `#BFC1C5` | 버튼 외곽선 메인 색상 / 고객 프로필 메인 색상 |
| **grey-border-90** | `var(--color-grey-border-90)` | `#C7C7C7` | 입력 필드 기본 border / 비활성 버튼 외곽선 |
| **grey-border-80** | `var(--color-grey-border-80)` | `#CCCCCC` | 카드 border / 구분선 보조 |
| **grey-border-70** | `var(--color-grey-border-70)` | `#D0D0D0` | 리스트 구분선 / 드롭다운 border |
| **grey-border-60** | `var(--color-grey-border-60)` | `#DBDBDB` | 고객 프로필 구분선 / 테이블 border |
| **grey-border-50** | `var(--color-grey-border-50)` | `#DDDDDD` | 약한 구분선 / 컨테이너 border |
| **grey-border-40** | `var(--color-grey-border-40)` | `#E0E0E0` | 섹션 구분선 / 탭 하단 border |
| **grey-border-30** | `var(--color-grey-border-30)` | `#E2E8F0` | 버튼 외곽선 메인 / 입력 필드 border / 카드 border |
| **grey-border-20** | `var(--color-grey-border-20)` | `#E5E5E5` | 리스트 아이템 구분선 / 하단 border |
| **grey-border-15** | `var(--color-grey-border-15)` | `#E5E7EB` | 테이블 행 구분선 / 컨텐츠 영역 border |
| **grey-border-10** | `var(--color-grey-border-10)` | `#E9E9E9` | 연한 카드 border / 호버 시 border |
| **grey-line-90** | `var(--color-grey-line-90)` | `#EAEAEA` | 구분선 stroke / 헤더-바디 구분 |
| **grey-line-80** | `var(--color-grey-line-80)` | `#EBEBEB` | 구분선 / 카드 border / 버튼 외곽선 보조 색상 |
| **grey-line-70** | `var(--color-grey-line-70)` | `#EEEEEE` | 연한 구분선 / 그리드 라인 |
| **grey-line-60** | `var(--color-grey-line-60)` | `#EFEFEF` | 구글 로그인 버튼 (`.btn-google`) 배경 / 버튼 외곽선 보조 색상 |
| **grey-bg-90** | `var(--color-grey-bg-90)` | `#F0F0F0` | 비활성 입력 필드 배경 / disabled 상태 배경 |
| **grey-bg-80** | `var(--color-grey-bg-80)` | `#F1F1F1` | 검색 바 배경 / 필터 영역 배경 |
| **grey-bg-70** | `var(--color-grey-bg-70)` | `#F3F3F3` | 사이드바 배경 / 코드 블록 배경 |
| **grey-bg-65** | `var(--color-grey-bg-65)` | `#F3F4F6` | 테이블 헤더 배경 / 비활성 탭 배경 |
| **grey-bg-60** | `var(--color-grey-bg-60)` | `#F5F5F5` | 섹션 배경 / 접힌 영역 배경 |
| **grey-bg-50** | `var(--color-grey-bg-50)` | `#F8F8F8` | 주소 화면 보조 색상 / 입력 필드 배경 / 카드 배경 |
| **grey-bg-40** | `var(--color-grey-bg-40)` | `#FAFAFA` | 모달 배경 / 드롭다운 배경 |
| **grey-bg-30** | `var(--color-grey-bg-30)` | `#FAFBFD` | 페이지 전체 배경 / 메인 콘텐츠 영역 배경 |
| **grey-bg-20** | `var(--color-grey-bg-20)` | `#FDFDFD` | 가장 밝은 카드 배경 / 호버 상태 배경 |
| **grey-alpha-26** | `var(--color-grey-alpha-26)` | `#555555 @ 26%` | 투명 회색 오버레이 / 스켈레톤 로딩 |

---

## White 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **white-100** | `var(--color-white-100)` | `#FFFFFF` | 버튼 텍스트 색상 / 카드 배경 / 네비게이션 바 배경 |
| **white-alpha-88** | `var(--color-white-alpha-88)` | `#FFFFFF @ 88%` | 반투명 흰색 네비게이션 바 배경 (블러 효과) |
| **white-alpha-20** | `var(--color-white-alpha-20)` | `#FFFFFF @ 20%` | 다크 모드 위 투명 흰색 구분선 |

---

## Skin / Brown 계열

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **skin-100** | `var(--color-skin-100)` | `#966239` | 아바타 일러스트 스킨톤 (어두운 색) |
| **skin-80** | `var(--color-skin-80)` | `#B38251` | 아바타 일러스트 스킨톤 (중간 어두운 색) |
| **skin-60** | `var(--color-skin-60)` | `#BF9F85` | 아바타 일러스트 스킨톤 (중간색) |
| **skin-40** | `var(--color-skin-40)` | `#DEA66C` | 아바타 일러스트 스킨톤 (밝은색) |

---

## 기타

| 토큰명 | CSS 변수명 | HEX Code | 사용처 및 설명 |
| :--- | :--- | :--- | :--- |
| **misc-green** | `var(--color-misc-green)` | `#65F465` | 온라인 상태 표시 / 실시간 활성 인디케이터 |



---

## 4. Typography System (타이포그래피)

## 4. Typography System (타이포그래피)

---

### Display / Header

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-header-xl` | Extra Bold (800) | 40px | Auto | 메인 타이틀, 스플래시 대형 문구 ("Deliver Happiness") |
| `.typo-header-lg` | Extra Bold (800) | 30px | Auto | 마이페이지 섹션 타이틀 ("My Point", "My Page") |
| `.typo-header-md` | Extra Bold (800) | 25px | Auto | 포인트 잔액 대형 표시 ("10,000 Point") |
| `.typo-header-sm` | Extra Bold (800) | 22px | Auto | 슬로건 문구 ("문앞에서 문앞으로") |
| `.typo-header-xs` | Extra Bold (800) | 20px | Auto | 카드 메인 타이틀 ("결제 비밀번호를 입력하세요") |
| `.typo-header-xxs` | Extra Bold (800) | 18px | Auto | 사용자 이름 강조 ("김이현 기사님", "김혜민 님") |

---

### Title

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-title-xl` | Bold (700) | 35px | Auto | 포인트 단위 대형 표시 ("P") |
| `.typo-title-lg` | Bold (700) | 30px | Auto | 역할 선택 타이틀 ("개인 고객", "배송파트너") |
| `.typo-title-md` | Bold (700) | 28px | Auto | 포인트 단위 중형 표시 ("P") |
| `.typo-title-sm` | Bold (700) | 25px | Auto | 화면 메인 타이틀 ("새 배송 신청 예약") |
| `.typo-title-xs` | Bold (700) | 24px | Auto | 어드민 관리 페이지 헤딩 ("회원 관리", "배달원 관리") |
| `.typo-title-xxs` | Bold (700) | 22px | Auto | 넘버링 / 스텝 표시 ("1", "2", "3") |
| `.typo-title-20` | Bold (700) | 20px | Auto | CTA 버튼 텍스트 ("다음으로", "start") |

---

### Subtitle

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-subtitle-lg` | Semi Bold (600) | 20px | Auto | 배송 상태 메인 텍스트 ("물품 픽업 완료", "고객님께 이동 중") |
| `.typo-subtitle-md` | Bold (700) | 18px | Auto | 화면 타이틀 ("로그인") / 섹션 헤딩 |
| `.typo-subtitle-sm` | Bold (700) | 16px | Auto | 버튼 텍스트 ("가입 완료", "가입 신청") |
| `.typo-subtitle-xs` | Extra Bold (800) | 16px | Auto | 주소 강조 ("서울 강남구 테헤란로 123") |
| `.typo-subtitle-xxs` | Extra Bold (800) | 15px | Auto | 포인트 금액 ("100,000 P") / 경로 요약 ("강남구 역삼동 → 서초구 반포동") |

---

### Body — Semi Bold

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-body-semibold-lg` | Semi Bold (600) | 15px | Auto | 배송 상태 라벨 ("배송 중", "예상 도착: 오후 14:25") |
| `.typo-body-semibold-md` | Semi Bold (600) | 14px | Auto | 폼 라벨 ("이름", "이메일 주소") |
| `.typo-body-semibold-sm` | Semi Bold (600) | 13px | Auto | 상태 바 시간 ("10:00") / 강조 라벨 |
| `.typo-body-semibold-xs` | Semi Bold (600) | 12px | Auto | 경고 안내문 / 날짜 ("01.18 14:10") |
| `.typo-body-semibold-xxs` | Semi Bold (600) | 11px | Auto | 예상 시간 ("예상 22분") / 지역명 ("송파구") |

---

### Body — Bold

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-body-bold-md` | Bold (700) | 15px | Auto | 알림 메시지 ("배송원 배차가 완료되었습니다.") |
| `.typo-body-bold-sm` | Bold (700) | 14px | Auto | 상태 바 시간 (볼드) / 강조 정보 |
| `.typo-body-bold-xs` | Bold (700) | 13px | Auto | 탭 라벨 ("전체") / 필터명 ("물품 크기") |
| `.typo-body-bold-xxs` | Bold (700) | 12px | Auto | 하단 네비게이션 라벨 ("매칭", "배송") |
| `.typo-body-bold-xxxs` | Bold (700) | 11px | Auto | 상태 배지 텍스트 ("진행중", "완료") |
| `.typo-body-bold-xxxxs` | Bold (700) | 10px | Auto | 지도 마커 라벨 ("출발", "도착") |

---

### Body — Medium

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-body-medium-lg` | Medium (500) | 16px | Auto | 단위 텍스트 ("원") / 금액 라벨 ("충전 신청 금액") |
| `.typo-body-medium-md` | Medium (500) | 15px | Auto | 본문 설명 ("초단기 매칭으로 가장 빠르게 원하는 곳으로 보내세요.") |
| `.typo-body-medium-sm` | Medium (500) | 14px | Auto | 리스트 아이템 ("5kg", "10kg") |
| `.typo-body-medium-sm-150` | Medium (500) | 14px | 150% | 테이블 셀 / 기간 필터 텍스트 |
| `.typo-body-medium-sm-140` | Medium (500) | 14px | 140% | 사이드바 메뉴 ("대시보드", "배달 현황") |
| `.typo-body-medium-xs` | Medium (500) | 13px | Auto | 소셜 로그인 라벨 ("간편 소셜 로그인") |
| `.typo-body-medium-xxs` | Medium (500) | 12px | Auto | 타임스탬프 ("14:12 PM", "14:52") |
| `.typo-body-medium-xxs-130` | Medium (500) | 12px | 130% | 차트 라벨 ("월간 배달 건수", "월간 매출") |
| `.typo-body-medium-xxxs` | Medium (500) | 11px | Auto | 앱 버전 ("앱 버전 v1.0.0") / 하단 메뉴 라벨 |

---

### Body — Regular

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-body-regular-lg` | Regular (400) | 15px | Auto | 입력 필드 placeholder ("실명을 입력해주세요", "example@lumen.com") |
| `.typo-body-regular-lg-22` | Regular (400) | 15px | 22px | 본문 긴 텍스트 ("복잡한 접수 과정 없이 우리 집 문앞에 두면 바로 픽업") |
| `.typo-body-regular-md` | Regular (400) | 14px | Auto | 주소 상세 ("아크플레이스 14층", "사랑 빌딩 302호") |
| `.typo-body-regular-sm` | Regular (400) | 13px | Auto | 약관 텍스트 ("[필수] 이용약관 및 가상결제 서비스 동의") |
| `.typo-body-regular-sm-18` | Regular (400) | 13px | 18px | 알림 본문 긴 메시지 |
| `.typo-body-regular-xs` | Regular (400) | 12px | Auto | 메모 / 참고 텍스트 |
| `.typo-body-regular-xxs` | Regular (400) | 11px | Auto | 타임스탬프 ("2분 전", "1시간 전") |
| `.typo-body-regular-xxxs` | Regular (400) | 10px | Auto | 구분자 ("｜") / 최소 크기 텍스트 |

---

### Table 전용

| Style Class | Font Weight | Font Size | Line Height | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `.typo-table-header` | Semi Bold (600) | 13px | 140% | 테이블 헤더 ("정산번호", "배달원명") |
| `.typo-table-status` | Semi Bold (600) | 12px | 120% | 테이블 상태 배지 ("정산완료") |

---

### 요약

| 항목 | 값 |
| :--- | :--- |
| **사용 폰트** | Inter (전체 통일) |
| **총 스타일 수** | 81개 |
| **Weight 범위** | Regular (400) ~ Extra Bold (800) |
| **Size 범위** | 10px ~ 40px |
| **Line Height** | 대부분 Auto, 일부 고정 (18px, 22px, 120%, 130%, 140%, 150%) |

---


## 5. Spacing & Layout Tokens (여백 규격)

---

### Screen / Frame 기준

| 토큰명 | 값 | 주요 사용처 |
| :--- | :--- | :--- |
| **screen-mobile** | `412 × 917px` | 모바일 화면 기본 사이즈 |
| **screen-desktop** | `1440 × 1024px` | 어드민 데스크탑 화면 |
| **screen-desktop-sm** | `1440 × 900px` | 어드민 보조 화면 |

---

### Container Padding (내부 여백)

| 토큰명 | 값 | 주요 사용처 |
| :--- | :--- | :--- |
| **padding-none** | `0px` | 여백 없음 (기본값) |
| **padding-xxs** | `4px 8px` | 상태 배지 |
| **padding-xs** | `4px 10px` | 소형 태그 / 칩 |
| **padding-sm** | `6px 12px` | 보조 버튼 / 필터 칩 |
| **padding-md** | `8px 12px` | 사이즈 선택 칩 |
| **padding-md-alt** | `8px 20px` | 화면 상단 헤더 |
| **padding-base** | `10px 16px` | 큰 배지 / 리스트 아이템 |
| **padding-lg** | `12px 16px` | 알림 카드 / 검색 바 |
| **padding-xl** | `16px 16px` | 일반 카드 |
| **padding-xl-wide** | `16px 20px` | 메뉴 항목 / 입력 필드 |
| **padding-xl-wider** | `16px 24px` | 테이블 행 / 데이터 리스트 |
| **padding-2xl** | `18px 18px` | 히스토리 카드 |
| **padding-3xl** | `20px 20px` | 일반 카드 / 콘텐츠 섹션 |
| **padding-4xl** | `24px 24px` | 소셜 로그인 섹션 / 큰 카드 |
| **padding-5xl** | `24px 32px` | 하단 탭 바 |
| **padding-container** | `0px 24px` | 화면 좌우 기본 여백 |
| **padding-table-row** | `14px 20px` | 테이블 행 |

---

### Item Spacing (요소 간 간격)

| 토큰명 | 값 | 주요 사용처 |
| :--- | :--- | :--- |
| **gap-xxs** | `2px` | 별점 아이콘 사이 |
| **gap-xs** | `4px` | 아이콘과 텍스트 사이 / 알림 본문 줄 간격 |
| **gap-sm** | `6px` | 상태 아이콘 그룹 |
| **gap-md** | `8px` | 가로 나열 요소 기본 간격 / 페이지네이션 |
| **gap-base** | `10px` | 입력 필드 내부 / 출발지-도착지 간격 |
| **gap-lg** | `12px` | 라벨 → 입력창 간격 |
| **gap-xl** | `16px` | 입력창 → 입력창 간격 / 섹션 내 요소 간격 |
| **gap-2xl** | `20px` | 버튼 간 간격 / 소셜 로그인 버튼 사이 |
| **gap-3xl** | `24px` | 섹션 간 간격 |
| **gap-4xl** | `32px` | 큰 섹션 간 간격 / 폼 그룹 사이 |
| **gap-section-sm** | `25px` | 소셜 로그인 영역 내부 |
| **gap-section-md** | `30px` | 로그인 상단 영역 내부 |
| **gap-section-lg** | `49px` | 역할 선택 화면 내부 |
| **gap-hero-sm** | `70px` | 온보딩 일러스트와 텍스트 사이 |
| **gap-hero-md** | `80px` | 워크스루 일러스트와 텍스트 사이 |
| **gap-hero-lg** | `90px` | 온보딩 카드 내부 |
| **gap-hero-xl** | `100px` | 스플래시 로고와 텍스트 사이 |

---

### Corner Radius (모서리 둥글기)

| 토큰명 | 값 | 주요 사용처 |
| :--- | :--- | :--- |
| **radius-xs** | `3px` | 슬라이더 바 / 프로그레스 바 |
| **radius-sm** | `4px` | 테이블 셀 / 작은 입력창 |
| **radius-md** | `6px` | 상태 배지 / 소형 태그 |
| **radius-base** | `8px` | 드롭다운 / 작은 카드 |
| **radius-lg** | `10px` | 중간 카드 / 이미지 박스 |
| **radius-xl** | `12px` | 입력 필드 기본 둥글기 |
| **radius-2xl** | `15px` | 메인 버튼 둥글기 |
| **radius-3xl** | `16px` | 일러스트 박스 / 큰 카드 |
| **radius-4xl** | `20px` | 아이콘 박스 / 중형 카드 |
| **radius-5xl** | `30px` | 프로필 카드 / 대형 카드 |
| **radius-6xl** | `35px` | 큰 라운드 카드 |
| **radius-7xl** | `40px` | 온보딩 화면 / 대형 라운드 카드 |
| **radius-8xl** | `50px` | 역할 선택 카드 / 대형 버튼 |
| **radius-full** | `100px` | 완전 둥근 형태 (칩, 탭 버튼, 둥근 아이콘 버튼) |

---

### Component Spacing (컴포넌트별 간격)

| 토큰명 | 값 | 주요 사용처 |
| :--- | :--- | :--- |
| **button-padding** | `16px 24px` | 기본 버튼 내부 여백 (컨테이너 여백: 31px) |
| **button-gap** | `20px` | 버튼 간 간격 |
| **input-padding** | `16px 20px` | 입력 필드 내부 여백 |
| **label-input-gap** | `12px` | 라벨과 입력창 사이 |
| **input-input-gap** | `16px` | 입력창과 입력창 사이 |
| **card-padding** | `20px` | 카드 내부 여백 (간격 25px) |

---

### 요약

| 항목 | 값 |
| :--- | :--- |
| **기본 간격 단위** | 4px (대부분 4의 배수) |
| **Padding 범위** | 4px ~ 32px |
| **Gap 범위** | 2px ~ 100px |
| **Radius 범위** | 3px ~ 100px |
| **모바일 화면** | 412 × 917px |
| **데스크탑 화면** | 1440 × 1024px |


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

## 8. Card & Badge Components (카드 및 배치)

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

> 💡 **Spring Form 바인딩 팁 (th:field)**  
> Spring DTO 바인딩이 필요한 폼 개발 시, `<input class="input-field" th:field="*{email}">` 형태로 `th:field`를 직접 사용하면 `id`, `name`, `value`가 바인딩 객체 사양에 따라 자동 생성됩니다.

