"""Scrape tournament detail pages from playtennis.usta.com."""
from __future__ import annotations

import logging
import re
import time

from playwright.sync_api import Page

import config
import browser as br
from parsers import tournament_parser, match_parser

logger = logging.getLogger(__name__)


def scrape_tournament_detail(
    page: Page, org_slug: str, tournament_guid: str
) -> dict:
    """
    Navigate to a tournament detail page and scrape overview, registrations, and match results.
    Returns dict with keys: tournament, registrations, matches.
    """
    url = config.PLAYTENNIS_TOURNAMENT_DETAIL_URL.format(
        org_slug=org_slug, tournament_guid=tournament_guid
    )
    logger.info("Scraping tournament detail: %s", url)

    page.goto(url, timeout=config.TIMEOUT)
    _wait_for_detail(page)

    result = {"tournament": {}, "registrations": [], "matches": []}

    # ── Overview tab ──────────────────────────────────────────────────
    result["tournament"] = tournament_parser.parse_tournament_overview(
        page, tournament_guid
    )
    logger.info("Parsed tournament: %s", result["tournament"].get("name"))

    br.delay()

    # ── Draws / Entries tab ───────────────────────────────────────────
    try:
        _navigate_tab(page, "draws", "entries")
        result["registrations"] = _extract_registrations(page)
        logger.info("Parsed %d registrations", len(result["registrations"]))
    except Exception as e:
        logger.warning("Failed to scrape registrations for %s: %s", tournament_guid, e)
        br.screenshot_on_error(page, f"tournament_{tournament_guid}_draws")

    br.delay()

    # ── Results tab ───────────────────────────────────────────────────
    try:
        _navigate_tab(page, "results")
        result["matches"] = match_parser.parse_tournament_matches(page)
        logger.info("Parsed %d match results", len(result["matches"]))
    except Exception as e:
        logger.warning("Failed to scrape results for %s: %s", tournament_guid, e)
        br.screenshot_on_error(page, f"tournament_{tournament_guid}_results")

    return result


def _wait_for_detail(page: Page):
    """Wait for tournament detail page to render."""
    try:
        page.wait_for_load_state("domcontentloaded", timeout=config.TIMEOUT)
    except Exception:
        pass
    time.sleep(3)
    try:
        page.locator(
            "h1, .tournament-name, [class*='tournamentName'], "
            "[class*='TournamentHeader']"
        ).first.wait_for(timeout=config.TIMEOUT)
    except Exception:
        logger.warning("Tournament name element not found on detail page")


def _navigate_tab(page: Page, *tab_names: str):
    """Click a tab on the tournament detail page."""
    for name in tab_names:
        tab = page.locator(
            f"a:has-text('{name}'), button:has-text('{name}'), "
            f"[role='tab']:has-text('{name}'), "
            f"nav a[href*='{name}'], .tab:has-text('{name}')"
        ).first
        try:
            if tab.is_visible(timeout=3000):
                tab.click()
                time.sleep(3)
                return
        except Exception:
            continue
    logger.warning("Could not find tab: %s", tab_names)


def _extract_registrations(page: Page) -> list[dict]:
    """Extract registration / draw entries from the draws tab."""
    registrations = []

    # Look for draw sections (each division is a separate draw)
    draw_sections = page.locator(
        ".draw-section, [class*='DrawSection'], "
        "[class*='draw-container'], [class*='bracket']"
    )

    # If no distinct draw sections, try to find a table of entries
    if draw_sections.count() == 0:
        return _extract_flat_registrations(page)

    for i in range(draw_sections.count()):
        section = draw_sections.nth(i)
        try:
            # Get division name from section header
            header = section.locator("h2, h3, h4, .draw-title, [class*='drawName']").first
            division_name = header.inner_text().strip() if header.count() > 0 else f"Draw {i + 1}"

            # Get entries in this draw
            entries = section.locator(
                "tr, .entry-row, .player-entry, [class*='entryRow'], [class*='seedEntry']"
            )
            for j in range(entries.count()):
                entry = entries.nth(j)
                reg = _parse_entry_row(entry, division_name)
                if reg:
                    registrations.append(reg)
        except Exception as e:
            logger.warning("Failed to parse draw section %d: %s", i, e)

    return registrations


def _extract_flat_registrations(page: Page) -> list[dict]:
    """Fallback: extract registrations from a flat table/list."""
    registrations = []
    rows = page.locator(
        "table tbody tr, .entry-row, .player-entry, [class*='entryRow']"
    )
    for i in range(rows.count()):
        reg = _parse_entry_row(rows.nth(i), "Unknown Division")
        if reg:
            registrations.append(reg)
    return registrations


def _parse_entry_row(entry, division_name: str) -> dict | None:
    """Parse a single draw entry row into a registration dict."""
    text = entry.inner_text().strip()
    if not text or len(text) < 3:
        return None

    lines = [l.strip() for l in text.split("\n") if l.strip()]

    # Try to detect doubles (look for "/" or "& " separator between player names)
    match_type = "SINGLES"
    player1_name = lines[0] if lines else None
    player2_name = None

    if player1_name:
        for sep in [" / ", " & ", " and "]:
            if sep in player1_name:
                parts = player1_name.split(sep, 1)
                player1_name = parts[0].strip()
                player2_name = parts[1].strip()
                match_type = "DOUBLES"
                break

    # Try to detect seed
    seed = None
    seed_match = re.search(r"\[(\d+)\]", text)
    if seed_match:
        seed = int(seed_match.group(1))

    return {
        "division_name": division_name,
        "match_type": match_type,
        "player1_name": player1_name,
        "player2_name": player2_name,
        "seed": seed,
        "status": "ENTERED",
        "raw_text": text,
    }
