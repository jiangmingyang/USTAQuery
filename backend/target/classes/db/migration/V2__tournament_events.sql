-- ============================================================
-- V2: Add tournament fields + tournament_events table
-- ============================================================

-- ── Expand tournaments table ──────────────────────────────────
ALTER TABLE tournaments
    ADD COLUMN code         VARCHAR(20)      AFTER tournament_id,
    ADD COLUMN section      VARCHAR(100)     AFTER state,
    ADD COLUMN organization VARCHAR(255)     AFTER section,
    ADD COLUMN postcode     VARCHAR(10)      AFTER organization,
    ADD COLUMN latitude     DECIMAL(10,7)    AFTER postcode,
    ADD COLUMN longitude    DECIMAL(10,7)    AFTER latitude,
    ADD COLUMN timezone     VARCHAR(50)      AFTER longitude,
    ADD COLUMN status       VARCHAR(20)      DEFAULT 'active' AFTER timezone,
    ADD COLUMN events_count SMALLINT UNSIGNED AFTER status,
    MODIFY COLUMN level     VARCHAR(50);

ALTER TABLE tournaments
    ADD UNIQUE INDEX idx_tournaments_code (code),
    ADD INDEX idx_tournaments_section (section);

-- ── Create tournament_events table ────────────────────────────
CREATE TABLE tournament_events (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id        VARCHAR(50)      NOT NULL,
    tournament_id   BIGINT           NOT NULL,
    gender          VARCHAR(20),
    event_type      VARCHAR(20),
    age_category    VARCHAR(20),
    min_age         TINYINT UNSIGNED,
    max_age         TINYINT UNSIGNED,
    surface         VARCHAR(30),
    court_location  VARCHAR(20),
    entry_fee       DECIMAL(8,2),
    currency        VARCHAR(3)       DEFAULT 'USD',
    level           VARCHAR(50),
    ball_color      VARCHAR(20),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX idx_events_eid (event_id),
    INDEX idx_events_tournament (tournament_id),
    INDEX idx_events_gender (gender),
    INDEX idx_events_type (event_type),
    INDEX idx_events_age (age_category),
    CONSTRAINT fk_events_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
