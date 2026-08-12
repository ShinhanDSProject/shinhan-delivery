-- linter:ignore-online-ddl
ALTER TABLE vehicle ADD COLUMN name VARCHAR(100) NULL AFTER member_id;
-- linter:ignore-online-ddl
ALTER TABLE vehicle ADD COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' AFTER status;
-- linter:ignore-online-ddl
ALTER TABLE vehicle ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT FALSE AFTER approval_status;
-- linter:ignore-online-ddl
ALTER TABLE vehicle ADD COLUMN license_plate_number VARCHAR(50) NULL AFTER is_active;
-- linter:ignore-online-ddl
ALTER TABLE vehicle ADD COLUMN insurance_photo_url LONGTEXT NULL AFTER license_plate_number;
-- linter:ignore-online-ddl
ALTER TABLE vehicle ADD COLUMN photo_url LONGTEXT NULL AFTER insurance_photo_url;
-- linter:ignore-online-ddl
ALTER TABLE vehicle ADD COLUMN displacement INT NULL AFTER photo_url;
