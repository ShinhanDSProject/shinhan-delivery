-- linter:ignore-online-ddl (소규모 데이터셋, 컬럼 추가는 온라인 DDL 락 위험이 낮음)
ALTER TABLE delivery_request ADD COLUMN picked_up_at TIMESTAMP NULL;
