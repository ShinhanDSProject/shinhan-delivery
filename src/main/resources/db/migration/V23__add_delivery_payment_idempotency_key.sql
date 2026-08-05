ALTER TABLE delivery_request ADD COLUMN payment_idempotency_key VARCHAR(100) NULL, ALGORITHM=INPLACE, LOCK=NONE;

CREATE UNIQUE INDEX uq_delivery_request_customer_payment_key
    ON delivery_request (customer_id, payment_idempotency_key), ALGORITHM=INPLACE, LOCK=NONE;
