-- ============================================================================
-- shinhan-delivery : 로컬/테스트용 더미 데이터 스크립트 (Dummy Data)
-- ----------------------------------------------------------------------------
-- 전제 조건 : 스키마가 이미 구축된 DB (Flyway V1~V32 적용 완료 또는
--             scripts/sql/schema-init.sql 실행 완료)
--
-- [주의] 로컬/개발용 DB에서만 실행하세요. 맨 앞의 초기화(DELETE) 블록이
--        member.id 1~7 및 그에 딸린 배송·포인트·알림·주소 데이터를 삭제하므로,
--        운영 DB나 실제 데이터가 있는 DB에서는 절대 실행하면 안 됩니다.
--
-- [실행 방법]
--   mysql -u root -p shinhan_delivery < scripts/sql/dummy-data.sql
--   (재실행해도 같은 상태가 되도록 초기화 후 적재하는 구조입니다)
--
-- [포함하지 않는 데이터]
--   category(12종), notice(초기 3건)은 기준 데이터이므로 schema-init.sql 또는
--   Flyway 마이그레이션(V8/V13)이 이미 적재합니다. 여기서는 건드리지 않습니다.
--
-- ----------------------------------------------------------------------------
-- [테스트 계정] 비밀번호는 모두 password123 (BCrypt 해시로 저장)
--   admin@shinhan.com     관리자     ADMIN     - 관리자 승인 화면 확인용
--   client@example.com    김화주     CUSTOMER  - 결제 PIN 135790 / 잔액 466,709P
--   customer2@example.com 최고객     CUSTOMER  - 완료·취소 이력 보유
--   customer3@example.com 정단골     CUSTOMER  - 자동 타임아웃 환불 이력 보유
--   courier1@example.com  박배송     COURIER   - 승인 완료, 차량 2대
--   courier2@example.com  이배송     COURIER   - 승인 완료, 차량 2대
--   courier3@example.com  신입배송   COURIER   - 승인 대기(PENDING), 장비도 심사 대기
--
-- [배송 데이터 시나리오] delivery_request 8건으로 전 상태를 재현합니다.
--   1 REQUESTED  (결제 완료, 매칭 대기)     5 CANCELLED (고객 취소 / 수수료 1,000P 정산)
--   2 MATCHED    (배송원 수락, 추적 가능)   6 CANCELLED (30분 미배정 자동 취소 / 전액 환불)
--   3 PICKED_UP  (픽업 완료, 배송 중)       7 REQUESTED (미결제 요청)
--   4 COMPLETED  (완료, 인증 사진 보유)     8 COMPLETED (완료, 커스텀 전달 요청)
--
-- 배송비(fee_point)는 DeliveryFeeCalculator 공식(기본 3,000 + 거리 500/km +
-- 무게 200/kg, 크기 할증 SMALL 0% / MEDIUM 30% / LARGE 60%)으로 계산한 값이며,
-- point_history 원장의 잔액 흐름과 point_wallet.balance도 서로 일치합니다.
-- ============================================================================

SET NAMES utf8mb4;

START TRANSACTION;

-- ============================================================================
-- 0. 기존 더미 데이터 초기화 (FK 제약을 지키는 역순으로 삭제)
-- ============================================================================
DELETE FROM point_history WHERE member_id BETWEEN 1 AND 7;
DELETE FROM point_wallet  WHERE member_id BETWEEN 1 AND 7;
DELETE FROM matching      WHERE delivery_request_id IN (
    SELECT id FROM delivery_request WHERE member_id BETWEEN 1 AND 7
);
DELETE FROM delivery_request WHERE member_id BETWEEN 1 AND 7;
DELETE FROM vehicle         WHERE member_id BETWEEN 1 AND 7;
DELETE FROM notification    WHERE member_id BETWEEN 1 AND 7;
DELETE FROM address         WHERE member_id BETWEEN 1 AND 7;
DELETE FROM notice          WHERE id IN (4, 5);
DELETE FROM member          WHERE id BETWEEN 1 AND 7;

-- ============================================================================
-- 1. member : 회원 7명 (관리자 1 / 고객 3 / 배송원 3)
--    password  = BCrypt('password123')
--    pin_hash  = BCrypt('135790')  ← 김화주만 결제 PIN 등록 상태
-- ============================================================================
INSERT INTO member
    (id, email, password, name, phone_number, role, activity_region, preferred_weight,
     pin_hash, pin_fail_count, pin_locked, courier_approval_status, proof_document_url)
VALUES
    (1, 'admin@shinhan.com', '$2y$10$fzchmOlPci759PM62N7Ki.SJlvV0eCzcosBo/MCquJMxPfVzWc6Pm',
     '관리자', '010-0000-0000', 'ADMIN', NULL, NULL,
     NULL, 0, FALSE, 'APPROVED', NULL),
    (2, 'client@example.com', '$2y$10$fzchmOlPci759PM62N7Ki.SJlvV0eCzcosBo/MCquJMxPfVzWc6Pm',
     '김화주', '010-1234-5678', 'CUSTOMER', NULL, NULL,
     '$2y$10$duIH6.y2IMBWTR34/uVtAetZf8Vsm9uWOuIOBHjsPR7Mg4IkcqnQy', 0, FALSE, 'APPROVED', NULL),
    (3, 'customer2@example.com', '$2y$10$fzchmOlPci759PM62N7Ki.SJlvV0eCzcosBo/MCquJMxPfVzWc6Pm',
     '최고객', '010-2222-3333', 'CUSTOMER', NULL, NULL,
     NULL, 0, FALSE, 'APPROVED', NULL),
    (4, 'customer3@example.com', '$2y$10$fzchmOlPci759PM62N7Ki.SJlvV0eCzcosBo/MCquJMxPfVzWc6Pm',
     '정단골', '010-4444-5555', 'CUSTOMER', NULL, NULL,
     NULL, 0, FALSE, 'APPROVED', NULL),
    (5, 'courier1@example.com', '$2y$10$fzchmOlPci759PM62N7Ki.SJlvV0eCzcosBo/MCquJMxPfVzWc6Pm',
     '박배송', '010-2345-6789', 'COURIER', '서울 강남구', 100.0,
     NULL, 0, FALSE, 'APPROVED', '/uploads/seed/courier1-license.png'),
    (6, 'courier2@example.com', '$2y$10$fzchmOlPci759PM62N7Ki.SJlvV0eCzcosBo/MCquJMxPfVzWc6Pm',
     '이배송', '010-3456-7890', 'COURIER', '서울 성동구', 30.0,
     NULL, 0, FALSE, 'APPROVED', '/uploads/seed/courier2-license.png'),
    (7, 'courier3@example.com', '$2y$10$fzchmOlPci759PM62N7Ki.SJlvV0eCzcosBo/MCquJMxPfVzWc6Pm',
     '신입배송', '010-5555-6666', 'COURIER', '서울 마포구', 20.0,
     NULL, 0, FALSE, 'PENDING', NULL);

-- ============================================================================
-- 2. vehicle : 배송 장비 5대
--    운행 중(BUSY) 2대 / 대기(AVAILABLE) 2대 / 관리자 승인 대기(PENDING) 1대
-- ============================================================================
INSERT INTO vehicle
    (id, member_id, name, type, max_weight, max_distance, latitude, longitude,
     status, approval_status, is_active, license_plate_number,
     insurance_photo_url, photo_url, displacement)
VALUES
    -- 박배송(5)의 1.5톤 트럭 : 배송 요청 2번을 수행 중이라 BUSY
    (1, 5, '1.5톤 트럭', 'CAR', 1500.0, 300.0, 37.5045, 127.0410,
     'BUSY', 'APPROVED', TRUE, '12가 3456',
     '/uploads/seed/vehicle1-insurance.png', '/uploads/seed/vehicle1.png', 2500),
    -- 박배송(5)의 예비 오토바이 : 승인 완료, 현재 운행 OFF
    (2, 5, '배송용 오토바이', 'MOTORCYCLE', 50.0, 50.0, 37.5006, 127.0364,
     'AVAILABLE', 'APPROVED', FALSE, '서울 강남 가 1234',
     '/uploads/seed/vehicle2-insurance.png', '/uploads/seed/vehicle2.png', 125),
    -- 이배송(6)의 퀵 오토바이 : 배송 요청 3번을 픽업해 이동 중이라 BUSY
    (3, 6, '퀵 오토바이', 'MOTORCYCLE', 30.0, 40.0, 37.5290, 127.0350,
     'BUSY', 'APPROVED', TRUE, '서울 성동 나 5678',
     '/uploads/seed/vehicle3-insurance.png', '/uploads/seed/vehicle3.png', 110),
    -- 이배송(6)의 도보 배송 : 근거리 소형 물품 전용
    (4, 6, '도보 배송', 'WALK', 5.0, 3.0, 37.5445, 127.0557,
     'AVAILABLE', 'APPROVED', FALSE, NULL, NULL, NULL, NULL),
    -- 신입배송(7)의 드론 : 관리자 승인 대기 상태 (관리자 승인 화면 테스트용)
    (5, 7, '배송 드론 1호', 'DRONE', 10.0, 20.0, 37.5299, 126.9648,
     'AVAILABLE', 'PENDING', FALSE, NULL,
     '/uploads/seed/vehicle5-insurance.png', '/uploads/seed/vehicle5.png', NULL);

-- ============================================================================
-- 3. delivery_request : 배송 요청 8건 (REQUESTED ~ CANCELLED 전 상태 재현)
-- ============================================================================
INSERT INTO delivery_request
    (id, member_id, pickup_address, dropoff_address, weight, distance, status, fee_point,
     pickup_latitude, pickup_longitude, dropoff_latitude, dropoff_longitude, item_size,
     proof_photo_url, completed_at, picked_up_at, created_at, payment_idempotency_key, version,
     delivery_instruction_type, entrance_code, unit_detail, delivery_note,
     delivery_reference_photo_url, cancellation_reason, cancelled_at, refunded_at,
     timeout_retry_count, timeout_next_retry_at, pickup_photo_url,
     cancellation_fee, refund_amount, courier_compensation, cancelled_by_member_id,
     compensated_at, cancellation_previous_status)
VALUES
    -- (1) REQUESTED : 결제까지 끝내고 배송원 매칭을 기다리는 상태
    (1, 2, '서울특별시 강남구 테헤란로 152', '서울특별시 서초구 서초대로 398',
     3.0, 1.120378, 'REQUESTED', 5408,
     37.5006, 127.0364, 37.4923, 127.0292, 'MEDIUM',
     NULL, NULL, NULL, NOW() - INTERVAL 10 MINUTE, 'seed-delivery-pay-1', 0,
     'NONE', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL,
     0, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    -- (2) MATCHED : 박배송의 트럭이 수락한 상태 (실시간 위치 추적 화면 테스트용)
    (2, 2, '서울특별시 강남구 테헤란로 152', '서울특별시 성동구 아차산로 17',
     10.0, 5.169688, 'MATCHED', 12136,
     37.5006, 127.0364, 37.5445, 127.0557, 'LARGE',
     NULL, NULL, NULL, NOW() - INTERVAL 2 HOUR, 'seed-delivery-pay-2', 1,
     'ENTRANCE_CODE', '1004#', '3동 1201호', '엘리베이터 이용해 문 앞에 놓아주세요.',
     NULL, NULL, NULL, NULL,
     0, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    -- (3) PICKED_UP : 이배송의 오토바이가 픽업을 마치고 이동 중인 상태
    (3, 2, '서울특별시 강남구 삼성로 511', '서울특별시 성동구 왕십리로 50',
     5.0, 2.634339, 'PICKED_UP', 5317,
     37.5172, 127.0473, 37.5326, 127.0246, 'SMALL',
     NULL, NULL, NOW() - INTERVAL 150 MINUTE, NOW() - INTERVAL 3 HOUR, 'seed-delivery-pay-3', 2,
     'LEAVE_AT_DOOR_NO_BELL', NULL, '101동 902호', '초인종 누르지 말고 문 앞에 둬 주세요.',
     NULL, NULL, NULL, NULL,
     0, NULL, '/uploads/seed/delivery3-pickup.png',
     NULL, NULL, NULL, NULL, NULL, NULL),

    -- (4) COMPLETED : 완료 + 인증 사진 보유 (배송 완료 상세 화면 테스트용)
    (4, 3, '서울특별시 강남구 강남대로 382', '서울특별시 강남구 봉은사로 524',
     2.0, 2.017223, 'COMPLETED', 4409,
     37.4979, 127.0276, 37.5045, 127.0489, 'SMALL',
     '/uploads/seed/delivery4-proof.png', NOW() - INTERVAL 4 DAY + INTERVAL 52 MINUTE,
     NOW() - INTERVAL 4 DAY + INTERVAL 21 MINUTE, NOW() - INTERVAL 4 DAY,
     'seed-delivery-pay-4', 3,
     'SECURITY_OFFICE', NULL, 'B동 1504호', '부재 시 경비실에 맡겨주세요.',
     NULL, NULL, NULL, NULL,
     0, NULL, '/uploads/seed/delivery4-pickup.png',
     NULL, NULL, NULL, NULL, NULL, NULL),

    -- (5) CANCELLED : MATCHED 상태에서 고객이 취소 → 수수료 1,000P 차감 후 7,221P 환불,
    --                 배정된 배송원(박배송)에게 이동 보상 1,000P 지급
    (5, 3, '서울특별시 종로구 세종대로 175', '서울특별시 중구 을지로 100',
     7.5, 3.647282, 'CANCELLED', 8221,
     37.5665, 126.9780, 37.5512, 127.0146, 'MEDIUM',
     NULL, NULL, NULL, NOW() - INTERVAL 2 DAY, 'seed-delivery-pay-5', 2,
     'NONE', NULL, NULL, NULL,
     NULL, 'CUSTOMER_REQUEST', NOW() - INTERVAL 2 DAY + INTERVAL 15 MINUTE,
     NOW() - INTERVAL 2 DAY + INTERVAL 15 MINUTE,
     0, NULL, NULL,
     1000, 7221, 1000, 3, NOW() - INTERVAL 2 DAY + INTERVAL 15 MINUTE, 'MATCHED'),

    -- (6) CANCELLED : 30분간 배송원이 배정되지 않아 스케줄러가 자동 취소 + 전액 환불
    --                 (자동 취소는 단계별 정산 컬럼을 채우지 않는다)
    (6, 4, '서울특별시 송파구 올림픽로 300', '서울특별시 강남구 남부순환로 2806',
     12.0, 6.540232, 'CANCELLED', 13872,
     37.5111, 127.0980, 37.4837, 127.0324, 'LARGE',
     NULL, NULL, NULL, NOW() - INTERVAL 3 DAY, 'seed-delivery-pay-6', 1,
     'NONE', NULL, NULL, NULL,
     NULL, 'AUTO_TIMEOUT', NOW() - INTERVAL 3 DAY + INTERVAL 31 MINUTE,
     NOW() - INTERVAL 3 DAY + INTERVAL 31 MINUTE,
     0, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    -- (7) REQUESTED : 포인트 결제를 거치지 않은 요청 (payment_idempotency_key가 NULL)
    (7, 4, '서울특별시 마포구 양화로 45', '서울특별시 영등포구 여의대로 108',
     1.5, 6.031278, 'REQUESTED', 6316,
     37.5299, 126.9648, 37.5088, 126.9018, 'SMALL',
     NULL, NULL, NULL, NOW() - INTERVAL 5 MINUTE, NULL, 0,
     'NONE', NULL, NULL, NULL,
     NULL, NULL, NULL, NULL,
     0, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL, NULL),

    -- (8) COMPLETED : 커스텀 전달 요청(CUSTOM)과 참고 사진을 함께 남긴 완료 건
    (8, 2, '서울특별시 강남구 테헤란로 152', '서울특별시 강동구 천호대로 1077',
     4.0, 8.445218, 'COMPLETED', 10430,
     37.5006, 127.0364, 37.4783, 127.1279, 'MEDIUM',
     '/uploads/seed/delivery8-proof.png', NOW() - INTERVAL 5 DAY + INTERVAL 55 MINUTE,
     NOW() - INTERVAL 5 DAY + INTERVAL 20 MINUTE, NOW() - INTERVAL 5 DAY,
     'seed-delivery-pay-8', 3,
     'CUSTOM', NULL, '상가 2층 카페 카운터', '카페 카운터 직원에게 전달 부탁드립니다.',
     '/uploads/seed/delivery8-reference.png', NULL, NULL, NULL,
     0, NULL, '/uploads/seed/delivery8-pickup.png',
     NULL, NULL, NULL, NULL, NULL, NULL);

-- ============================================================================
-- 4. matching : 배송 요청과 장비의 매칭 5건 (요청 1·6·7번은 미매칭)
-- ============================================================================
INSERT INTO matching (id, delivery_request_id, vehicle_id, status, matched_at) VALUES
    (1, 2, 1, 'MATCHED',   NOW() - INTERVAL 110 MINUTE),
    (2, 3, 3, 'MATCHED',   NOW() - INTERVAL 170 MINUTE),
    (3, 4, 3, 'COMPLETED', NOW() - INTERVAL 4 DAY + INTERVAL 10 MINUTE),
    (4, 5, 1, 'CANCELLED', NOW() - INTERVAL 2 DAY + INTERVAL 5 MINUTE),
    (5, 8, 1, 'COMPLETED', NOW() - INTERVAL 5 DAY + INTERVAL 8 MINUTE);

-- ============================================================================
-- 5. point_wallet : 회원별 포인트 지갑 (잔액은 아래 6번 원장의 최종 잔액과 일치)
-- ============================================================================
INSERT INTO point_wallet (id, member_id, balance, version) VALUES
    (1, 1,      0, 0),  -- 관리자
    (2, 2, 466709, 4),  -- 김화주   : 50만 충전 후 배송 4건(33,291P) 결제
    (3, 3,  94591, 3),  -- 최고객   : 10만 충전 후 결제 2건, 취소 1건 환불
    (4, 4,  50000, 2),  -- 정단골   : 5만 충전 후 자동 취소로 전액 환불받아 원복
    (5, 5,   1000, 1),  -- 박배송   : 고객 취소 이동 보상 1,000P 수령
    (6, 6,      0, 0),  -- 이배송
    (7, 7,      0, 0);  -- 신입배송

-- ============================================================================
-- 6. point_history : 포인트 원장 13건
--    amount는 항상 양수이며 입출금 방향은 type(CHARGE/USE/REFUND/COURIER_COMPENSATION)으로 구분합니다.
--    환불·보상 이력의 idempotency_key는 서비스 코드의 실제 키 규칙을 따릅니다.
-- ============================================================================
INSERT INTO point_history
    (id, member_id, wallet_id, amount, balance_after, type, payment_method,
     idempotency_key, created_at, reference_id, description)
VALUES
    -- 김화주(2) : 충전 1건 + 배송 결제 4건
    (1, 2, 2, 500000, 500000, 'CHARGE', 'CARD',
     'seed-charge-client-1', NOW() - INTERVAL 7 DAY, NULL, '카드 포인트 충전'),
    (2, 2, 2,  10430, 489570, 'USE', NULL,
     'seed-delivery-pay-8', NOW() - INTERVAL 5 DAY, NULL, '배송 요청 결제'),
    (3, 2, 2,   5317, 484253, 'USE', NULL,
     'seed-delivery-pay-3', NOW() - INTERVAL 3 HOUR, NULL, '배송 요청 결제'),
    (4, 2, 2,  12136, 472117, 'USE', NULL,
     'seed-delivery-pay-2', NOW() - INTERVAL 2 HOUR, NULL, '배송 요청 결제'),
    (5, 2, 2,   5408, 466709, 'USE', NULL,
     'seed-delivery-pay-1', NOW() - INTERVAL 10 MINUTE, NULL, '배송 요청 결제'),

    -- 최고객(3) : 충전 1건 + 결제 2건 + 고객 취소 환불 1건(수수료 1,000P 차감)
    (6, 3, 3, 100000, 100000, 'CHARGE', 'EASY_PAY',
     'seed-charge-customer2-1', NOW() - INTERVAL 6 DAY, NULL, '간편결제 포인트 충전'),
    (7, 3, 3,   4409,  95591, 'USE', NULL,
     'seed-delivery-pay-4', NOW() - INTERVAL 4 DAY, NULL, '배송 요청 결제'),
    (8, 3, 3,   8221,  87370, 'USE', NULL,
     'seed-delivery-pay-5', NOW() - INTERVAL 2 DAY, NULL, '배송 요청 결제'),
    (9, 3, 3,   7221,  94591, 'REFUND', NULL,
     'delivery-cancel-refund:5', NOW() - INTERVAL 2 DAY + INTERVAL 15 MINUTE, 5, '고객 요청 배송 취소 환불'),

    -- 정단골(4) : 충전 1건 + 결제 1건 + 자동 타임아웃 전액 환불 1건
    (10, 4, 4,  50000, 50000, 'CHARGE', 'BANK_TRANSFER',
     'seed-charge-customer3-1', NOW() - INTERVAL 5 DAY, NULL, '계좌이체 포인트 충전'),
    (11, 4, 4,  13872, 36128, 'USE', NULL,
     'seed-delivery-pay-6', NOW() - INTERVAL 3 DAY, NULL, '배송 요청 결제'),
    (12, 4, 4,  13872, 50000, 'REFUND', NULL,
     'delivery-timeout-refund:6', NOW() - INTERVAL 3 DAY + INTERVAL 31 MINUTE, 6, '30분 미배정 자동 취소 환불'),

    -- 박배송(5) : 고객 취소로 발생한 이동 보상 수령
    (13, 5, 5,   1000,  1000, 'COURIER_COMPENSATION', NULL,
     'delivery-cancel-compensation:5', NOW() - INTERVAL 2 DAY + INTERVAL 15 MINUTE, 5, '고객 취소 배송원 이동 보상');

-- ============================================================================
-- 7. notification : 알림 8건 (읽음/안읽음 혼합 - 홈 배지, 카테고리 필터 테스트용)
-- ============================================================================
INSERT INTO notification (id, member_id, title, message, category, is_read, created_at) VALUES
    (1, 2, '배송원이 매칭됐어요', '박배송님이 배송을 수락했어요. 실시간 위치를 확인해보세요.', 'MATCHING', FALSE, NOW() - INTERVAL 110 MINUTE),
    (2, 2, '배송원이 픽업을 완료했어요', '이배송님이 물품을 픽업해 도착지로 이동 중이에요.', 'DELIVERY', FALSE, NOW() - INTERVAL 150 MINUTE),
    (3, 2, '배송이 완료됐어요', '천호대로 1077로 보낸 물품이 안전하게 전달됐어요.', 'DELIVERY', TRUE, NOW() - INTERVAL 5 DAY + INTERVAL 55 MINUTE),
    (4, 2, '포인트가 충전됐어요', '500,000P가 충전됐어요.', 'POINT', TRUE, NOW() - INTERVAL 7 DAY),
    (5, 3, '배송이 완료됐어요', '봉은사로 524로 보낸 물품이 안전하게 전달됐어요.', 'DELIVERY', TRUE, NOW() - INTERVAL 4 DAY + INTERVAL 52 MINUTE),
    (6, 3, '취소 수수료가 정산됐어요', '취소 수수료 1,000P를 제외한 7,221P가 환불됐어요.', 'POINT', FALSE, NOW() - INTERVAL 2 DAY + INTERVAL 15 MINUTE),
    (7, 4, '배송이 자동 취소됐어요', '30분 동안 배송원이 배정되지 않아 13,872P를 전액 환불했어요.', 'DELIVERY', FALSE, NOW() - INTERVAL 3 DAY + INTERVAL 31 MINUTE),
    (8, 5, '이동 보상이 지급됐어요', '고객 취소로 발생한 이동 보상 1,000P가 지급됐어요.', 'POINT', FALSE, NOW() - INTERVAL 2 DAY + INTERVAL 15 MINUTE);

-- ============================================================================
-- 8. address : 자주 쓰는 주소 5건 (배송 요청 화면의 주소 퀵칩 테스트용)
-- ============================================================================
INSERT INTO address (id, member_id, alias, address, detail_address, pickup_guide) VALUES
    (1, 2, '집',   '서울특별시 강남구 테헤란로 152', '101동 1503호', '공동현관 비밀번호 1004#'),
    (2, 2, '회사', '서울특별시 성동구 아차산로 17',  '3층 물류팀',    '1층 안내데스크에 문의해주세요.'),
    (3, 3, '집',   '서울특별시 강남구 강남대로 382', 'B동 1504호',    '부재 시 경비실에 맡겨주세요.'),
    (4, 3, '가게', '서울특별시 강남구 봉은사로 524', '1층 상가',      '영업시간 10:00~20:00'),
    (5, 4, '집',   '서울특별시 송파구 올림픽로 300', '5동 702호',     '엘리베이터 이용 가능');

-- ============================================================================
-- 9. notice : 공지사항 추가 2건 (id 1~3은 기준 데이터라 건드리지 않음)
-- ============================================================================
INSERT INTO notice (id, title, content, category, is_pinned, created_at, updated_at) VALUES
    (4, '[안내] 배송 자동 취소 및 환불 정책 변경 안내',
     '배송원이 30분 동안 배정되지 않은 요청은 자동으로 취소되며, 결제하신 포인트는 전액 환불됩니다.\n매칭 이후 고객님이 직접 취소하실 경우에는 취소 수수료 1,000P가 부과되고 나머지 금액이 환불됩니다.',
     'SYSTEM', FALSE, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
    (5, '[이벤트] 배송원 신규 등록 장비 심사 기간 단축',
     '배송원 등록 시 제출하는 장비 심사 처리 기간이 기존 3영업일에서 1영업일로 단축되었습니다.',
     'EVENT', FALSE, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

COMMIT;

-- ============================================================================
-- 적재 결과 확인용 쿼리 (선택)
-- ----------------------------------------------------------------------------
-- SELECT id, email, name, role, courier_approval_status FROM member ORDER BY id;
-- SELECT id, member_id, status, fee_point, cancellation_reason FROM delivery_request ORDER BY id;
-- SELECT w.member_id, m.name, w.balance FROM point_wallet w JOIN member m ON m.id = w.member_id ORDER BY w.member_id;
-- ============================================================================
