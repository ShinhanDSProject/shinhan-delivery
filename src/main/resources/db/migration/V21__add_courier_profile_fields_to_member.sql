-- linter:ignore-online-ddl
ALTER TABLE member ADD COLUMN activity_region VARCHAR(100) NULL;
-- linter:ignore-online-ddl
ALTER TABLE member ADD COLUMN preferred_weight DOUBLE NULL;
