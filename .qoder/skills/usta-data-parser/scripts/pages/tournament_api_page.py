"""Fetch tournament listings from the USTA unified-search API."""
from __future__ import annotations

import json
import logging
import time
from typing import Optional

from playwright.sync_api import Page

import config

logger = logging.getLogger(__name__)

API_URL = config.USTA_TOURNAMENT_API_URL
PAGE_SIZE = config.TOURNAMENT_API_PAGE_SIZE


def scrape_tournament_api(
    page: Page,
    org_group: str | None,
    level_category: str = "junior",
    date_from: str | None = None,
    date_to: str | None = None,
    max_pages: int | None = None,
) -> list[dict]:
    """Fetch all tournaments for an org-group in a date range via API.

    Returns raw API result items (list of dicts from searchResults).
    """
    from datetime import datetime, timedelta

    if not date_from:
        date_from = datetime.now().strftime("%Y-%m-%d")
    if not date_to:
        date_to = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")

    # Navigate to establish browser context (cookies/session)
    logger.info("Initializing browser context for tournament API...")
    page.goto("https://playtennis.usta.com/tournaments", timeout=60000)
    page.wait_for_timeout(3000)

    all_items: list[dict] = []
    from_offset = 0
    page_num = 1
    total = None

    while True:
        logger.info("Fetching page %d (offset=%d)...", page_num, from_offset)

        body = _build_request_body(org_group, level_category, date_from, date_to, from_offset)
        response = _fetch_page(page, body)

        if not response:
            logger.warning("Empty response on page %d, stopping", page_num)
            break

        if "error" in response:
            logger.error("API error: %s", response["error"])
            break

        if total is None:
            total = response.get("total", 0)
            logger.info("Total tournaments: %d", total)
            if total == 0:
                break

        results = response.get("searchResults", [])
        if not results:
            logger.info("No more results on page %d", page_num)
            break

        all_items.extend(results)
        logger.info("  Got %d items, cumulative %d", len(results), len(all_items))

        if len(results) < PAGE_SIZE:
            break

        if max_pages and page_num >= max_pages:
            logger.info("Reached max pages limit (%d)", max_pages)
            break

        from_offset += PAGE_SIZE
        page_num += 1
        time.sleep(config.TOURNAMENT_API_DELAY)

    logger.info("Total fetched: %d tournament items", len(all_items))
    return all_items


def fetch_sections(page: Page) -> list[dict]:
    """Fetch all USTA sections from the filters JSON endpoint.

    Returns list of {name, value, districts: [{name, value}]}.
    """
    logger.info("Fetching USTA sections list...")

    page.goto("https://playtennis.usta.com/tournaments", timeout=60000)
    page.wait_for_timeout(2000)

    url = config.USTA_TOURNAMENT_FILTERS_URL
    result = page.evaluate(f"""
        async () => {{
            try {{
                const res = await fetch('{url}');
                const text = await res.text();
                // Handle UTF-8 BOM
                const clean = text.charCodeAt(0) === 0xFEFF ? text.substring(1) : text;
                return JSON.parse(clean);
            }} catch (e) {{
                return {{error: e.message}};
            }}
        }}
    """)

    if not result or "error" in result:
        logger.error("Failed to fetch sections: %s", result)
        return []

    sections = []
    primary = result.get("filters", {}).get("primary", [])
    for filter_item in primary:
        if filter_item.get("key") == "organisation-group":
            for item in filter_item.get("items", []):
                section = {
                    "name": item.get("label"),
                    "value": item.get("value"),
                    "districts": [],
                }
                for sub in item.get("items", []):
                    section["districts"].append({
                        "name": sub.get("label"),
                        "value": sub.get("value"),
                    })
                sections.append(section)
            break

    logger.info("Found %d sections", len(sections))
    return sections


def _build_request_body(
    org_group: str | None,
    level_category: str,
    date_from: str,
    date_to: str,
    from_offset: int = 0,
) -> dict:
    """Build the POST body for the tournament search API."""
    return {
        "options": {
            "size": PAGE_SIZE,
            "from": from_offset,
            "sortKey": "date",
            "latitude": 0,
            "longitude": 0,
        },
        "filters": [
            {"key": "organisation-id", "items": []},
            {"key": "location-id", "items": []},
            {"key": "region-id", "items": []},
            {"key": "publish-target", "items": [{"value": 1}]},
            {
                "key": "level-category",
                "items": [{"value": level_category}],
                "operator": "Or",
            },
            {
                "key": "organisation-group",
                "items": [{"value": org_group}] if org_group else [],
                "operator": "Or",
            },
            {
                "key": "date-range",
                "items": [{
                    "minDate": f"{date_from}T00:00:00.000Z",
                    "maxDate": f"{date_to}T23:59:59.999Z",
                }],
                "operator": "Or",
            },
            {"key": "distance", "items": [{"value": 100}], "operator": "Or"},
            {"key": "tournament-status", "items": [], "operator": "Or"},
            {"key": "tournament-level", "items": [], "operator": "Or"},
            {"key": "event-wtn-level", "items": [], "operator": "Or"},
            {"key": "event-division-age-range", "items": [], "operator": "Or"},
            {"key": "event-division-gender", "items": [], "operator": "Or"},
            {"key": "event-ntrp-rating-level", "items": [], "operator": "Or"},
            {"key": "event-division-age-category", "items": [], "operator": "Or"},
            {"key": "event-division-event-type", "items": [], "operator": "Or"},
            {"key": "event-court-location", "items": [], "operator": "Or"},
            {"key": "event-surface", "items": [], "operator": "Or"},
        ],
    }


def _fetch_page(page: Page, body: dict) -> Optional[dict]:
    """Make a single API call via browser fetch."""
    api_url = f"{API_URL}?indexSchema=tournament"

    try:
        response = page.evaluate(f"""
            async () => {{
                try {{
                    const res = await fetch('{api_url}', {{
                        method: 'POST',
                        headers: {{
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        }},
                        body: JSON.stringify({json.dumps(body)})
                    }});
                    return await res.json();
                }} catch (e) {{
                    return {{error: e.message}};
                }}
            }}
        """)
        return response
    except Exception as e:
        logger.error("Fetch page failed: %s", e)
        return None
