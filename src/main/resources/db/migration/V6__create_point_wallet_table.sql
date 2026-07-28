CREATE TABLE point_wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_point_wallet_member UNIQUE (member_id),
    CONSTRAINT fk_point_wallet_member FOREIGN KEY (member_id) REFERENCES member (id)
);
