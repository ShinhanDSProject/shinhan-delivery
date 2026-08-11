-- linter:ignore-online-ddl
ALTER TABLE member ADD COLUMN courier_approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';
-- linter:ignore-online-ddl
ALTER TABLE member ADD COLUMN proof_document_url VARCHAR(255) NULL;
