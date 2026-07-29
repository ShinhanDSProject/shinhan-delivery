CREATE TABLE address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    alias VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    detail_address VARCHAR(255),
    pickup_guide VARCHAR(255)
);
