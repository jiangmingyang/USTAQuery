"""USTA Scraper — CLI entry point and orchestrator."""
from __future__ import annotations

import argparse
import json
import logging
import sys
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

import browser as br
import config
import db

logger = logging.getLogger(__name__)

# All supported junior ranking list types
ALL_LIST_TYPES = [
    "combined", "seeding", "doubles", "bonusPoints",
    "quota", "combinedYearEnd", "doublesYearEnd",
]


def scrape_single_player(uaid: str):
    """Scrape a single player by UAID using API response interception."""
    from pages.player_page import scrape_player

    job_id = db.create_scrape_job("player_profile", f"uaid={uaid}")
    page = br.get_page()
    stats = {"processed": 0, "created": 0, "updated": 0, "failed": 0}

    try:
        # No login needed — API data is publicly accessible
        data = scrape_player(page, uaid)

        # Upsert player
        if data["player"]:
            player_id = db.upsert_player(data["player"])
            stats["processed"] += 1
            stats["created" if player_id else "updated"] += 1
            logger.info("Player upserted: id=%s, uaid=%s", player_id, uaid)

            # Upsert rankings
            for r in data.get("rankings", []):
                r["player_id"] = player_id
                db.upsert_ranking(r)
                stats["processed"] += 1
                stats["created"] += 1

        db.update_scrape_job(
            job_id, "COMPLETED",
            records_processed=stats["processed"],
            records_created=stats["created"],
            records_updated=stats["updated"],
        )
        logger.info("Scrape completed for player %s: %s", uaid, stats)

    except Exception as e:
        logger.error("Scrape failed for player %s: %s", uaid, e)
        db.update_scrape_job(job_id, "FAILED", error_message=str(e),
                            records_failed=stats["failed"] + 1)
        db.log_scrape_error(job_id, f"uaid={uaid}", type(e).__name__, str(e))
    finally:
        page.close()


def scrape_tournament_list(org_guid: str):
    """Scrape tournament listings for an organization group."""
    from pages.tournament_list_page import scrape_tournament_list as _scrape

    job_id = db.create_scrape_job("tournament_list", f"org_guid={org_guid}")
    page = br.get_page()
    stats = {"processed": 0, "created": 0, "failed": 0}

    try:
        br.ensure_logged_in(page)
        br.delay()

        tournaments = _scrape(page, org_guid)

        for t in tournaments:
            try:
                if t.get("tournament_guid"):
                    db.upsert_tournament({
                        "tournament_id": t["tournament_guid"],
                        "name": t.get("name"),
                        "url": t.get("detail_url"),
                    })
                    stats["processed"] += 1
                    stats["created"] += 1
            except Exception as e:
                logger.warning("Failed to save tournament: %s", e)
                stats["failed"] += 1
                db.log_scrape_error(job_id, t.get("detail_url", ""), type(e).__name__, str(e))

        db.update_scrape_job(
            job_id, "COMPLETED",
            records_processed=stats["processed"],
            records_created=stats["created"],
            records_failed=stats["failed"],
        )
        logger.info("Tournament list scrape completed: %s", stats)

    except Exception as e:
        logger.error("Tournament list scrape failed: %s", e)
        db.update_scrape_job(job_id, "FAILED", error_message=str(e))
    finally:
        page.close()


def scrape_tournament_detail(org_slug: str, tournament_guid: str):
    """Scrape a single tournament's full detail."""
    from pages.tournament_detail_page import scrape_tournament_detail as _scrape

    job_id = db.create_scrape_job(
        "tournament_detail",
        f"org_slug={org_slug}&guid={tournament_guid}",
    )
    page = br.get_page()
    stats = {"processed": 0, "created": 0, "failed": 0}

    try:
        br.ensure_logged_in(page)
        br.delay()

        data = _scrape(page, org_slug, tournament_guid)

        # Upsert tournament
        if data["tournament"]:
            t_id = db.upsert_tournament(data["tournament"])
            stats["processed"] += 1
            stats["created"] += 1

            # Upsert registrations
            internal_tid = db.get_tournament_internal_id(tournament_guid)
            for reg in data.get("registrations", []):
                reg["tournament_id"] = internal_tid
                reg["player1_id"] = _resolve_player(reg.get("player1_name"))
                reg["player2_id"] = _resolve_player(reg.get("player2_name"))
                if reg["player1_id"]:
                    try:
                        db.upsert_registration(reg)
                        stats["created"] += 1
                    except Exception as e:
                        logger.warning("Failed to save registration: %s", e)
                        stats["failed"] += 1
                stats["processed"] += 1

            # Upsert matches
            for m in data.get("matches", []):
                m["tournament_id"] = internal_tid
                sets_data = m.pop("sets", [])
                m["player1_id"] = _resolve_player(m.get("player1_name"))
                m["opponent1_id"] = _resolve_player(m.get("opponent1_name"))
                try:
                    db.upsert_match(m, sets_data)
                    stats["created"] += 1
                except Exception as e:
                    logger.warning("Failed to save match: %s", e)
                    stats["failed"] += 1
                stats["processed"] += 1

        db.update_scrape_job(
            job_id, "COMPLETED",
            records_processed=stats["processed"],
            records_created=stats["created"],
            records_failed=stats["failed"],
        )
        logger.info("Tournament detail scrape completed: %s", stats)

    except Exception as e:
        logger.error("Tournament detail scrape failed: %s", e)
        db.update_scrape_job(job_id, "FAILED", error_message=str(e))
    finally:
        page.close()


def scrape_tournament_api(
    level_category: str = "junior",
    year: int | None = None,
    month: str | None = None,
    date_from: str | None = None,
    date_to: str | None = None,
    all_sections: bool = False,
    org_group: str | None = None,
    max_pages: int | None = None,
):
    """Scrape tournament listings + events via the USTA search API."""
    from pages.tournament_api_page import scrape_tournament_api as _fetch
    from pages.tournament_api_page import fetch_sections
    from parsers.tournament_api_parser import parse_tournament_results

    # Resolve date range
    if year:
        date_from = f"{year}-01-01"
        date_to = f"{year}-12-31"
    elif month:
        from datetime import date, timedelta
        parts = month.split("-")
        y, m = int(parts[0]), int(parts[1])
        first_day = date(y, m, 1)
        if m == 12:
            last_day = date(y + 1, 1, 1) - timedelta(days=1)
        else:
            last_day = date(y, m + 1, 1) - timedelta(days=1)
        date_from = first_day.isoformat()
        date_to = last_day.isoformat()

    job_id = db.create_scrape_job(
        "tournament_api",
        f"level={level_category}&dates={date_from}~{date_to}",
    )
    stats = {"tournaments": 0, "events": 0, "failed": 0}

    try:
        # Determine sections to scrape
        if all_sections:
            page = br.get_page()
            try:
                sections = fetch_sections(page)
            finally:
                page.close()
            if not sections:
                raise RuntimeError("Failed to fetch sections list")
            # Skip "National" — its results overlap with all other sections
            # and would overwrite their section assignments.
            sections = [s for s in sections if s["name"] != "National"]
            # Append an aggregate scrape (no section filter) to catch
            # national-only tournaments without overwriting existing sections.
            # section_name=None means COALESCE in the upsert preserves
            # any previously-assigned section value.
            sections.append({"name": None, "value": None})
            logger.info("Scraping %d sections + aggregate", len(sections) - 1)
        elif org_group:
            sections = [{"name": None, "value": org_group}]
        else:
            raise ValueError("Must specify --all-sections or --org-group")

        def _section_worker(section):
            section_name = section["name"]
            section_value = section["value"]
            label = section_name or ("(aggregate)" if section_value is None else section_value)
            local_stats = {"tournaments": 0, "events": 0, "failed": 0}
            page = br.get_page()
            try:
                logger.info("Section %s: starting", label)
                items = _fetch(
                    page, section_value, level_category,
                    date_from, date_to, max_pages,
                )
                parsed = parse_tournament_results(items, section_name)

                for entry in parsed:
                    try:
                        t_data = entry["tournament"]
                        internal_id = db.upsert_tournament(t_data)
                        local_stats["tournaments"] += 1

                        for ev in entry["events"]:
                            ev["tournament_id"] = internal_id
                            db.upsert_tournament_event(ev)
                            local_stats["events"] += 1

                    except Exception as e:
                        code = entry.get("tournament", {}).get("code", "?")
                        logger.warning("Failed to save tournament %s: %s", code, e)
                        local_stats["failed"] += 1

                logger.info(
                    "Section %s done: %d tournaments, %d events",
                    label, len(parsed), sum(len(e["events"]) for e in parsed),
                )
            except Exception as e:
                logger.error("Section %s failed: %s", label, e)
                local_stats["failed"] += 1
            finally:
                page.close()
            return local_stats

        logger.info("Starting tournament API scrape (concurrency=%d)", config.CONCURRENCY)
        with ThreadPoolExecutor(max_workers=config.CONCURRENCY) as executor:
            futures = {executor.submit(_section_worker, s): s for s in sections}
            for future in as_completed(futures):
                result = future.result()
                stats["tournaments"] += result["tournaments"]
                stats["events"] += result["events"]
                stats["failed"] += result["failed"]

        db.update_scrape_job(
            job_id, "COMPLETED",
            records_processed=stats["tournaments"] + stats["events"],
            records_created=stats["tournaments"],
            records_updated=stats["events"],
            records_failed=stats["failed"],
        )
        logger.info(
            "Tournament API scrape completed: %d tournaments, %d events, %d failed",
            stats["tournaments"], stats["events"], stats["failed"],
        )

    except Exception as e:
        logger.error("Tournament API scrape failed: %s", e)
        db.update_scrape_job(job_id, "FAILED", error_message=str(e))
        raise


def scrape_single_tournament_detail_graphql(
    tournament_id: str,
    org_slug: str,
):
    """Scrape a single tournament's entries via GraphQL (for testing).

    Looks up the tournament by tournament_id, scrapes participants,
    and upserts entries into the database.
    """
    from pages.tournament_detail_graphql import scrape_tournament_detail_graphql

    internal_id = db.get_tournament_internal_id(tournament_id)
    if internal_id is None:
        logger.error(
            "Tournament %s not found in database. "
            "Run tournament-api scrape first to populate tournament records.",
            tournament_id,
        )
        sys.exit(1)

    job_id = db.create_scrape_job(
        "tournament_detail_single",
        f"tournament_id={tournament_id}&org_slug={org_slug}",
    )

    usta_url = f"https://playtennis.usta.com/Competitions/{org_slug}/Tournaments/players/{tournament_id}"

    page = br.get_page()
    try:
        result = scrape_tournament_detail_graphql(page, tournament_id, org_slug)
        entries = result.get("entries", [])

        if not entries:
            truly_empty = result.get("truly_empty", False)
            if truly_empty:
                from datetime import date
                end_date = db.get_tournament_end_date(tournament_id)
                if end_date and end_date < date.today():
                    status = "SUCCESS"
                    logger.info("Tournament %s — no players registered (ended), marked SUCCESS", tournament_id)
                else:
                    status = "EMPTY"
                    logger.info("Tournament %s — no players registered (not ended), marked EMPTY", tournament_id)
                db.update_tournament_detail_status(tournament_id, status)
                db.update_scrape_job(
                    job_id, "COMPLETED",
                    records_processed=0, records_created=0,
                )
            else:
                logger.error(
                    "[FAILED] Tournament %s — scrape returned no data, marked FAILED for retry\n  %s",
                    tournament_id, usta_url,
                )
                db.update_tournament_detail_status(tournament_id, "FAILED")
                db.update_scrape_job(
                    job_id, "FAILED",
                    error_message="APIs returned empty but page does not confirm no players",
                )
            return

        # Delete existing entries and re-insert
        db.delete_tournament_entries(internal_id)

        saved = 0
        for entry in entries:
            entry["tournament_id"] = internal_id
            try:
                db.upsert_tournament_entry(entry)
                saved += 1
            except Exception as e:
                logger.warning(
                    "Failed to save entry for %s: %s",
                    entry.get("player_name", "?"), e,
                )

        db.update_tournament_detail_status(tournament_id, "SUCCESS")
        db.update_scrape_job(
            job_id, "COMPLETED",
            records_processed=len(entries),
            records_created=saved,
            records_failed=len(entries) - saved,
        )
        logger.info("Tournament %s — %d/%d entries saved", tournament_id, saved, len(entries))

    except Exception as e:
        logger.error("[FAILED] Tournament %s — %s\n  %s", tournament_id, e, usta_url)
        db.update_tournament_detail_status(tournament_id, "FAILED")
        db.update_scrape_job(job_id, "FAILED", error_message=str(e))
        raise
    finally:
        page.close()


def scrape_tournament_details_batch(
    year: int | None = None,
    date_from: str | None = None,
    date_to: str | None = None,
    limit: int | None = None,
    delay: float = 3.0,
):
    """Batch-scrape tournament detail (participants/entries) via GraphQL.

    For each tournament in the date range:
    - Skip if already scraped successfully after the tournament ended
    - Otherwise scrape participants and upsert entries
    - Record scrape status and timestamp
    """
    from pages.tournament_detail_graphql import scrape_tournament_detail_graphql

    # Resolve date range from year
    if year:
        date_from = f"{year}-01-01"
        date_to = f"{year}-12-31"

    tournaments = db.get_tournaments_to_scrape(date_from, date_to, limit)
    total = len(tournaments)

    if total == 0:
        logger.info("No tournaments need detail scraping")
        return

    job_id = db.create_scrape_job(
        "tournament_details_batch",
        f"dates={date_from}~{date_to}&total={total}",
    )
    stats = {"succeeded": 0, "entries": 0, "failed": 0, "empty": 0}
    _progress_lock = threading.Lock()
    _progress = {"done": 0}

    logger.info(
        "Starting tournament details batch: %d tournaments (concurrency=%d)",
        total, config.CONCURRENCY,
    )

    def _detail_worker(t):
        tid = t["tournament_id"]
        org_slug = t.get("org_slug") or "abc"
        name = t.get("name") or tid
        usta_url = f"https://playtennis.usta.com/Competitions/{org_slug}/Tournaments/players/{tid}"
        local_stats = {"succeeded": 0, "entries": 0, "failed": 0, "empty": 0}
        page = br.get_page()
        try:
            result = scrape_tournament_detail_graphql(page, tid, org_slug)
            entries = result.get("entries", [])

            if not entries:
                truly_empty = result.get("truly_empty", False)
                if truly_empty:
                    end_date = t.get("end_date")
                    from datetime import date
                    if end_date and end_date < date.today():
                        db.update_tournament_detail_status(tid, "SUCCESS")
                    else:
                        db.update_tournament_detail_status(tid, "EMPTY")
                    local_stats["empty"] += 1
                else:
                    logger.error(
                        "[FAILED] %s — scrape returned no data, marking FAILED for retry\n  %s",
                        name, usta_url,
                    )
                    db.update_tournament_detail_status(tid, "FAILED")
                    local_stats["failed"] += 1
                return local_stats

            # Delete existing entries and re-insert
            internal_id = t["id"]
            db.delete_tournament_entries(internal_id)

            for entry in entries:
                entry["tournament_id"] = internal_id
                try:
                    db.upsert_tournament_entry(entry)
                    local_stats["entries"] += 1
                except Exception as e:
                    logger.warning(
                        "Failed to save entry for %s: %s",
                        entry.get("player_name", "?"), e,
                    )

            db.update_tournament_detail_status(tid, "SUCCESS")
            local_stats["succeeded"] += 1

        except Exception as e:
            logger.error("[FAILED] %s — %s\n  %s", name, e, usta_url)
            db.update_tournament_detail_status(tid, "FAILED")
            db.log_scrape_error(
                job_id, f"tournament_id={tid}", type(e).__name__, str(e)
            )
            local_stats["failed"] += 1
        finally:
            page.close()

        # Progress
        with _progress_lock:
            _progress["done"] += 1
            done = _progress["done"]
        status = (
            f"{local_stats['entries']} entries"
            if local_stats["succeeded"]
            else ("empty" if local_stats["empty"] else "FAILED")
        )
        logger.info(
            "[%d/%d] %s — %s",
            done, total, name, status,
        )
        return local_stats

    with ThreadPoolExecutor(max_workers=config.CONCURRENCY) as executor:
        futures = {executor.submit(_detail_worker, t): t for t in tournaments}
        for future in as_completed(futures):
            result = future.result()
            stats["succeeded"] += result["succeeded"]
            stats["entries"] += result["entries"]
            stats["failed"] += result["failed"]
            stats["empty"] += result["empty"]

    db.update_scrape_job(
        job_id, "COMPLETED",
        records_processed=stats["succeeded"] + stats["failed"] + stats["empty"],
        records_created=stats["entries"],
        records_failed=stats["failed"],
    )
    logger.info(
        "Batch completed: %d succeeded (%d entries), %d empty, %d failed — total %d",
        stats["succeeded"], stats["entries"], stats["empty"], stats["failed"], total,
    )


def scrape_full_sync():
    """Discover players via rankings, then re-scrape stale player profiles."""
    logger.info("Starting full sync...")

    # Phase 1: Refresh rankings to discover all players
    logger.info("Phase 1: Scraping rankings to discover players...")
    scrape_rankings(
        ["Y12", "Y14", "Y16", "Y18"], ["M", "F"], ALL_LIST_TYPES, max_pages=None
    )

    # Phase 2: Re-scrape stale player profiles
    stale_uaids = db.get_stale_player_uaids(hours=6)
    logger.info("Phase 2: Found %d stale players to refresh", len(stale_uaids))
    for uaid in stale_uaids:
        try:
            scrape_single_player(uaid)
            br.delay()
        except Exception as e:
            logger.error("Failed to refresh player %s: %s", uaid, e)

    logger.info("Full sync completed")


def scrape_rankings(
    age_groups: list[str],
    genders: list[str],
    list_types: list[str],
    max_pages: int | None = None,
):
    """Scrape rankings lists across all age/gender/listType combos."""
    from pages.rankings_page import scrape_rankings_list

    combos = [
        (age, gender, lt)
        for age in age_groups
        for gender in genders
        for lt in list_types
    ]
    total_combos = len(combos)

    job_id = db.create_scrape_job(
        "rankings_list",
        f"ages={age_groups}&genders={genders}&list_types={list_types}",
    )
    stats = {"players": 0, "rankings": 0, "failed": 0}

    logger.info("Starting rankings scrape: %d combos (concurrency=%d)", total_combos, config.CONCURRENCY)

    def _rankings_worker(combo):
        age, gender, lt = combo
        page = br.get_page()
        local_stats = {"players": 0, "rankings": 0, "failed": 0}
        try:
            logger.info("Combo %s %s %s: starting", age, gender, lt)
            entries = scrape_rankings_list(page, age, gender, lt, max_pages)

            for entry in entries:
                try:
                    player_id = db.upsert_player_basic(entry["player"])
                    entry["ranking"]["player_id"] = player_id
                    db.upsert_ranking(entry["ranking"])
                    local_stats["players"] += 1
                    local_stats["rankings"] += 1
                except Exception as e:
                    logger.warning(
                        "Failed to save player %s: %s",
                        entry["player"].get("uaid"), e,
                    )
                    local_stats["failed"] += 1

            logger.info("Combo %s %s %s done: %d players saved", age, gender, lt, len(entries))
        except Exception as e:
            logger.error("Combo %s %s %s failed: %s", age, gender, lt, e)
            local_stats["failed"] += 1
        finally:
            page.close()
        return local_stats

    with ThreadPoolExecutor(max_workers=config.CONCURRENCY) as executor:
        futures = {executor.submit(_rankings_worker, c): c for c in combos}
        for future in as_completed(futures):
            result = future.result()
            stats["players"] += result["players"]
            stats["rankings"] += result["rankings"]
            stats["failed"] += result["failed"]

    db.update_scrape_job(
        job_id, "COMPLETED",
        records_processed=stats["players"] + stats["rankings"],
        records_created=stats["players"],
        records_updated=stats["rankings"],
        records_failed=stats["failed"],
    )
    logger.info(
        "Rankings scrape completed: %d players, %d rankings, %d failed",
        stats["players"], stats["rankings"], stats["failed"],
    )


def scrape_batch_players(delay: float, limit: int | None = None):
    """Iterate through all known players and scrape each one's full profile."""
    uaids = db.get_all_player_uaids()
    if not uaids:
        logger.warning("No players in database - run rankings scrape first")
        return

    if limit is not None:
        uaids = uaids[:limit]

    total = len(uaids)
    job_id = db.create_scrape_job("batch_player", f"total={total}&concurrency={config.CONCURRENCY}")
    succeeded = 0
    failed = 0

    logger.info("Starting batch scrape: %d players (concurrency=%d)", total, config.CONCURRENCY)

    def _player_worker(uaid):
        try:
            scrape_single_player(uaid)
            return True
        except Exception as e:
            logger.error("Player %s failed: %s", uaid, e)
            return False

    with ThreadPoolExecutor(max_workers=config.CONCURRENCY) as executor:
        futures = {executor.submit(_player_worker, u): u for u in uaids}
        for future in as_completed(futures):
            if future.result():
                succeeded += 1
            else:
                failed += 1

    db.update_scrape_job(
        job_id, "COMPLETED",
        records_processed=total,
        records_created=succeeded,
        records_failed=failed,
    )
    logger.info(
        "Batch complete: %d/%d succeeded, %d failed", succeeded, total, failed
    )


def process_pending_jobs():
    """Process any pending scrape_jobs from the database."""
    jobs = db.get_pending_scrape_jobs()
    if not jobs:
        return

    logger.info("Processing %d pending scrape jobs", len(jobs))
    for job in jobs:
        try:
            params = json.loads(job.get("parameters") or "{}") if job.get("parameters") else {}
            job_type = job.get("job_type", "")

            if job_type == "player_profile" and params.get("uaid"):
                scrape_single_player(params["uaid"])
            elif job_type == "tournament_list" and params.get("org_guid"):
                scrape_tournament_list(params["org_guid"])
            elif job_type == "tournament_detail" and params.get("org_slug") and params.get("guid"):
                scrape_tournament_detail(params["org_slug"], params["guid"])
            else:
                logger.warning("Unknown job type: %s", job_type)
                db.update_scrape_job(job["id"], "FAILED", error_message=f"Unknown job type: {job_type}")
        except Exception as e:
            logger.error("Failed to process job %s: %s", job.get("id"), e)


def _resolve_player(name: str | None) -> int | None:
    """Try to find a player ID by name. Returns None if not found."""
    if not name:
        return None
    with db.get_cursor() as cur:
        parts = name.split(None, 1)
        if len(parts) == 2:
            cur.execute(
                "SELECT id FROM players WHERE first_name = %s AND last_name = %s LIMIT 1",
                (parts[0], parts[1]),
            )
        else:
            cur.execute(
                "SELECT id FROM players WHERE last_name = %s LIMIT 1",
                (name,),
            )
        row = cur.fetchone()
        return row["id"] if row else None


def main():
    parser = argparse.ArgumentParser(description="USTA Tennis Data Scraper")
    parser.add_argument(
        "--mode",
        required=True,
        choices=[
            "single", "rankings", "batch",
            "tournaments", "tournament-detail", "tournament-api",
            "tournament-details", "tournament-detail-graphql",
            "full-sync", "scheduler", "poll-jobs",
        ],
        help="Scraper mode",
    )
    parser.add_argument("--uaid", help="Player UAID (for 'single' mode)")
    parser.add_argument("--org-guid", help="Organization group GUID (for 'tournaments' mode)")
    parser.add_argument("--org-slug", help="Organization slug (for 'tournament-detail' and 'tournament-detail-graphql' modes)")
    parser.add_argument("--guid", help="Tournament GUID (for 'tournament-detail' mode)")
    parser.add_argument("--tournament-id", help="Tournament ID/GUID (for 'tournament-detail-graphql' mode)")
    parser.add_argument(
        "--age", nargs="+", default=["Y12", "Y14", "Y16", "Y18"],
        choices=["Y12", "Y14", "Y16", "Y18"],
        help="Age groups for 'rankings' mode (default: all)",
    )
    parser.add_argument(
        "--gender", nargs="+", default=["M", "F"],
        choices=["M", "F"],
        help="Genders for 'rankings' mode (default: both)",
    )
    parser.add_argument(
        "--list-type", nargs="+", default=ALL_LIST_TYPES,
        choices=ALL_LIST_TYPES,
        help="Junior list types for 'rankings' mode (default: all 7)",
    )
    parser.add_argument(
        "--max-pages", type=int, default=None,
        help="Max pages per combo for 'rankings' mode",
    )
    parser.add_argument(
        "--delay", type=float, default=None,
        help="Delay in seconds between players for 'batch' mode",
    )
    parser.add_argument(
        "--limit", type=int, default=None,
        help="Max players to scrape in 'batch' mode",
    )
    # Tournament-api mode options
    parser.add_argument(
        "--level-category", default="junior",
        choices=["junior", "adult", "wheelchair", "wtnPlay"],
        help="Level category for 'tournament-api' mode (default: junior)",
    )
    parser.add_argument("--year", type=int, help="Year for 'tournament-api' mode")
    parser.add_argument("--month", help="Month YYYY-MM for 'tournament-api' mode")
    parser.add_argument("--from-date", help="Start date YYYY-MM-DD for 'tournament-api' mode")
    parser.add_argument("--to-date", help="End date YYYY-MM-DD for 'tournament-api' mode")
    parser.add_argument("--all-sections", action="store_true", help="Scrape all sections")
    parser.add_argument("--org-group", help="Specific org group ID for 'tournament-api' mode")
    parser.add_argument("--debug", action="store_true", help="Enable debug logging")

    args = parser.parse_args()

    # Setup logging
    level = logging.DEBUG if args.debug else logging.INFO
    logging.basicConfig(
        level=level,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    try:
        if args.mode == "single":
            if not args.uaid:
                parser.error("--uaid is required for 'single' mode")
            scrape_single_player(args.uaid)

        elif args.mode == "rankings":
            scrape_rankings(args.age, args.gender, args.list_type, args.max_pages)

        elif args.mode == "batch":
            delay = args.delay if args.delay is not None else config.BATCH_PLAYER_DELAY
            scrape_batch_players(delay=delay, limit=args.limit)

        elif args.mode == "tournaments":
            if not args.org_guid:
                parser.error("--org-guid is required for 'tournaments' mode")
            scrape_tournament_list(args.org_guid)

        elif args.mode == "tournament-detail":
            if not args.org_slug or not args.guid:
                parser.error("--org-slug and --guid are required for 'tournament-detail' mode")
            scrape_tournament_detail(args.org_slug, args.guid)

        elif args.mode == "tournament-api":
            if not args.all_sections and not args.org_group:
                parser.error("--all-sections or --org-group is required for 'tournament-api' mode")
            scrape_tournament_api(
                level_category=args.level_category,
                year=args.year,
                month=args.month,
                date_from=args.from_date,
                date_to=args.to_date,
                all_sections=args.all_sections,
                org_group=args.org_group,
                max_pages=args.max_pages,
            )

        elif args.mode == "tournament-details":
            delay = args.delay if args.delay is not None else 3.0
            scrape_tournament_details_batch(
                year=args.year,
                date_from=args.from_date,
                date_to=args.to_date,
                limit=args.limit,
                delay=delay,
            )

        elif args.mode == "tournament-detail-graphql":
            if not args.tournament_id or not args.org_slug:
                parser.error("--tournament-id and --org-slug are required for 'tournament-detail-graphql' mode")
            scrape_single_tournament_detail_graphql(args.tournament_id, args.org_slug)

        elif args.mode == "full-sync":
            scrape_full_sync()

        elif args.mode == "scheduler":
            from scheduler import run_scheduler
            run_scheduler()

        elif args.mode == "poll-jobs":
            process_pending_jobs()

    except KeyboardInterrupt:
        logger.info("Scraper interrupted by user")
    except Exception as e:
        logger.error("Scraper failed: %s", e, exc_info=True)
        sys.exit(1)
    finally:
        br.close()


if __name__ == "__main__":
    main()
