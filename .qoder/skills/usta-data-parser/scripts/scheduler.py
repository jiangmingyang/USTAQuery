"""APScheduler-based cron jobs for periodic scraping."""

import logging

from apscheduler.schedulers.blocking import BlockingScheduler
from apscheduler.triggers.interval import IntervalTrigger

import config

logger = logging.getLogger(__name__)


def _refresh_stale_players():
    """Refresh player profiles that are more than 6 hours stale."""
    from scraper_main import scrape_single_player
    import db
    import browser as br

    uaids = db.get_stale_player_uaids(hours=config.SCHEDULE_PLAYER_REFRESH // 3600)
    logger.info("Scheduler: refreshing %d stale players", len(uaids))
    for uaid in uaids:
        try:
            scrape_single_player(uaid)
            br.delay()
        except Exception as e:
            logger.error("Scheduler: failed to refresh player %s: %s", uaid, e)


def _snapshot_rankings():
    """Take ranking snapshots for all tracked players."""
    from scraper_main import scrape_single_player
    import db
    import browser as br

    uaids = db.get_all_player_uaids()
    logger.info("Scheduler: taking ranking snapshots for %d players", len(uaids))
    for uaid in uaids:
        try:
            scrape_single_player(uaid)
            br.delay()
        except Exception as e:
            logger.error("Scheduler: failed ranking snapshot for %s: %s", uaid, e)


def _poll_pending_jobs():
    """Check for on-demand scrape requests from the backend."""
    from scraper_main import process_pending_jobs
    try:
        process_pending_jobs()
    except Exception as e:
        logger.error("Scheduler: failed to process pending jobs: %s", e)


def run_scheduler():
    """Start the APScheduler daemon with configured intervals."""
    scheduler = BlockingScheduler()

    logger.info("Scheduler started — no default jobs configured (manual mode)")

    try:
        scheduler.start()
    except (KeyboardInterrupt, SystemExit):
        logger.info("Scheduler shutting down")
        scheduler.shutdown()
