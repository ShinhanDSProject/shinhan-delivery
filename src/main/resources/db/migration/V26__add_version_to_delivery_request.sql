-- linter:ignore-online-ddl
ALTER TABLE delivery_request ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
