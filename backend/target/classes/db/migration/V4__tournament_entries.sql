-- ============================================================
-- V4: Add detail scrape tracking + tournament_entries table
-- ============================================================

-- Track when tournament detail (participants) was last scraped
ALTER TABLE tournaments
    ADD COLUMN detail_scraped_at    TIMESTAMP    NULL     AFTER total_draws,
    ADD COLUMN detail_scrape_status VARCHAR(20)  NULL     AFTER detail_scraped_at;

-- Player entries per event within a tournament (from GraphQL API)
CREATE TABLE tournament_entries (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id    BIGINT       NOT NULL,
    event_id         VARCHAR(50)  NOT NULL,
    participant_id   VARCHAR(50)  NOT NULL,
    player_uaid      VARCHAR(20),
    player_name      VARCHAR(200),
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    gender           VARCHAR(10),
    city             VARCHAR(100),
    state            VARCHAR(50),
    event_type       VARCHAR(20)  COMMENT 'SINGLES, DOUBLES',
    entry_stage      VARCHAR(30)  COMMENT 'MAIN, QUALIFYING',
    entry_status     VARCHAR(30)  COMMENT 'DIRECT_ACCEPTANCE, ALTERNATE, WITHDRAWN',
    entry_position   INT,
    status_detail    VARCHAR(100),
    draw_id          VARCHAR(50),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX idx_entry_unique (tournament_id, event_id, participant_id),
    INDEX idx_entry_tournament (tournament_id),
    INDEX idx_entry_uaid (player_uaid),
    INDEX idx_entry_event (event_id),
    INDEX idx_entry_status (entry_status),
    CONSTRAINT fk_entry_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
