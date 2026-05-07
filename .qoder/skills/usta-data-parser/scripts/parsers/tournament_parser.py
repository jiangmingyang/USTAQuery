"""Parse tournament overview DOM into structured dict."""
from __future__ import annotations

import logging
import re
from datetime import datetime

from playwright.sync_api import Page

logger = logging.getLogger(__name__)

# Map common level text to our L1-L7 enum
_LEVEL_MAP = {
    "level 1": "L1", "l1": "L1", "national": "L1",
    "level 2": "L2", "l2": "L2", "sectional": "L2",
    "level 3": "L3", "l3": "L3",
    "level 4": "L4", "l4": "L4",
    "level 5": "L5", "l5": "L5",
    "level 6": "L6", "l6": "L6",
    "level 7": "L7", "l7": "L7", "introductory": "L7",
}

_CATEGORY_MAP = {
    "open": "OPEN",
    "closed": "CLOSED",
}


def parse_tournament_overview(page: Page, tournament_guid: str) -> dict:
    """
    Extract tournament metadata from the overview page.
    Returns a dict matching the tournaments table columns.
    """
    data = {"tournament_id": tournament_guid}

    # ── Name ──────────────────────────────────────────────────────────
    try:
        name_el = page.locator(
            "h1, .tournament-name, [class*='tournamentName'], "
            "[class*='TournamentHeader'] h1, [class*='competitionName']"
        ).first
        data["name"] = name_el.inner_text().strip()
    except Exception:
        data["name"] = None

    body_text = ""
    try:
        body_text = page.locator("body").inner_text()
    except Exception:
        pass

    # ── Level ─────────────────────────────────────────────────────────
    data["level"] = _extract_level(body_text)

    # ── Category ──────────────────────────────────────────────────────
    data["category"] = _extract_category(body_text)

    # ── Dates ─────────────────────────────────────────────────────────
    dates = _extract_dates(body_text)
    data["start_date"] = dates.get("start_date")
    data["end_date"] = dates.get("end_date")
    data["entry_deadline"] = dates.get("entry_deadline")

    # ── Accepting entries ─────────────────────────────────────────────
    data["accepting_entries"] = _check_accepting_entries(body_text)

    # ── Venue / Location ──────────────────────────────────────────────
    data.update(_extract_venue(page, body_text))

    # ── Surface ───────────────────────────────────────────────────────
    surface_match = re.search(r"(?:Surface|Court)[:\s]*(Hard|Clay|Grass|Indoor|Carpet)", body_text, re.IGNORECASE)
    data["surface"] = surface_match.group(1).title() if surface_match else None

    # ── Director ──────────────────────────────────────────────────────
    data.update(_extract_director(body_text))

    # ── URL ────────────────────────────────────────────────────────────
    data["url"] = page.url

    # ── Total draws ───────────────────────────────────────────────────
    data["total_draws"] = _count_draws(page)

    return data


def _extract_level(text: str) -> str | None:
    for pattern, level in _LEVEL_MAP.items():
        if re.search(r"\b" + re.escape(pattern) + r"\b", text, re.IGNORECASE):
            return level
    # Try generic "Level X" or "LX"
    m = re.search(r"(?:Level\s*|L)([1-7])\b", text, re.IGNORECASE)
    return f"L{m.group(1)}" if m else None


def _extract_category(text: str) -> str | None:
    for pattern, cat in _CATEGORY_MAP.items():
        if re.search(r"\b" + re.escape(pattern) + r"\b", text, re.IGNORECASE):
            return cat
    return None


def _extract_dates(text: str) -> dict:
    """Extract start_date, end_date, entry_deadline from page text."""
    result = {"start_date": None, "end_date": None, "entry_deadline": None}

    # Common date patterns
    date_patterns = [
        r"(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})",
        r"(\w+\s+\d{1,2},?\s+\d{4})",
    ]

    # Try to find date range like "Jan 5 - Jan 8, 2025" or "01/05/2025 - 01/08/2025"
    range_match = re.search(
        r"(?:Date|Start|Tournament)[:\s]*(.+?)(?:\n|$)", text, re.IGNORECASE
    )
    if range_match:
        range_text = range_match.group(1)
        dates = re.findall(r"(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})", range_text)
        if len(dates) >= 2:
            result["start_date"] = _parse_date(dates[0])
            result["end_date"] = _parse_date(dates[1])
        elif len(dates) == 1:
            result["start_date"] = _parse_date(dates[0])

    # Entry deadline
    deadline_match = re.search(
        r"(?:Entry\s*Deadline|Registration\s*Deadline|Entries?\s*Close)[:\s]*"
        r"(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\w+\s+\d{1,2},?\s+\d{4})",
        text, re.IGNORECASE,
    )
    if deadline_match:
        result["entry_deadline"] = _parse_date(deadline_match.group(1))

    return result


def _parse_date(date_str: str) -> str | None:
    """Try to parse a date string into YYYY-MM-DD format."""
    formats = ["%m/%d/%Y", "%m-%d-%Y", "%m/%d/%y", "%B %d, %Y", "%B %d %Y", "%b %d, %Y", "%b %d %Y"]
    for fmt in formats:
        try:
            return datetime.strptime(date_str.strip(), fmt).strftime("%Y-%m-%d")
        except ValueError:
            continue
    return None


def _check_accepting_entries(text: str) -> int:
    """Check if the tournament is currently accepting entries."""
    if re.search(r"(?:accepting|registration\s+open|entries?\s+open)", text, re.IGNORECASE):
        return 1
    if re.search(r"(?:closed|registration\s+closed|entries?\s+closed|full)", text, re.IGNORECASE):
        return 0
    return 0


def _extract_venue(page: Page, text: str) -> dict:
    """Extract venue name, city, state."""
    result = {"venue_name": None, "city": None, "state": None}

    venue_match = re.search(
        r"(?:Venue|Location|Facility)[:\s]*(.+?)(?:\n|$)", text, re.IGNORECASE
    )
    if venue_match:
        result["venue_name"] = venue_match.group(1).strip()[:255]

    # City, State pattern
    loc_match = re.search(r"([A-Za-z\s]+),\s*([A-Z]{2})\b", text)
    if loc_match:
        result["city"] = loc_match.group(1).strip()
        result["state"] = loc_match.group(2)

    return result


def _extract_director(text: str) -> dict:
    """Extract tournament director info."""
    result = {"director_name": None, "director_email": None, "director_phone": None}

    name_match = re.search(
        r"(?:Tournament\s*Director|Director)[:\s]*([A-Za-z\s]+?)(?:\n|Email|Phone|\||$)",
        text, re.IGNORECASE,
    )
    if name_match:
        result["director_name"] = name_match.group(1).strip()[:200]

    email_match = re.search(r"[\w.+-]+@[\w.-]+\.\w+", text)
    if email_match:
        result["director_email"] = email_match.group(0)

    phone_match = re.search(r"\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}", text)
    if phone_match:
        result["director_phone"] = phone_match.group(0)

    return result


def _count_draws(page: Page) -> int | None:
    """Count the number of draw sections/tabs visible."""
    try:
        draws = page.locator(
            ".draw-tab, [class*='drawTab'], [class*='draw-section'], "
            "[role='tab']:has-text('Draw')"
        )
        count = draws.count()
        return count if count > 0 else None
    except Exception:
        return None
