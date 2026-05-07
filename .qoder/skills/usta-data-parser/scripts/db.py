"""MySQL connection pool and upsert helpers for all tables."""
from __future__ import annotations

import logging
from contextlib import contextmanager

import mysql.connector
from mysql.connector import pooling

import config

logger = logging.getLogger(__name__)

_pool: pooling.MySQLConnectionPool | None = None


def get_pool() -> pooling.MySQLConnectionPool:
    global _pool
    if _pool is None:
        _pool = pooling.MySQLConnectionPool(
            pool_name="usta_scraper",
            pool_size=min(config.DB_POOL_SIZE if config.DB_POOL_SIZE > 0 else config.CONCURRENCY + 2, 32),
            host=config.DB_HOST,
            port=config.DB_PORT,
            database=config.DB_NAME,
            user=config.DB_USER,
            password=config.DB_PASSWORD,
            charset="utf8mb4",
            collation="utf8mb4_unicode_ci",
            autocommit=False,
        )
    return _pool


@contextmanager
def get_connection():
    conn = get_pool().get_connection()
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


@contextmanager
def get_cursor():
    with get_connection() as conn:
        cursor = conn.cursor(dictionary=True)
        try:
            yield cursor
        finally:
            cursor.close()


# ── Player ────────────────────────────────────────────────────────────

def upsert_player(data: dict) -> int:
    """Insert or update a player by UAID. Returns the player's internal id."""
    sql = """
        INSERT INTO players (
            uaid, first_name, last_name, gender, city, state,
            section, section_code, district, district_code,
            nationality, itf_tennis_id, age_category, wheelchair,
            wtn_singles, wtn_singles_confidence, wtn_singles_last_played,
            wtn_singles_game_zone_upper, wtn_singles_game_zone_lower,
            wtn_doubles, wtn_doubles_confidence, wtn_doubles_last_played,
            wtn_doubles_game_zone_upper, wtn_doubles_game_zone_lower,
            utr_id, utr_singles, utr_doubles,
            rating_ntrp, profile_image_url, membership_type, membership_expiry
        ) VALUES (
            %(uaid)s, %(first_name)s, %(last_name)s, %(gender)s, %(city)s, %(state)s,
            %(section)s, %(section_code)s, %(district)s, %(district_code)s,
            %(nationality)s, %(itf_tennis_id)s, %(age_category)s, %(wheelchair)s,
            %(wtn_singles)s, %(wtn_singles_confidence)s, %(wtn_singles_last_played)s,
            %(wtn_singles_game_zone_upper)s, %(wtn_singles_game_zone_lower)s,
            %(wtn_doubles)s, %(wtn_doubles_confidence)s, %(wtn_doubles_last_played)s,
            %(wtn_doubles_game_zone_upper)s, %(wtn_doubles_game_zone_lower)s,
            %(utr_id)s, %(utr_singles)s, %(utr_doubles)s,
            %(rating_ntrp)s, %(profile_image_url)s, %(membership_type)s, %(membership_expiry)s
        )
        ON DUPLICATE KEY UPDATE
            first_name = VALUES(first_name),
            last_name = VALUES(last_name),
            gender = VALUES(gender),
            city = VALUES(city),
            state = VALUES(state),
            section = VALUES(section),
            section_code = VALUES(section_code),
            district = VALUES(district),
            district_code = VALUES(district_code),
            nationality = VALUES(nationality),
            itf_tennis_id = VALUES(itf_tennis_id),
            age_category = VALUES(age_category),
            wheelchair = VALUES(wheelchair),
            wtn_singles = VALUES(wtn_singles),
            wtn_singles_confidence = VALUES(wtn_singles_confidence),
            wtn_singles_last_played = VALUES(wtn_singles_last_played),
            wtn_singles_game_zone_upper = VALUES(wtn_singles_game_zone_upper),
            wtn_singles_game_zone_lower = VALUES(wtn_singles_game_zone_lower),
            wtn_doubles = VALUES(wtn_doubles),
            wtn_doubles_confidence = VALUES(wtn_doubles_confidence),
            wtn_doubles_last_played = VALUES(wtn_doubles_last_played),
            wtn_doubles_game_zone_upper = VALUES(wtn_doubles_game_zone_upper),
            wtn_doubles_game_zone_lower = VALUES(wtn_doubles_game_zone_lower),
            utr_id = VALUES(utr_id),
            utr_singles = VALUES(utr_singles),
            utr_doubles = VALUES(utr_doubles),
            rating_ntrp = VALUES(rating_ntrp),
            profile_image_url = VALUES(profile_image_url),
            membership_type = VALUES(membership_type),
            membership_expiry = VALUES(membership_expiry)
    """
    defaults = {
        "uaid": None, "first_name": None, "last_name": None, "gender": None,
        "city": None, "state": None, "section": None, "section_code": None,
        "district": None, "district_code": None,
        "nationality": None, "itf_tennis_id": None, "age_category": None, "wheelchair": False,
        "wtn_singles": None, "wtn_singles_confidence": None, "wtn_singles_last_played": None,
        "wtn_singles_game_zone_upper": None, "wtn_singles_game_zone_lower": None,
        "wtn_doubles": None, "wtn_doubles_confidence": None, "wtn_doubles_last_played": None,
        "wtn_doubles_game_zone_upper": None, "wtn_doubles_game_zone_lower": None,
        "utr_id": None, "utr_singles": None, "utr_doubles": None,
        "rating_ntrp": None, "profile_image_url": None,
        "membership_type": None, "membership_expiry": None,
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        # Get the id (either newly inserted or existing)
        cur.execute("SELECT id FROM players WHERE uaid = %s", (params["uaid"],))
        row = cur.fetchone()
        return row["id"] if row else 0


def get_player_id_by_uaid(uaid: str) -> int | None:
    with get_cursor() as cur:
        cur.execute("SELECT id FROM players WHERE uaid = %s", (uaid,))
        row = cur.fetchone()
        return row["id"] if row else None


def upsert_player_basic(data: dict) -> int:
    """Insert or update basic player info from rankings data.

    Only touches demographic fields — does NOT overwrite WTN/UTR/membership
    data that may have been populated by a full player profile scrape.
    Returns the player's internal id.
    """
    sql = """
        INSERT INTO players (
            uaid, first_name, last_name, gender,
            city, state, section, district
        ) VALUES (
            %(uaid)s, %(first_name)s, %(last_name)s, %(gender)s,
            %(city)s, %(state)s, %(section)s, %(district)s
        )
        ON DUPLICATE KEY UPDATE
            first_name = VALUES(first_name),
            last_name = VALUES(last_name),
            gender = VALUES(gender),
            city = VALUES(city),
            state = VALUES(state),
            section = VALUES(section),
            district = VALUES(district)
    """
    defaults = {
        "uaid": None, "first_name": None, "last_name": None, "gender": None,
        "city": None, "state": None, "section": None, "district": None,
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        cur.execute("SELECT id FROM players WHERE uaid = %s", (params["uaid"],))
        row = cur.fetchone()
        return row["id"] if row else 0


# ── Tournament ────────────────────────────────────────────────────────

def upsert_tournament(data: dict) -> int:
    """Insert or update a tournament by tournament_id. Returns internal id."""
    sql = """
        INSERT INTO tournaments (
            tournament_id, code, name, level, category, start_date, end_date, entry_deadline,
            accepting_entries, venue_name, city, state, section, organization, org_slug,
            postcode, latitude, longitude, timezone, status, events_count,
            surface, url, director_name, director_email, director_phone, total_draws
        ) VALUES (
            %(tournament_id)s, %(code)s, %(name)s, %(level)s, %(category)s, %(start_date)s,
            %(end_date)s, %(entry_deadline)s, %(accepting_entries)s, %(venue_name)s,
            %(city)s, %(state)s, %(section)s, %(organization)s, %(org_slug)s,
            %(postcode)s, %(latitude)s, %(longitude)s, %(timezone)s, %(status)s, %(events_count)s,
            %(surface)s, %(url)s, %(director_name)s, %(director_email)s, %(director_phone)s,
            %(total_draws)s
        )
        ON DUPLICATE KEY UPDATE
            code = COALESCE(VALUES(code), code),
            name = VALUES(name),
            level = VALUES(level),
            category = VALUES(category),
            start_date = VALUES(start_date),
            end_date = VALUES(end_date),
            entry_deadline = VALUES(entry_deadline),
            accepting_entries = VALUES(accepting_entries),
            venue_name = VALUES(venue_name),
            city = VALUES(city),
            state = VALUES(state),
            section = COALESCE(VALUES(section), section),
            organization = COALESCE(VALUES(organization), organization),
            org_slug = COALESCE(VALUES(org_slug), org_slug),
            postcode = COALESCE(VALUES(postcode), postcode),
            latitude = COALESCE(VALUES(latitude), latitude),
            longitude = COALESCE(VALUES(longitude), longitude),
            timezone = COALESCE(VALUES(timezone), timezone),
            status = COALESCE(VALUES(status), status),
            events_count = COALESCE(VALUES(events_count), events_count),
            surface = COALESCE(VALUES(surface), surface),
            url = VALUES(url),
            director_name = COALESCE(VALUES(director_name), director_name),
            director_email = COALESCE(VALUES(director_email), director_email),
            director_phone = COALESCE(VALUES(director_phone), director_phone),
            total_draws = COALESCE(VALUES(total_draws), total_draws)
    """
    defaults = {
        "tournament_id": None, "code": None, "name": None, "level": None, "category": None,
        "start_date": None, "end_date": None, "entry_deadline": None,
        "accepting_entries": 0, "venue_name": None, "city": None, "state": None,
        "section": None, "organization": None, "org_slug": None,
        "postcode": None, "latitude": None, "longitude": None, "timezone": None,
        "status": "active", "events_count": None,
        "surface": None, "url": None, "director_name": None, "director_email": None,
        "director_phone": None, "total_draws": None,
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        cur.execute(
            "SELECT id FROM tournaments WHERE tournament_id = %s",
            (params["tournament_id"],),
        )
        row = cur.fetchone()
        return row["id"] if row else 0


def get_tournament_internal_id(tournament_id: str) -> int | None:
    with get_cursor() as cur:
        cur.execute(
            "SELECT id FROM tournaments WHERE tournament_id = %s", (tournament_id,)
        )
        row = cur.fetchone()
        return row["id"] if row else None


def get_tournament_end_date(tournament_id: str):
    """Return the end_date for a tournament, or None."""
    with get_cursor() as cur:
        cur.execute(
            "SELECT end_date FROM tournaments WHERE tournament_id = %s", (tournament_id,)
        )
        row = cur.fetchone()
        return row["end_date"] if row else None


def upsert_tournament_event(data: dict) -> int:
    """Insert or update a tournament event by event_id. Returns internal id."""
    sql = """
        INSERT INTO tournament_events (
            event_id, tournament_id, gender, event_type, age_category,
            min_age, max_age, surface, court_location,
            entry_fee, currency, level, ball_color
        ) VALUES (
            %(event_id)s, %(tournament_id)s, %(gender)s, %(event_type)s, %(age_category)s,
            %(min_age)s, %(max_age)s, %(surface)s, %(court_location)s,
            %(entry_fee)s, %(currency)s, %(level)s, %(ball_color)s
        )
        ON DUPLICATE KEY UPDATE
            tournament_id = VALUES(tournament_id),
            gender = VALUES(gender),
            event_type = VALUES(event_type),
            age_category = VALUES(age_category),
            min_age = VALUES(min_age),
            max_age = VALUES(max_age),
            surface = VALUES(surface),
            court_location = VALUES(court_location),
            entry_fee = VALUES(entry_fee),
            currency = VALUES(currency),
            level = VALUES(level),
            ball_color = VALUES(ball_color)
    """
    defaults = {
        "event_id": None, "tournament_id": None, "gender": None, "event_type": None,
        "age_category": None, "min_age": None, "max_age": None, "surface": None,
        "court_location": None, "entry_fee": None, "currency": "USD",
        "level": None, "ball_color": None,
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        return cur.lastrowid or 0


# ── Registration ──────────────────────────────────────────────────────

def upsert_registration(data: dict) -> int:
    sql = """
        INSERT INTO registrations (
            tournament_id, player1_id, player2_id, match_type,
            division_name, seed, registration_date, status
        ) VALUES (
            %(tournament_id)s, %(player1_id)s, %(player2_id)s, %(match_type)s,
            %(division_name)s, %(seed)s, %(registration_date)s, %(status)s
        )
        ON DUPLICATE KEY UPDATE
            player2_id = VALUES(player2_id),
            seed = VALUES(seed),
            registration_date = VALUES(registration_date),
            status = VALUES(status)
    """
    defaults = {
        "tournament_id": None, "player1_id": None, "player2_id": None,
        "match_type": "SINGLES", "division_name": None, "seed": None,
        "registration_date": None, "status": "ENTERED",
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        return cur.lastrowid or 0


# ── Match + Sets ──────────────────────────────────────────────────────

def upsert_match(data: dict, sets: list[dict] | None = None) -> int:
    """Insert a match row and its child set rows. Returns match id."""
    sql = """
        INSERT INTO matches (
            tournament_id, division_name, `round`, match_type,
            player1_id, player2_id, opponent1_id, opponent2_id,
            opponent1_name, opponent2_name, winner_side, win_type,
            match_date, score_summary, duration_minutes
        ) VALUES (
            %(tournament_id)s, %(division_name)s, %(round)s, %(match_type)s,
            %(player1_id)s, %(player2_id)s, %(opponent1_id)s, %(opponent2_id)s,
            %(opponent1_name)s, %(opponent2_name)s, %(winner_side)s, %(win_type)s,
            %(match_date)s, %(score_summary)s, %(duration_minutes)s
        )
    """
    defaults = {
        "tournament_id": None, "division_name": None, "round": None,
        "match_type": "SINGLES", "player1_id": None, "player2_id": None,
        "opponent1_id": None, "opponent2_id": None, "opponent1_name": None,
        "opponent2_name": None, "winner_side": None, "win_type": None,
        "match_date": None, "score_summary": None, "duration_minutes": None,
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        match_id = cur.lastrowid
        if sets and match_id:
            _insert_match_sets(cur, match_id, sets)
        return match_id or 0


def _insert_match_sets(cursor, match_id: int, sets: list[dict]):
    sql = """
        INSERT INTO match_sets (
            match_id, set_number, player_games, opponent_games,
            tiebreak_player, tiebreak_opponent
        ) VALUES (
            %(match_id)s, %(set_number)s, %(player_games)s, %(opponent_games)s,
            %(tiebreak_player)s, %(tiebreak_opponent)s
        )
        ON DUPLICATE KEY UPDATE
            player_games = VALUES(player_games),
            opponent_games = VALUES(opponent_games),
            tiebreak_player = VALUES(tiebreak_player),
            tiebreak_opponent = VALUES(tiebreak_opponent)
    """
    for s in sets:
        params = {
            "match_id": match_id,
            "set_number": s.get("set_number"),
            "player_games": s.get("player_games"),
            "opponent_games": s.get("opponent_games"),
            "tiebreak_player": s.get("tiebreak_player"),
            "tiebreak_opponent": s.get("tiebreak_opponent"),
        }
        cursor.execute(sql, params)


# ── Ranking ───────────────────────────────────────────────────────────

def upsert_ranking(data: dict) -> int:
    """Insert or update a ranking by (player_id, catalog_id, publish_date)."""
    sql = """
        INSERT INTO rankings (
            player_id, catalog_id, display_label, player_type,
            age_restriction, age_restriction_modifier, rank_list_gender,
            list_type, match_format, match_format_type, family_category,
            national_rank, section_rank, district_rank,
            points, singles_points, doubles_points, bonus_points,
            wins, losses, trend_direction, publish_date,
            section, district, state
        ) VALUES (
            %(player_id)s, %(catalog_id)s, %(display_label)s, %(player_type)s,
            %(age_restriction)s, %(age_restriction_modifier)s, %(rank_list_gender)s,
            %(list_type)s, %(match_format)s, %(match_format_type)s, %(family_category)s,
            %(national_rank)s, %(section_rank)s, %(district_rank)s,
            %(points)s, %(singles_points)s, %(doubles_points)s, %(bonus_points)s,
            %(wins)s, %(losses)s, %(trend_direction)s, %(publish_date)s,
            %(section)s, %(district)s, %(state)s
        )
        ON DUPLICATE KEY UPDATE
            display_label = VALUES(display_label),
            player_type = VALUES(player_type),
            age_restriction_modifier = VALUES(age_restriction_modifier),
            match_format = VALUES(match_format),
            match_format_type = VALUES(match_format_type),
            family_category = VALUES(family_category),
            national_rank = VALUES(national_rank),
            section_rank = VALUES(section_rank),
            district_rank = VALUES(district_rank),
            points = VALUES(points),
            singles_points = VALUES(singles_points),
            doubles_points = VALUES(doubles_points),
            bonus_points = VALUES(bonus_points),
            wins = VALUES(wins),
            losses = VALUES(losses),
            trend_direction = VALUES(trend_direction),
            section = VALUES(section),
            district = VALUES(district),
            state = VALUES(state)
    """
    defaults = {
        "player_id": None, "catalog_id": None, "display_label": None, "player_type": None,
        "age_restriction": None, "age_restriction_modifier": None, "rank_list_gender": None,
        "list_type": None, "match_format": None, "match_format_type": None, "family_category": None,
        "national_rank": None, "section_rank": None, "district_rank": None,
        "points": None, "singles_points": None, "doubles_points": None, "bonus_points": None,
        "wins": None, "losses": None, "trend_direction": None, "publish_date": None,
        "section": None, "district": None, "state": None,
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        return cur.lastrowid or 0


# ── Scrape Jobs ───────────────────────────────────────────────────────

def create_scrape_job(job_type: str, target_url: str = None, parameters: str = None) -> int:
    with get_cursor() as cur:
        cur.execute(
            """INSERT INTO scrape_jobs (job_type, status, target_url, parameters)
               VALUES (%s, 'RUNNING', %s, %s)""",
            (job_type, target_url, parameters),
        )
        return cur.lastrowid or 0


def update_scrape_job(
    job_id: int,
    status: str,
    records_processed: int = 0,
    records_created: int = 0,
    records_updated: int = 0,
    records_failed: int = 0,
    error_message: str = None,
):
    with get_cursor() as cur:
        cur.execute(
            """UPDATE scrape_jobs
               SET status = %s,
                   records_processed = %s, records_created = %s,
                   records_updated = %s, records_failed = %s,
                   error_message = %s,
                   completed_at = CURRENT_TIMESTAMP
               WHERE id = %s""",
            (status, records_processed, records_created, records_updated,
             records_failed, error_message, job_id),
        )


def log_scrape_error(job_id: int, url: str, error_type: str, error_message: str, http_status: int = None):
    with get_cursor() as cur:
        cur.execute(
            """INSERT INTO scrape_errors (scrape_job_id, url, error_type, error_message, http_status_code)
               VALUES (%s, %s, %s, %s, %s)""",
            (job_id, url, error_type, error_message, http_status),
        )


def get_pending_scrape_jobs() -> list[dict]:
    with get_cursor() as cur:
        cur.execute(
            "SELECT * FROM scrape_jobs WHERE status = 'PENDING' ORDER BY created_at ASC LIMIT 10"
        )
        return cur.fetchall()


def get_stale_player_uaids(hours: int = 6) -> list[str]:
    with get_cursor() as cur:
        cur.execute(
            "SELECT uaid FROM players WHERE updated_at < NOW() - INTERVAL %s HOUR ORDER BY updated_at ASC",
            (hours,),
        )
        return [row["uaid"] for row in cur.fetchall()]


def get_all_player_uaids() -> list[str]:
    with get_cursor() as cur:
        cur.execute("SELECT uaid FROM players ORDER BY id")
        return [row["uaid"] for row in cur.fetchall()]


# ── Tournament Entries ────────────────────────────────────────────────

def upsert_tournament_entry(data: dict) -> int:
    """Insert or update a tournament entry by (tournament_id, event_id, participant_id)."""
    sql = """
        INSERT INTO tournament_entries (
            tournament_id, event_id, participant_id,
            player_uaid, player_name, first_name, last_name,
            gender, city, state,
            event_type, entry_stage, entry_status,
            entry_position, status_detail, draw_id
        ) VALUES (
            %(tournament_id)s, %(event_id)s, %(participant_id)s,
            %(player_uaid)s, %(player_name)s, %(first_name)s, %(last_name)s,
            %(gender)s, %(city)s, %(state)s,
            %(event_type)s, %(entry_stage)s, %(entry_status)s,
            %(entry_position)s, %(status_detail)s, %(draw_id)s
        )
        ON DUPLICATE KEY UPDATE
            player_uaid = VALUES(player_uaid),
            player_name = VALUES(player_name),
            first_name = VALUES(first_name),
            last_name = VALUES(last_name),
            gender = VALUES(gender),
            city = VALUES(city),
            state = VALUES(state),
            event_type = VALUES(event_type),
            entry_stage = VALUES(entry_stage),
            entry_status = VALUES(entry_status),
            entry_position = VALUES(entry_position),
            status_detail = VALUES(status_detail),
            draw_id = VALUES(draw_id)
    """
    defaults = {
        "tournament_id": None, "event_id": None, "participant_id": None,
        "player_uaid": None, "player_name": None, "first_name": None,
        "last_name": None, "gender": None, "city": None, "state": None,
        "event_type": None, "entry_stage": None, "entry_status": None,
        "entry_position": None, "status_detail": None, "draw_id": None,
    }
    params = {**defaults, **data}
    with get_cursor() as cur:
        cur.execute(sql, params)
        return cur.lastrowid or 0


def update_tournament_detail_status(
    tournament_id: str, status: str, scraped_at: str | None = None
):
    """Update tournament detail scrape status and timestamp."""
    with get_cursor() as cur:
        if scraped_at:
            cur.execute(
                """UPDATE tournaments
                   SET detail_scrape_status = %s, detail_scraped_at = %s
                   WHERE tournament_id = %s""",
                (status, scraped_at, tournament_id),
            )
        else:
            cur.execute(
                """UPDATE tournaments
                   SET detail_scrape_status = %s, detail_scraped_at = CURRENT_TIMESTAMP
                   WHERE tournament_id = %s""",
                (status, tournament_id),
            )


def get_tournaments_to_scrape(
    date_from: str | None = None,
    date_to: str | None = None,
    limit: int | None = None,
) -> list[dict]:
    """Get tournaments that need detail scraping.

    Skips tournaments where:
    - detail was already scraped successfully AND
    - the tournament has ended (end_date < CURDATE()) AND
    - the scrape happened after the tournament ended

    Returns list of dicts with id, tournament_id, org_slug, name, start_date, end_date.
    """
    sql = """
        SELECT id, tournament_id, org_slug, name, start_date, end_date,
               detail_scraped_at, detail_scrape_status
        FROM tournaments
        WHERE tournament_id IS NOT NULL
          AND (%s IS NULL OR start_date >= %s)
          AND (%s IS NULL OR start_date <= %s)
          AND NOT (
              detail_scrape_status = 'SUCCESS'
              AND end_date IS NOT NULL
              AND end_date < CURDATE()
              AND detail_scraped_at IS NOT NULL
              AND detail_scraped_at > end_date
          )
        ORDER BY start_date ASC
    """
    params = [date_from, date_from, date_to, date_to]
    if limit:
        sql += " LIMIT %s"
        params.append(limit)

    with get_cursor() as cur:
        cur.execute(sql, params)
        return cur.fetchall()


def delete_tournament_entries(internal_id: int):
    """Delete all entries for a tournament before re-inserting."""
    with get_cursor() as cur:
        cur.execute(
            "DELETE FROM tournament_entries WHERE tournament_id = %s",
            (internal_id,),
        )
