"""Scrape USTA rankings list pages via API response interception."""
from __future__ import annotations

import json
import logging
import time

from playwright.sync_api import Page, Request

import config
from parsers.rankings_list_parser import parse_rankings_response

logger = logging.getLogger(__name__)

JUNIOR_LIST_TYPES = [
    "combined",
    "seeding",
    "doubles",
    "bonusPoints",
    "quota",
    "combinedYearEnd",
    "doublesYearEnd",
]


def scrape_rankings_list(
    page: Page,
    age_group: str,
    gender: str,
    junior_list_type: str = "combined",
    max_pages: int | None = None,
) -> list[dict]:
    """
    Scrape a rankings list for a single age/gender/listType combo.

    Strategy:
      1. Navigate to the rankings page so the browser makes its own API call.
      2. Intercept the request body to learn the full selection structure
         (including extra fields like matchFormat that vary by list type).
      3. Override rankListGender + ageRestriction with our desired values.
      4. Use this corrected selection for ALL pages (including page 1)
         via page.evaluate(fetch()).

    Returns a list of {"player": {...}, "ranking": {...}} dicts.
    """
    captured_request_body: dict | None = None

    def handle_request(request: Request):
        nonlocal captured_request_body
        if (
            "/rankings/search/public" in request.url
            and request.method == "POST"
            and captured_request_body is None
        ):
            try:
                captured_request_body = json.loads(request.post_data)
                logger.debug("Captured browser request body: %s", request.post_data[:400])
            except Exception:
                pass

    page.on("request", handle_request)

    # Navigate to rankings page — the hash params determine which list type
    # the browser loads, giving us the correct selection template
    url = config.USTA_RANKINGS_URL.format(
        age=age_group, gender=gender, list_type=junior_list_type
    )
    logger.info("Navigating to rankings: %s %s %s", age_group, gender, junior_list_type)
    page.goto(url, wait_until="domcontentloaded", timeout=config.TIMEOUT)

    # Wait for the browser to make its first API call
    page.wait_for_timeout(5000)
    max_wait = 15
    for i in range(max_wait):
        if captured_request_body is not None:
            break
        time.sleep(1)

    if not captured_request_body:
        logger.warning(
            "Could not capture browser request for %s %s %s",
            age_group, gender, junior_list_type,
        )
        return []

    # Build our selection by taking the browser's template and overriding
    # gender + age to ensure correctness (hash params aren't always reliable)
    selection = captured_request_body.get("selection", {})
    selection["rankListGender"] = gender
    selection["ageRestriction"] = age_group
    logger.info("Using selection: %s", json.dumps(selection))

    # Fetch ALL pages ourselves with the corrected selection
    all_entries: list[dict] = []
    total_pages: int | None = None

    page_num = 1
    while True:
        if max_pages is not None and page_num > max_pages:
            break
        if total_pages is not None and page_num > total_pages:
            break

        page_data = _fetch_rankings_page(page, selection, page_num)
        if not page_data or not page_data.get("data"):
            if page_num == 1:
                logger.warning(
                    "No data on page 1 for %s %s %s", age_group, gender, junior_list_type
                )
            else:
                logger.warning("  Page %d/%s: no data — stopping", page_num, total_pages)
            break

        entries, catalog_id, resp_total_pages = parse_rankings_response(
            page_data, age_group, gender
        )
        all_entries.extend(entries)

        # Set total_pages from the first response
        if total_pages is None:
            total_pages = resp_total_pages
            if max_pages is not None:
                total_pages = min(total_pages, max_pages)
            logger.info(
                "  Page 1: %d players, %d total pages (catalog=%s)",
                len(entries), total_pages, catalog_id,
            )
        else:
            logger.info("  Page %d/%d: %d players", page_num, total_pages, len(entries))

        page_num += 1
        if total_pages is not None and page_num <= total_pages:
            time.sleep(config.RANKINGS_PAGE_DELAY)

    logger.info(
        "Total: %d players for %s %s %s",
        len(all_entries), age_group, gender, junior_list_type,
    )
    return all_entries


def _fetch_rankings_page(
    page: Page,
    selection: dict,
    page_num: int,
) -> dict | None:
    """Fetch a single rankings page via page.evaluate(fetch()).

    Uses the exact selection dict (captured from the browser's request and
    corrected for gender/age), only varying the pagination page number.
    """
    api_url = config.RANKINGS_API_URL

    body = {
        "selection": selection,
        "pagination": {
            "pageSize": 100,
            "currentPage": page_num,
        },
    }

    body_json = json.dumps(body)

    try:
        result = page.evaluate(f"""
            async () => {{
                try {{
                    const res = await fetch('{api_url}', {{
                        method: 'POST',
                        headers: {{
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        }},
                        body: JSON.stringify({body_json})
                    }});
                    if (!res.ok) {{
                        return {{error: 'HTTP ' + res.status, status: res.status}};
                    }}
                    return await res.json();
                }} catch (e) {{
                    return {{error: e.message}};
                }}
            }}
        """)

        if result is None:
            logger.warning("Page %d: evaluate returned None", page_num)
            return None

        if isinstance(result, dict) and "error" in result:
            logger.warning("API error on page %d: %s", page_num, result)
            return None

        return result

    except Exception as e:
        logger.warning("Failed to fetch rankings page %d: %s", page_num, e)
        return None
