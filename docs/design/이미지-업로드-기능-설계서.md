# 설계서: 이미지 업로드 (Image Upload)

이 문서는 물품 사진 등 이미지 파일을 서버에 업로드하고 접근 가능한 URL을 반환하는 공통 기능에 대한 설계 문서입니다. 특정 도메인에 종속되지 않는 범용 업로드 API라 `upload`라는 독립 패키지로 둔다(`common`은 전역 설정/예외 처리기 전용이라 이 기능을 넣지 않음).

---

## 1. 요구사항 정의서 (User Story)

* **User Story:**
  우리는 **배송을 신청하는 고객**으로서, 물품 사진을 서버에 올리고 그 접근 URL을 돌려받아 **다른 API(배송 신청 등)에 함께 제출**하기를 원한다.
* **성공 기준 (Acceptance Criteria):**
  1. `jpg`/`jpeg`/`png`/`webp` 확장자의 이미지를 업로드하면, UUID 기반 파일명으로 로컬 디스크에 저장되고 접근 가능한 URL이 반환된다.
  2. 허용되지 않은 확장자(예: `exe`)를 업로드하면 `400 Bad Request`(`INVALID_FILE_TYPE`)를 반환한다.
  3. 5MB를 초과하는 파일을 업로드하면 `400 Bad Request`(`FILE_TOO_LARGE`)를 반환한다.
  4. 이 API는 DB에 아무것도 저장하지 않는다(파일 시스템 저장 + URL 반환만 담당하는 상태 없는 유틸리티 성격의 API).

---

## 2. ERD 설계 (Entity-Relationship Diagram)

신규 테이블 없음 — 업로드 이력을 DB에 남기지 않는다(이번 범위 밖).

---

## 3. API 명세서 (API Specification)

### 3.1 이미지 업로드
* **엔드포인트:** `POST /api/v1/uploads/image` (multipart/form-data)
  * (이슈 원문은 `POST /api/uploads/image`이나, 이 저장소의 실제 컨벤션 `/api/v1/...`에 맞춤)
* **요청:** `file` 파트(`@RequestParam("file") MultipartFile`)
* **응답 바디 및 상태 코드 (Response Body & Status):**
  * **Success (201 Created):**
    ```json
    { "imageUrl": "/uploads/3fa85f64-5717-4562-b3fc-2c963f66afa6.jpg" }
    ```
  * **Failure (400 Bad Request - 허용되지 않은 확장자, ErrorCode `C005`):**
    ```json
    { "status": 400, "code": "C005", "message": "허용되지 않는 파일 형식입니다: exe", "timestamp": "..." }
    ```
  * **Failure (400 Bad Request - 5MB 초과, ErrorCode `C006`):**
    ```json
    { "status": 400, "code": "C006", "message": "파일 크기가 5MB를 초과했습니다.", "timestamp": "..." }
    ```
* **검증 순서:** 빈 파일 → 확장자 화이트리스트(`jpg`, `jpeg`, `png`, `webp`) → 크기(5MB) 순으로 검증한다.
* **저장 방식:** `UUID.randomUUID() + 확장자`로 파일명을 만들어 `app.upload.dir`(기본값 `uploads/`) 아래 로컼 디스크에 저장하고, `WebMvcConfigurer.addResourceHandlers`로 `/uploads/**` 정적 리소스 서빙을 설정해 반환된 URL로 바로 접근 가능하게 한다.
* **Spring 멀티파트 크기 제한:** `spring.servlet.multipart.max-file-size`를 10MB로 설정해, 5~10MB 사이 파일은 Tomcat/Spring 레벨에서 거절되지 않고 우리 서비스 코드까지 도달해 `FILE_TOO_LARGE`로 일관된 에러 응답을 받도록 한다(10MB 초과는 이번 범위 밖 — Spring 기본 예외로 처리됨).

---

## 4. 작업 분할 목록 (WBS)

- [ ] `ErrorCode`에 `INVALID_FILE_TYPE`(C005), `FILE_TOO_LARGE`(C006) 추가
- [ ] `InvalidFileTypeException`, `FileTooLargeException`(`BusinessException` 상속) 추가
- [ ] `ImageUploadResponse` record 추가
- [ ] `UploadWebConfig`(정적 리소스 서빙 설정) + `application.yaml`에 `app.upload.dir`/`base-url`, 멀티파트 크기 설정 추가
- [ ] `FileUploadService.upload()`: 확장자/크기 검증 + UUID 파일명 저장 + URL 반환
- [ ] `FileUploadController`: `POST /api/v1/uploads/image`
- [ ] 단위 테스트(정상 jpg 업로드, 허용 안된 확장자 400, 5MB 초과 400) — 실제 디스크에 쓰되 JUnit `@TempDir`로 격리
