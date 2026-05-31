-- ============================================================
-- V5: Add registration_status column to tournaments
-- (column may already exist if added manually; skip if so)
-- ============================================================

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tournaments'
      AND COLUMN_NAME  = 'registration_status'
);

SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE tournaments ADD COLUMN registration_status VARCHAR(30) NULL COMMENT ''Registrations open | Registrations closed | Completed'' AFTER detail_scrape_status',
    'SELECT ''column already exists, skipping'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
