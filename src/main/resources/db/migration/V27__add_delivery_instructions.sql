-- linter:ignore-online-ddl (기존 행은 NONE 기본값으로 호환되며 컬럼을 순차 추가한다)
ALTER TABLE delivery_request
    ADD COLUMN delivery_instruction_type VARCHAR(40) NOT NULL DEFAULT 'NONE';

-- linter:ignore-online-ddl (nullable 민감정보 컬럼 추가)
ALTER TABLE delivery_request
    ADD COLUMN entrance_code VARCHAR(100) NULL;

-- linter:ignore-online-ddl (nullable 현장 위치 컬럼 추가)
ALTER TABLE delivery_request
    ADD COLUMN unit_detail VARCHAR(100) NULL;

-- linter:ignore-online-ddl (nullable 전달 요청 컬럼 추가)
ALTER TABLE delivery_request
    ADD COLUMN delivery_note VARCHAR(500) NULL;

-- linter:ignore-online-ddl (nullable 참고 사진 URL 컬럼 추가)
ALTER TABLE delivery_request
    ADD COLUMN delivery_reference_photo_url VARCHAR(255) NULL;
