"""Scrape tournament listings from playtennis.usta.com."""
from __future__ import annotations

import logging
import time

from playwright.sync_api import Page

import config
import browser as br

logger = logging.getLogger(__name__)


def scrape_tournament_list(page: Page, org_guid: str) -> list[dict]:
    """
    Navigate to the tournament list for an organization group and extract all tournament cards.
    Returns a list of dicts with summary info + links for detail scraping.
    """
    url = config.PLAYTENNIS_TOURNAMENT_LIST_URL.format(org_guid=org_guid)
    logger.info("Scraping tournament list: %s", url)

    page.goto(url, timeout=config.TIMEOUT)
    _wait_for_list(page)

    tournaments = []
    _load_all_tournaments(page)

    cards = page.locator(
        ".tournament-card, [class*='TournamentCard'], "
        "[class*='tournament-item'], [class*='tournamentList'] > div, "
        "a[href*='/Tournaments/overview/']"
    )
    count = cards.count()
    logger.info("Found %d tournament cards", count)

    for i in range(count):
        card = cards.nth(i)
        try:
            tournament = _parse_tournament_card(card)
            if tournament:
                tournaments.append(tournament)
        except Exception as e:
            logger.warning("Failed to parse tournament card %d: %s", i, e)

    return tournaments


def _wait_for_list(page: Page):
    """Wait for tournament list to render."""
    try:
        page.wait_for_load_state("domcontentloaded", timeout=config.TIMEOUT)
    except Exception:
        pass
    time.sleep(3)
    # Wait for at least one tournament card or a "no results" message
    try:
        page.locator(
            ".tournament-card, [class*='TournamentCard'], "
            "[class*='tournament-item'], [class*='no-results'], "
            "[class*='emptyState']"
        ).first.wait_for(timeout=config.TIMEOUT)
    except Exception:
        logger.warning("No tournament cards or empty-state found after waiting")


def _load_all_tournaments(page: Page):
    """Click 'Load More' / pagination buttons until all tournaments are visible."""
    max_clicks = 20
    for _ in range(max_clicks):
        load_more = page.locator(
            "button:has-text('Load More'), button:has-text('Show More'), "
            "button:has-text('View More'), a:has-text('Load More')"
        ).first
        try:
            if load_more.is_visible(timeout=2000):
                load_more.click()
                br.delay()
            else:
                break
        except Exception:
            break


def _parse_tournament_card(card) -> dict | None:
    """Extract data from a single tournament card element."""
    text = card.inner_text().strip()
    if not text:
        return None

    # Try to extract the link to the tournament detail page
    href = None
    try:
        link = card.locator("a[href*='/Tournaments/overview/']").first
        href = link.get_attribute("href")
    except Exception:
        try:
            href = card.get_attribute("href")
        except Exception:
            pass

    # Parse org_slug and tournament_guid from href
    org_slug = None
    tournament_guid = None
    if href:
        parts = href.split("/")
        for idx, part in enumerate(parts):
            if part == "Competitions" and idx + 1 < len(parts):
                org_slug = parts[idx + 1]
            if part == "overview" and idx + 1 < len(parts):
                tournament_guid = parts[idx + 1].split("?")[0]

    lines = [l.strip() for l in text.split("\n") if l.strip()]
    return {
        "name": lines[0] if lines else None,
        "date_text": lines[1] if len(lines) > 1 else None,
        "location_text": lines[2] if len(lines) > 2 else None,
        "org_slug": org_slug,
        "tournament_guid": tournament_guid,
        "detail_url": href,
        "raw_text": text,
    }
