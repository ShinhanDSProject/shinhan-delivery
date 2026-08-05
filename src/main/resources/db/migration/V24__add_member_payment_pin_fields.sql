-- linter:ignore-online-ddl (소규모 데이터셋, member PIN 컬럼 추가는 락 위험이 낮음)
ALTER TABLE member
    ADD COLUMN pin_hash VARCHAR(255) NULL;

-- linter:ignore-online-ddl (소규모 데이터셋, member PIN 컬럼 추가는 락 위험이 낮음)
ALTER TABLE member
    ADD COLUMN pin_fail_count INT NOT NULL DEFAULT 0;

-- linter:ignore-online-ddl (소규모 데이터셋, member PIN 컬럼 추가는 락 위험이 낮음)
ALTER TABLE member
    ADD COLUMN pin_locked BOOLEAN NOT NULL DEFAULT FALSE;
