-- linter:ignore-online-ddl (nullable 물품 확인 사진 컬럼 추가, 기존 행은 null 유지)
ALTER TABLE delivery_request ADD COLUMN pickup_photo_url VARCHAR(255) NULL;
