-- linter:ignore-online-ddl
ALTER TABLE vehicle RENAME COLUMN owner_id TO member_id;

-- linter:ignore-online-ddl
ALTER TABLE delivery_request RENAME COLUMN customer_id TO member_id;
