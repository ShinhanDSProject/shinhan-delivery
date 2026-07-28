-- linter:ignore-online-ddl (이미 적용된 마이그레이션 - 컨벤션상 재수정 금지, CI DDL lock-risk 경고만 우회)
ALTER TABLE vehicle
    ADD COLUMN latitude DOUBLE NOT NULL DEFAULT 0,
    ADD COLUMN longitude DOUBLE NOT NULL DEFAULT 0,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

-- linter:ignore-online-ddl (이미 적용된 마이그레이션 - 컨벤션상 재수정 금지, CI DDL lock-risk 경고만 우회)
ALTER TABLE delivery_request
    ADD COLUMN pickup_latitude DOUBLE NOT NULL DEFAULT 0,
    ADD COLUMN pickup_longitude DOUBLE NOT NULL DEFAULT 0;
