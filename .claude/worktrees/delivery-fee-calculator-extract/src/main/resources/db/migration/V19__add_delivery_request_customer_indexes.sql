-- linter:ignore-online-ddl (소규모 데이터셋, 인덱스 추가는 락 위험이 낮음)
CREATE INDEX idx_delivery_request_customer_created ON delivery_request (customer_id, created_at);
-- linter:ignore-online-ddl (소규모 데이터셋, 인덱스 추가는 락 위험이 낮음)
CREATE INDEX idx_delivery_request_customer_status_created ON delivery_request (customer_id, status, created_at);
