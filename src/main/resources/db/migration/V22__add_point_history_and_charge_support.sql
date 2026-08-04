CREATE TABLE point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    payment_method VARCHAR(30) NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_point_history_member_key UNIQUE (member_id, idempotency_key),
    CONSTRAINT fk_point_history_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_point_history_wallet FOREIGN KEY (wallet_id) REFERENCES point_wallet (id)
);

-- linter:ignore-online-ddl
ALTER TABLE point_wallet ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
