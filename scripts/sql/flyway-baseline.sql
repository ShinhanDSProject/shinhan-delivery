-- ============================================================================
-- 이미 schema-init.sql 로 만들어 둔 DB를 Flyway가 인식하게 만드는 일회성 스크립트
-- ----------------------------------------------------------------------------
-- 이 DB가 "V32까지 정상 적용된 Flyway 관리 DB"라고 표시합니다.
-- 실행 후에는 별도 설정 없이 서버가 기동되고, 이후 V33 이상만 이어서 적용됩니다.
-- 이미 이력 테이블이 있는 DB에 실행하면 아무 일도 하지 않습니다(안전).
-- ============================================================================

CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT           NOT NULL,
    version        VARCHAR(50)   NULL,
    description    VARCHAR(200)  NOT NULL,
    type           VARCHAR(20)   NOT NULL,
    script         VARCHAR(1000) NOT NULL,
    checksum       INT           NULL,
    installed_by   VARCHAR(100)  NOT NULL,
    installed_on   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT           NOT NULL,
    success        BOOLEAN       NOT NULL,
    PRIMARY KEY (installed_rank),
    KEY flyway_schema_history_s_idx (success)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 이력이 비어 있을 때만 베이스라인 행을 넣는다 (재실행해도 중복되지 않음)
INSERT INTO flyway_schema_history
    (installed_rank, version, description, type, script, checksum,
     installed_by, installed_on, execution_time, success)
SELECT 1, '32', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL,
       SUBSTRING_INDEX(CURRENT_USER(), '@', 1), NOW(), 0, TRUE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM flyway_schema_history);
