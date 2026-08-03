-- H2 테스트 DB와 MariaDB에서 동일한 마이그레이션을 사용하기 위해 벤더 전용 Online DDL 옵션을 생략한다.
-- linter:ignore-online-ddl
ALTER TABLE point_wallet ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    payment_method VARCHAR(30),
    idempotency_key VARCHAR(36),
    balance_after BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_point_history_wallet FOREIGN KEY (wallet_id) REFERENCES point_wallet (id),
    CONSTRAINT uq_point_history_wallet_idempotency UNIQUE (wallet_id, idempotency_key),
    INDEX idx_point_history_wallet_type_created (wallet_id, type, created_at)
);
