-- linter:ignore-online-ddl (소규모 데이터셋, 컬럼 추가는 온라인 DDL 락 위험이 낮음)
ALTER TABLE delivery_request ADD COLUMN dropoff_latitude DOUBLE NOT NULL DEFAULT 0;
-- linter:ignore-online-ddl
ALTER TABLE delivery_request ADD COLUMN dropoff_longitude DOUBLE NOT NULL DEFAULT 0;
-- linter:ignore-online-ddl
ALTER TABLE delivery_request ADD COLUMN item_size VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';
