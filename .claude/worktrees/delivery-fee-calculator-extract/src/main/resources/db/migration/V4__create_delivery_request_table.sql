CREATE TABLE delivery_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    dropoff_address VARCHAR(255) NOT NULL,
    weight DOUBLE NOT NULL,
    distance DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    fee_point BIGINT NOT NULL,
    CONSTRAINT fk_delivery_request_customer FOREIGN KEY (customer_id) REFERENCES member (id)
);
