CREATE TABLE vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    max_weight DOUBLE NOT NULL,
    max_distance DOUBLE NOT NULL
);
