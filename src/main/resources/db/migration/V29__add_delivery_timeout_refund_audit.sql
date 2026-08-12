-- linter:ignore-online-ddl (nullable 감사 컬럼을 순차 추가해 기존 행과 호환한다)
ALTER TABLE delivery_request ADD COLUMN cancellation_reason VARCHAR(30) NULL;

-- linter:ignore-online-ddl (nullable 취소 시각 컬럼 추가)
ALTER TABLE delivery_request ADD COLUMN cancelled_at TIMESTAMP NULL;

-- linter:ignore-online-ddl (결제되지 않은 요청은 null을 유지한다)
ALTER TABLE delivery_request ADD COLUMN refunded_at TIMESTAMP NULL;

-- linter:ignore-online-ddl (기존 포인트 이력은 참조 배송이 없을 수 있다)
ALTER TABLE point_history ADD COLUMN reference_id BIGINT NULL;

-- linter:ignore-online-ddl (기존 포인트 이력은 설명이 없을 수 있다)
ALTER TABLE point_history ADD COLUMN description VARCHAR(100) NULL;

-- linter:ignore-online-ddl (타임아웃 후보 상태·시각 조회 인덱스)
CREATE INDEX idx_delivery_request_timeout
    ON delivery_request (status, created_at, id);

-- linter:ignore-online-ddl (환불 원 배송 ID별 중복 환불 방지, 기존 NULL 행은 상호 충돌하지 않는다)
CREATE UNIQUE INDEX uq_point_history_refund_reference
    ON point_history (type, reference_id);
