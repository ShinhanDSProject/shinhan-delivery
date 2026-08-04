-- linter:ignore-online-ddl (소규모 데이터셋, 인덱스 추가는 락 위험이 낮음)
CREATE INDEX IF NOT EXISTS idx_delivery_request_customer_created_id ON delivery_request (customer_id, created_at, id);
-- linter:ignore-online-ddl (소규모 데이터셋, 인덱스 추가는 락 위험이 낮음)
CREATE INDEX IF NOT EXISTS idx_delivery_request_customer_status_created_id ON delivery_request (customer_id, status, created_at, id);
-- linter:ignore-online-ddl (소규모 데이터셋, 인덱스 교체는 락 위험이 낮음. customer_id FK를 지지하는 인덱스가
-- 위에서 먼저 새로 만들어졌으므로 이 순서에서 삭제해도 FK 제약 위반이 없다)
DROP INDEX IF EXISTS idx_delivery_request_customer_created ON delivery_request;
-- linter:ignore-online-ddl (소규모 데이터셋, 인덱스 교체는 락 위험이 낮음)
DROP INDEX IF EXISTS idx_delivery_request_customer_status_created ON delivery_request;
