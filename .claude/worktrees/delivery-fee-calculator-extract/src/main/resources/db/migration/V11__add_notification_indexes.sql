-- linter:ignore-online-ddl (신규 테이블, 데이터 없어 락 위험 없음)
CREATE INDEX idx_notification_member_created ON notification (member_id, created_at);
-- linter:ignore-online-ddl
CREATE INDEX idx_notification_member_category_created ON notification (member_id, category, created_at);
