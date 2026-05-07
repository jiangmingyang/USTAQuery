-- ============================================================
-- USTA Tennis Query App — Full Schema
-- ============================================================

-- ── 1. players ──────────────────────────────────────────────
CREATE TABLE players (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    uaid            VARCHAR(20)    NOT NULL,
    first_name      VARCHAR(100)   NOT NULL,
    last_name       VARCHAR(100)   NOT NULL,
    gender          VARCHAR(1)     NOT NULL COMMENT 'M or F',
    city            VARCHAR(100),
    state           VARCHAR(2),
    section         VARCHAR(50),
    section_code    VARCHAR(10),
    district        VARCHAR(50),
    district_code   VARCHAR(10),
    nationality     VARCHAR(10)    DEFAULT 'USA',
    itf_tennis_id   VARCHAR(30),
    age_category    VARCHAR(20)    COMMENT 'e.g. 11_TO_18',
    wheelchair      BOOLEAN        NOT NULL DEFAULT FALSE,

    -- WTN Ratings
    wtn_singles                 DECIMAL(5,2),
    wtn_singles_confidence      INT            COMMENT '0-100',
    wtn_singles_last_played     DATE,
    wtn_singles_game_zone_upper DECIMAL(5,2),
    wtn_singles_game_zone_lower DECIMAL(5,2),
    wtn_doubles                 DECIMAL(5,2),
    wtn_doubles_confidence      INT            COMMENT '0-100',
    wtn_doubles_last_played     DATE,
    wtn_doubles_game_zone_upper DECIMAL(5,2),
    wtn_doubles_game_zone_lower DECIMAL(5,2),

    -- UTR Ratings (external source, kept for future)
    utr_id          VARCHAR(20),
    utr_singles     DECIMAL(4,2),
    utr_doubles     DECIMAL(4,2),
    rating_ntrp     VARCHAR(4),

    profile_image_url VARCHAR(500),
    membership_type   VARCHAR(30),
    membership_expiry DATE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX idx_players_uaid (uaid),
    INDEX idx_players_name (last_name, first_name),
    INDEX idx_players_section_district (section, district),
    INDEX idx_players_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 2. rankings ─────────────────────────────────────────────
CREATE TABLE rankings (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id                BIGINT       NOT NULL,
    catalog_id               VARCHAR(120) NOT NULL COMMENT 'USTA composite key',
    display_label            VARCHAR(200),
    player_type              VARCHAR(20)  COMMENT 'e.g. JUNIOR',
    age_restriction          VARCHAR(10)  NOT NULL COMMENT 'Y12, Y14, Y16, Y18',
    age_restriction_modifier VARCHAR(10)  COMMENT 'UNDER',
    rank_list_gender         VARCHAR(1)   NOT NULL COMMENT 'M or F',
    list_type                VARCHAR(30)  NOT NULL COMMENT 'QUOTA, STANDING, SEEDING, BONUS_POINTS, YEAR_END',
    match_format             VARCHAR(20)  COMMENT 'null, SINGLES, DOUBLES',
    match_format_type        VARCHAR(20)  COMMENT 'null, INDIVIDUAL',
    family_category          VARCHAR(20)  COMMENT 'e.g. S70',

    national_rank   INT UNSIGNED,
    section_rank    INT UNSIGNED,
    district_rank   INT UNSIGNED,

    points          INT,
    singles_points  INT,
    doubles_points  INT,
    bonus_points    INT,

    wins            INT UNSIGNED,
    losses          INT UNSIGNED,
    trend_direction VARCHAR(20) COMMENT 'up, down, no change',

    publish_date    DATETIME,
    section         VARCHAR(50),
    district        VARCHAR(50),
    state           VARCHAR(2),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX idx_rankings_natural (player_id, catalog_id, publish_date),
    INDEX idx_rankings_player (player_id),
    INDEX idx_rankings_catalog (catalog_id),
    INDEX idx_rankings_age_list (age_restriction, list_type),
    INDEX idx_rankings_publish (publish_date),
    CONSTRAINT fk_rankings_player FOREIGN KEY (player_id) REFERENCES players (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 3. tournaments ──────────────────────────────────────────
CREATE TABLE tournaments (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id     VARCHAR(50)  NOT NULL,
    name              VARCHAR(255) NOT NULL,
    level             VARCHAR(10),
    category          VARCHAR(20),
    start_date        DATE,
    end_date          DATE,
    entry_deadline    DATE,
    accepting_entries BOOLEAN      NOT NULL DEFAULT FALSE,
    venue_name        VARCHAR(255),
    city              VARCHAR(100),
    state             VARCHAR(2),
    surface           VARCHAR(30),
    url               VARCHAR(500),
    director_name     VARCHAR(200),
    director_email    VARCHAR(255),
    director_phone    VARCHAR(30),
    total_draws       SMALLINT UNSIGNED,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX idx_tournaments_tid (tournament_id),
    INDEX idx_tournaments_dates (start_date, end_date),
    INDEX idx_tournaments_level (level),
    INDEX idx_tournaments_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 4. registrations ────────────────────────────────────────
CREATE TABLE registrations (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id     BIGINT       NOT NULL,
    player1_id        BIGINT       NOT NULL,
    player2_id        BIGINT,
    match_type        VARCHAR(10)  NOT NULL COMMENT 'SINGLES or DOUBLES',
    division_name     VARCHAR(100) NOT NULL,
    seed              SMALLINT UNSIGNED,
    registration_date TIMESTAMP,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ENTERED',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX idx_reg_no_dup (tournament_id, division_name, player1_id),
    INDEX idx_reg_player1 (player1_id),
    INDEX idx_reg_player2 (player2_id),
    CONSTRAINT fk_reg_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id),
    CONSTRAINT fk_reg_player1   FOREIGN KEY (player1_id)    REFERENCES players (id),
    CONSTRAINT fk_reg_player2   FOREIGN KEY (player2_id)    REFERENCES players (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 5. matches ──────────────────────────────────────────────
CREATE TABLE matches (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id    BIGINT       NOT NULL,
    division_name    VARCHAR(100) NOT NULL,
    round            VARCHAR(30)  NOT NULL,
    match_type       VARCHAR(10)  NOT NULL COMMENT 'SINGLES or DOUBLES',
    player1_id       BIGINT       NOT NULL,
    player2_id       BIGINT,
    opponent1_id     BIGINT,
    opponent2_id     BIGINT,
    opponent1_name   VARCHAR(200),
    opponent2_name   VARCHAR(200),
    winner_side      VARCHAR(10)  COMMENT 'PLAYER or OPPONENT',
    win_type         VARCHAR(30),
    match_date       DATE,
    score_summary    VARCHAR(100),
    duration_minutes SMALLINT UNSIGNED,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_matches_tournament (tournament_id),
    INDEX idx_matches_player1 (player1_id),
    INDEX idx_matches_date (match_date),
    CONSTRAINT fk_matches_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments (id),
    CONSTRAINT fk_matches_player1    FOREIGN KEY (player1_id)    REFERENCES players (id),
    CONSTRAINT fk_matches_player2    FOREIGN KEY (player2_id)    REFERENCES players (id),
    CONSTRAINT fk_matches_opponent1  FOREIGN KEY (opponent1_id)  REFERENCES players (id),
    CONSTRAINT fk_matches_opponent2  FOREIGN KEY (opponent2_id)  REFERENCES players (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 6. match_sets ───────────────────────────────────────────
CREATE TABLE match_sets (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    match_id          BIGINT         NOT NULL,
    set_number        TINYINT UNSIGNED NOT NULL,
    player_games      TINYINT UNSIGNED NOT NULL,
    opponent_games    TINYINT UNSIGNED NOT NULL,
    tiebreak_player   TINYINT UNSIGNED,
    tiebreak_opponent TINYINT UNSIGNED,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE INDEX idx_match_sets_natural (match_id, set_number),
    CONSTRAINT fk_match_sets_match FOREIGN KEY (match_id) REFERENCES matches (id) ON DELETE CASCADE,
    CONSTRAINT chk_set_number CHECK (set_number BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 7. scrape_jobs ──────────────────────────────────────────
CREATE TABLE scrape_jobs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_type          VARCHAR(50)  NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    target_url        VARCHAR(1000),
    parameters        JSON,
    records_processed INT UNSIGNED NOT NULL DEFAULT 0,
    records_created   INT UNSIGNED NOT NULL DEFAULT 0,
    records_updated   INT UNSIGNED NOT NULL DEFAULT 0,
    records_failed    INT UNSIGNED NOT NULL DEFAULT 0,
    started_at        TIMESTAMP,
    completed_at      TIMESTAMP,
    error_message     TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_scrape_jobs_status (status),
    INDEX idx_scrape_jobs_type (job_type),
    INDEX idx_scrape_jobs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── 8. scrape_errors ────────────────────────────────────────
CREATE TABLE scrape_errors (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    scrape_job_id    BIGINT       NOT NULL,
    url              VARCHAR(1000),
    error_type       VARCHAR(100) NOT NULL,
    error_message    TEXT         NOT NULL,
    http_status_code SMALLINT,
    occurred_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_scrape_errors_job (scrape_job_id),
    CONSTRAINT fk_scrape_errors_job FOREIGN KEY (scrape_job_id) REFERENCES scrape_jobs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
