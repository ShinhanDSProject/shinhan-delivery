CREATE TABLE matching (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_request_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    matched_at DATETIME NOT NULL,
    CONSTRAINT uq_matching_delivery_request UNIQUE (delivery_request_id),
    CONSTRAINT fk_matching_delivery_request FOREIGN KEY (delivery_request_id) REFERENCES delivery_request (id),
    CONSTRAINT fk_matching_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id)
);
