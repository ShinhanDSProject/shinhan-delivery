-- ============================================================================
-- shinhan-delivery : 스키마 초기화 스크립트 (Schema Init)
-- ----------------------------------------------------------------------------
-- 대상 DBMS : MariaDB 10.6+ (MySQL 8.0 호환)
-- 기준 시점 : Flyway 마이그레이션 V1 ~ V32 전체 적용 후의 최종 스키마
-- 생성 근거 : src/main/resources/db/migration/V1~V32__*.sql 을 하나로 통합(squash)
--
-- [이 파일의 용도]
--   외부 제공/리뷰용, 로컬 샌드박스 DB 구축, ERD 분석 등 "한 파일로 전체 스키마를
--   재현"해야 할 때 사용합니다.
--
-- [주의] 운영/로컬 애플리케이션 DB의 스키마 변경은 반드시 Flyway 마이그레이션
--        (V{n}__설명.sql 신규 파일 추가)으로만 수행해야 합니다. 이 파일을 수정해
--        스키마를 바꾸면 Flyway 이력과 어긋납니다.
--        (docs/architecture/Flyway-마이그레이션-가이드.md 참고)
--
-- [실행 방법]
--   mysql -u root -p shinhan_delivery < scripts/sql/schema-init.sql
--
-- [이 파일로 만든 DB에 애플리케이션을 붙이려면]
--   Flyway가 V1부터 다시 적용하려다 "테이블 이미 존재" 오류를 냅니다.
--   아래처럼 Flyway를 끄고 기동하세요.
--     SPRING_FLYWAY_ENABLED=false ./gradlew bootRun
--   (Flyway를 정상적으로 쓰려면 빈 DB에서 ./gradlew bootRun 으로 마이그레이션하세요.)
-- ============================================================================

SET NAMES utf8mb4;

-- 필요 시 주석을 해제해 데이터베이스부터 생성합니다. (한글 데이터가 있어 utf8mb4 필수)
-- CREATE DATABASE IF NOT EXISTS shinhan_delivery
--   DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE shinhan_delivery;

-- 필요 시 주석을 해제해 기존 테이블을 정리합니다. (FK 역순 / 데이터가 모두 삭제됩니다)
-- DROP TABLE IF EXISTS point_history;
-- DROP TABLE IF EXISTS point_wallet;
-- DROP TABLE IF EXISTS matching;
-- DROP TABLE IF EXISTS delivery_request;
-- DROP TABLE IF EXISTS vehicle;
-- DROP TABLE IF EXISTS notification;
-- DROP TABLE IF EXISTS address;
-- DROP TABLE IF EXISTS notice;
-- DROP TABLE IF EXISTS category;
-- DROP TABLE IF EXISTS diary;
-- DROP TABLE IF EXISTS member;

-- ============================================================================
-- 1. member : 회원 (role로 CUSTOMER / COURIER / ADMIN 구분)
--    유래: V2(생성), V21(배송원 프로필), V24(결제 PIN), V28(배송원 자격 심사)
-- ============================================================================
CREATE TABLE member (
    id                      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    email                   VARCHAR(254) NOT NULL UNIQUE COMMENT '로그인 ID로 쓰이는 이메일',
    password                VARCHAR(100) NOT NULL COMMENT 'BCrypt 해시 (평문 저장 금지)',
    name                    VARCHAR(50)  NOT NULL,
    phone_number            VARCHAR(20)  NOT NULL,
    role                    VARCHAR(20)  NOT NULL COMMENT 'CUSTOMER / COURIER / ADMIN',
    activity_region         VARCHAR(100) NULL     COMMENT '배송원 희망 활동 지역',
    preferred_weight        DOUBLE       NULL     COMMENT '배송원 선호 최대 중량(kg)',
    pin_hash                VARCHAR(255) NULL     COMMENT '결제 PIN(6자리) BCrypt 해시',
    pin_fail_count          INT          NOT NULL DEFAULT 0 COMMENT 'PIN 연속 실패 횟수(3회 시 잠금)',
    pin_locked              BOOLEAN      NOT NULL DEFAULT FALSE,
    courier_approval_status VARCHAR(20)  NOT NULL DEFAULT 'APPROVED' COMMENT 'PENDING / APPROVED / REJECTED',
    proof_document_url      VARCHAR(255) NULL     COMMENT '배송원 자격 증빙 서류 이미지 URL'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 2. vehicle : 배송 운송수단(장비)
--    유래: V3(생성), V7(위치·상태), V25(owner_id → member_id), V31(자격·활성 필드)
-- ============================================================================
CREATE TABLE vehicle (
    id                   BIGINT      AUTO_INCREMENT PRIMARY KEY,
    member_id            BIGINT      NOT NULL COMMENT '장비 소유 배송원 (member.id)',
    name                 VARCHAR(100) NULL    COMMENT '배송원이 지정한 장비 별칭',
    type                 VARCHAR(20) NOT NULL COMMENT 'DRONE / MOTORCYCLE / CAR / BICYCLE / KICKBOARD / WALK',
    max_weight           DOUBLE      NOT NULL COMMENT '최대 적재 중량(kg)',
    max_distance         DOUBLE      NOT NULL COMMENT '최대 운행 거리(km)',
    latitude             DOUBLE      NOT NULL DEFAULT 0 COMMENT '현재 위도',
    longitude            DOUBLE      NOT NULL DEFAULT 0 COMMENT '현재 경도',
    status               VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE / BUSY',
    approval_status      VARCHAR(20) NOT NULL DEFAULT 'APPROVED' COMMENT 'PENDING / APPROVED / REJECTED',
    is_active            BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '배송원이 켜 둔 운행 장비 여부',
    license_plate_number VARCHAR(50) NULL     COMMENT '차량 번호판',
    insurance_photo_url  LONGTEXT    NULL     COMMENT '보험 증빙 이미지',
    photo_url            LONGTEXT    NULL     COMMENT '장비 사진',
    displacement         INT         NULL     COMMENT '배기량(cc)',
    CONSTRAINT fk_vehicle_owner FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 3. delivery_request : 배송 요청 (배송의 수명주기 전체를 담는 핵심 테이블)
--    유래: V4(생성), V7·V9(좌표·크기), V14~V18(사진·시각·백필), V19·V20(조회 인덱스),
--          V23(결제 멱등키), V26(낙관적 락), V27(전달 방식), V29(타임아웃 환불 감사),
--          V30(픽업 사진), V32(단계별 취소 정산)
-- ============================================================================
CREATE TABLE delivery_request (
    id                           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id                    BIGINT       NOT NULL COMMENT '배송을 요청한 고객 (member.id)',
    pickup_address               VARCHAR(255) NOT NULL,
    dropoff_address              VARCHAR(255) NOT NULL,
    weight                       DOUBLE       NOT NULL COMMENT '물품 무게(kg)',
    distance                     DOUBLE       NOT NULL COMMENT '픽업~도착 하버사인 거리(km)',
    status                       VARCHAR(20)  NOT NULL COMMENT 'REQUESTED / MATCHED / PICKED_UP / COMPLETED / CANCELLED',
    fee_point                    BIGINT       NOT NULL COMMENT '결제된 배송비(포인트)',
    pickup_latitude              DOUBLE       NOT NULL DEFAULT 0,
    pickup_longitude             DOUBLE       NOT NULL DEFAULT 0,
    dropoff_latitude             DOUBLE       NOT NULL DEFAULT 0,
    dropoff_longitude            DOUBLE       NOT NULL DEFAULT 0,
    item_size                    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM' COMMENT 'SMALL / MEDIUM / LARGE',
    proof_photo_url              VARCHAR(255) NULL     COMMENT '배송 완료 인증 사진',
    completed_at                 TIMESTAMP    NULL,
    picked_up_at                 TIMESTAMP    NULL,
    created_at                   TIMESTAMP    NULL,
    payment_idempotency_key      VARCHAR(100) NULL     COMMENT '배송 결제 멱등 키 (중복 결제 방지)',
    version                      BIGINT       NOT NULL DEFAULT 0 COMMENT 'JPA 낙관적 락 버전',
    delivery_instruction_type    VARCHAR(40)  NOT NULL DEFAULT 'NONE' COMMENT 'NONE / LEAVE_AT_DOOR_NO_BELL / ENTRANCE_CODE / SECURITY_OFFICE / CUSTOM',
    entrance_code                VARCHAR(100) NULL     COMMENT '공동현관 비밀번호(민감정보)',
    unit_detail                  VARCHAR(100) NULL     COMMENT '동/호수 등 현장 상세 위치',
    delivery_note                VARCHAR(500) NULL     COMMENT '배송원에게 전달할 요청사항',
    delivery_reference_photo_url VARCHAR(255) NULL     COMMENT '전달 위치 참고 사진',
    cancellation_reason          VARCHAR(30)  NULL     COMMENT 'AUTO_TIMEOUT / CUSTOMER_REQUEST',
    cancelled_at                 TIMESTAMP    NULL,
    refunded_at                  TIMESTAMP    NULL,
    timeout_retry_count          INT          NOT NULL DEFAULT 0 COMMENT '자동 타임아웃 처리 실패 재시도 횟수',
    timeout_next_retry_at        TIMESTAMP    NULL     COMMENT '백오프 종료 시각(이전에는 배치 후보 제외)',
    pickup_photo_url             VARCHAR(255) NULL     COMMENT '픽업 시 물품 확인 사진',
    cancellation_fee             BIGINT       NULL     COMMENT '고객 취소 수수료(MATCHED 이후 1,000P)',
    refund_amount                BIGINT       NULL     COMMENT '고객에게 환불한 포인트',
    courier_compensation         BIGINT       NULL     COMMENT '배정 배송원에게 지급한 이동 보상',
    cancelled_by_member_id       BIGINT       NULL     COMMENT '취소를 실행한 회원 (member.id)',
    compensated_at               TIMESTAMP    NULL,
    cancellation_previous_status VARCHAR(20)  NULL     COMMENT '취소 직전 상태(정산 근거)',
    CONSTRAINT fk_delivery_request_customer FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 고객별 배송 목록 커서 페이징용 (id를 tie-breaker로 포함 / V20)
CREATE INDEX idx_delivery_request_customer_created_id
    ON delivery_request (member_id, created_at, id);
CREATE INDEX idx_delivery_request_customer_status_created_id
    ON delivery_request (member_id, status, created_at, id);
-- 동일 고객의 동일 멱등 키 재결제 차단 (V23)
CREATE UNIQUE INDEX uq_delivery_request_customer_payment_key
    ON delivery_request (member_id, payment_idempotency_key);
-- 자동 타임아웃 배치의 후보 조회용 (V29)
CREATE INDEX idx_delivery_request_timeout
    ON delivery_request (status, timeout_next_retry_at, created_at, id);

-- ============================================================================
-- 4. matching : 배송 요청과 운송수단의 매칭 (배송 요청 1건당 최대 1건)
--    유래: V5(생성)
-- ============================================================================
CREATE TABLE matching (
    id                  BIGINT      AUTO_INCREMENT PRIMARY KEY,
    delivery_request_id BIGINT      NOT NULL,
    vehicle_id          BIGINT      NOT NULL,
    status              VARCHAR(20) NOT NULL COMMENT 'MATCHED / COMPLETED / CANCELLED',
    matched_at          DATETIME    NOT NULL,
    CONSTRAINT uq_matching_delivery_request UNIQUE (delivery_request_id),
    CONSTRAINT fk_matching_delivery_request FOREIGN KEY (delivery_request_id) REFERENCES delivery_request (id),
    CONSTRAINT fk_matching_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 5. point_wallet : 회원별 포인트 지갑 (회원 1명당 1개)
--    유래: V6(생성), V22(낙관적 락 version)
-- ============================================================================
CREATE TABLE point_wallet (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    balance   BIGINT NOT NULL DEFAULT 0 COMMENT '현재 포인트 잔액',
    version   BIGINT NOT NULL DEFAULT 0 COMMENT 'JPA 낙관적 락 버전',
    CONSTRAINT uq_point_wallet_member UNIQUE (member_id),
    CONSTRAINT fk_point_wallet_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 6. point_history : 포인트 충전/사용/환불/보상 원장
--    유래: V22(생성), V29(환불 참조·설명·중복 환불 방지 인덱스)
--    amount는 항상 양수이며 입출금 방향은 type으로 구분합니다.
-- ============================================================================
CREATE TABLE point_history (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT       NOT NULL,
    wallet_id       BIGINT       NOT NULL,
    amount          BIGINT       NOT NULL COMMENT '거래 금액(항상 양수)',
    balance_after   BIGINT       NOT NULL COMMENT '거래 직후 잔액',
    type            VARCHAR(20)  NOT NULL COMMENT 'CHARGE / USE / REFUND / COURIER_COMPENSATION',
    payment_method  VARCHAR(30)  NULL     COMMENT 'CARD / BANK_TRANSFER / EASY_PAY (충전 시에만)',
    idempotency_key VARCHAR(100) NOT NULL COMMENT '중복 거래 방지 키',
    created_at      TIMESTAMP    NOT NULL,
    reference_id    BIGINT       NULL     COMMENT '환불/보상의 원 배송 요청 ID',
    description     VARCHAR(100) NULL,
    CONSTRAINT uq_point_history_member_key UNIQUE (member_id, idempotency_key),
    -- 같은 배송에 대한 환불/보상이 두 번 적립되지 않게 막는다 (reference_id가 NULL인 충전·사용 이력은 서로 충돌하지 않음)
    CONSTRAINT uq_point_history_refund_reference UNIQUE (type, reference_id),
    CONSTRAINT fk_point_history_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_point_history_wallet FOREIGN KEY (wallet_id) REFERENCES point_wallet (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 7. notification : 회원별 알림 (FK 없이 member_id만 보관)
--    유래: V10(생성), V11(조회 인덱스)
-- ============================================================================
CREATE TABLE notification (
    id        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT       NOT NULL,
    title     VARCHAR(100) NOT NULL,
    message   VARCHAR(500) NOT NULL,
    category  VARCHAR(50)  NOT NULL COMMENT 'MATCHING / DELIVERY / POINT 등',
    is_read   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME    NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_notification_member_created
    ON notification (member_id, created_at);
CREATE INDEX idx_notification_member_category_created
    ON notification (member_id, category, created_at);

-- ============================================================================
-- 8. address : 회원 자주 쓰는 주소 (FK 없이 member_id만 보관)
--    유래: V12(생성)
-- ============================================================================
CREATE TABLE address (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT       NOT NULL,
    alias          VARCHAR(50)  NOT NULL COMMENT '집, 회사 등 주소 별칭',
    address        VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255) NULL,
    pickup_guide   VARCHAR(255) NULL     COMMENT '픽업 안내 메모'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 9. notice : 공지사항
--    유래: V13(생성 + 기준 데이터)
-- ============================================================================
CREATE TABLE notice (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(150) NOT NULL,
    content    TEXT         NOT NULL,
    category   VARCHAR(50)  NOT NULL COMMENT 'SYSTEM / EVENT / SERVICE',
    is_pinned  BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '상단 고정 여부',
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_notice_pinned_created ON notice (is_pinned DESC, created_at DESC);

-- ============================================================================
-- 10. category : 배송 물품 카테고리 (기준 데이터)
--     유래: V8(생성 + 기준 데이터)
-- ============================================================================
CREATE TABLE category (
    id   BIGINT      AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 11. diary : V1 초기 스키마의 잔존 테이블
--     현재 애플리케이션 코드에서 참조하지 않지만, Flyway로 구축한 DB와 동일한
--     상태를 재현하기 위해 그대로 포함합니다.
-- ============================================================================
CREATE TABLE diary (
    id         VARCHAR(36)   PRIMARY KEY,
    date       BIGINT        NOT NULL,
    content    VARCHAR(2000) NOT NULL,
    emotion_id INT           NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 12. 기준 데이터 (Master Data)
--     ----------------------------------------------------------------------
--     아래 두 블록은 Flyway 마이그레이션(V8 / V13)이 스키마와 함께 적재하는
--     "애플리케이션 동작에 필요한 기준 데이터"입니다. 테스트용 더미 데이터가
--     아니므로 이 초기화 파일에 포함합니다.
--     (회원·배송·포인트 등 테스트 데이터는 scripts/sql/dummy-data.sql 참고)
-- ============================================================================

-- 12-1. 배송 물품 카테고리 12종 (V8)
INSERT INTO category (name) VALUES
    ('전자기기/가전'),
    ('식품/음료'),
    ('의류/패션잡화'),
    ('서류/문서'),
    ('생활용품/잡화'),
    ('가구/인테리어'),
    ('화장품/뷰티'),
    ('도서/음반'),
    ('스포츠/레저'),
    ('반려동물 용품'),
    ('꽃/식물'),
    ('기타');

-- 12-2. 초기 공지사항 3건 (V13)
INSERT INTO notice (title, content, category, is_pinned, created_at, updated_at) VALUES
('[안내] 딜리버리 해피니스 서비스 정기 점검 안내', '안녕하세요. 딜리버리 해피니스 서비스의 안정적인 운영을 위한 정기 점검이 진행될 예정입니다.\n\n■ 점검 일시: 2026년 8월 1일 02:00 ~ 06:00 (4시간)\n■ 영향 범위: 서비스 전체 이용 및 배송 요청 일시 중단\n\n이용에 불편을 드려 죄송합니다.', 'SYSTEM', true, NOW(), NOW()),
('[이벤트] 신규 가입 회원 대상 배송 할인 쿠폰 지급', '신규 회원가입 고객님들을 위한 첫 배송 3,000원 할인 쿠폰이 일괄 발급되었습니다.\n마이페이지 > 쿠폰함에서 확인해 보세요!', 'EVENT', false, NOW(), NOW()),
('[안내] 실시간 위치 추적 기능 업데이트 안내', '배송 상태 및 이동 경로를 실시간 지도 화면에서 확인할 수 있는 위치 추적 기능이 업데이트되었습니다.', 'SERVICE', false, NOW(), NOW());
