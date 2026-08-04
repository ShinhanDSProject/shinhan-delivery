-- linter:ignore-online-ddl (소규모 데이터셋, 신규 컬럼 백필은 락 위험이 낮음)
UPDATE delivery_request SET created_at = NOW() WHERE created_at IS NULL;
