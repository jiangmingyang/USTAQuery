#!/usr/bin/env python3
"""Scrape USTA junior rankings via the public JSON API.

Fetches all combinations of gender (Boys/Girls) and age group (12/14/16/18)
from the USTA rankings API, paginating at 100 results per page.

Usage:
    python scrape_usta_rankings.py                  # scrape all combos
    python scrape_usta_rankings.py --gender M       # boys only
    python scrape_usta_rankings.py --age 14         # 14U only
    python scrape_usta_rankings.py --output results # custom output dir
    python scrape_usta_rankings.py --max-pages 2    # limit pages (for testing)
"""
from __future__ import annotations

import argparse
import json
import logging
import os
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

API_URL = "https://services.usta.com/v1/dataexchange/rankings/search/public"

GENDERS = ["M", "F"]
AGE_GROUPS = ["Y12", "Y14", "Y16", "Y18"]

GENDER_LABELS = {"M": "Boys", "F": "Girls"}

PAGE_SIZE = 100
REQUEST_TIMEOUT = 30  # seconds
PAGE_DELAY = 0.5  # seconds between paginated requests
GROUP_DELAY = 1.0  # seconds between different age/gender combos
MAX_RETRIES = 3
RETRY_DELAY = 2.0  # seconds before retry

DEFAULT_OUTPUT_DIR = "usta_rankings_output"

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# API interaction
# ---------------------------------------------------------------------------


def build_request_body(
    gender: str,
    age_group: str,
    page: int = 1,
    page_size: int = PAGE_SIZE,
) -> dict:
    """Build the POST body for the rankings API."""
    return {
        "selection": {
            "rankListGender": gender,
            "ageRestriction": age_group,
            "ageRestrictionModifier": "UNDER",
            "playerType": "JUNIOR",
            "listType": "STANDING",
        },
        "pagination": {
            "pageSize": page_size,
            "currentPage": page,
        },
    }


def fetch_rankings_page(
    gender: str,
    age_group: str,
    page: int = 1,
    page_size: int = PAGE_SIZE,
    timeout: int = REQUEST_TIMEOUT,
) -> dict:
    """Fetch a single page of rankings from the API.

    Returns the parsed JSON response dict.
    Raises on HTTP or network errors after retries.
    """
    body = build_request_body(gender, age_group, page, page_size)
    data = json.dumps(body).encode("utf-8")

    req = urllib.request.Request(API_URL, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")

    last_exc: Exception | None = None
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except (urllib.error.URLError, urllib.error.HTTPError, OSError) as exc:
            last_exc = exc
            logger.warning(
                "Attempt %d/%d failed for %s %s page %d: %s",
                attempt, MAX_RETRIES, gender, age_group, page, exc,
            )
            if attempt < MAX_RETRIES:
                time.sleep(RETRY_DELAY * attempt)

    raise RuntimeError(
        f"Failed to fetch {gender} {age_group} page {page} after {MAX_RETRIES} retries"
    ) from last_exc


def fetch_all_rankings(
    gender: str,
    age_group: str,
    max_pages: int | None = None,
    page_size: int = PAGE_SIZE,
) -> dict:
    """Fetch all pages of rankings for a given gender/age combination.

    Returns a dict with:
        - metadata: catalogId, displayLabel, publishDate, pagination summary
        - players: list of all player entries
    """
    all_players: list[dict] = []
    total_pages: int | None = None
    metadata: dict = {}

    page_num = 1
    while True:
        if max_pages is not None and page_num > max_pages:
            break
        if total_pages is not None and page_num > total_pages:
            break

        logger.info(
            "Fetching %s %s page %d%s ...",
            GENDER_LABELS.get(gender, gender),
            age_group,
            page_num,
            f"/{total_pages}" if total_pages else "",
        )

        resp = fetch_rankings_page(gender, age_group, page_num, page_size)
        players = resp.get("data", [])

        if not players:
            if page_num == 1:
                logger.warning("No data returned for %s %s", gender, age_group)
            break

        all_players.extend(players)

        pagination = resp.get("pagination", {})
        if total_pages is None:
            total_pages = pagination.get("totalPages", 1)
            metadata = {
                "catalogId": resp.get("catalogId"),
                "displayLabel": resp.get("displayLabel"),
                "publishDate": resp.get("publishDate"),
                "listType": resp.get("listType"),
                "playerType": resp.get("playerType"),
                "rankListGender": resp.get("rankListGender"),
                "ageRestriction": resp.get("ageRestriction"),
                "ageRestrictionModifier": resp.get("ageRestrictionModifier"),
                "matchFormat": resp.get("matchFormat"),
                "matchFormatType": resp.get("matchFormatType"),
                "familyCategory": resp.get("familyCategory"),
                "totalPages": total_pages,
                "totalResults": pagination.get("totalResults"),
                "pageSize": pagination.get("pageSize"),
            }
            logger.info(
                "  -> %s: %d total results, %d pages",
                metadata["displayLabel"],
                metadata.get("totalResults", 0),
                total_pages,
            )

        page_num += 1
        if total_pages is not None and page_num <= total_pages:
            time.sleep(PAGE_DELAY)

    logger.info(
        "Completed %s %s: %d players fetched",
        GENDER_LABELS.get(gender, gender), age_group, len(all_players),
    )

    return {
        "metadata": metadata,
        "players": all_players,
    }


# ---------------------------------------------------------------------------
# Orchestration
# ---------------------------------------------------------------------------


def scrape_all_rankings(
    genders: list[str] | None = None,
    age_groups: list[str] | None = None,
    max_pages: int | None = None,
    output_dir: str = DEFAULT_OUTPUT_DIR,
) -> dict[str, str]:
    """Scrape rankings for all requested gender/age combinations.

    Returns a dict mapping combo labels to output file paths.
    """
    genders = genders or GENDERS
    age_groups = age_groups or AGE_GROUPS
    out_path = Path(output_dir)
    out_path.mkdir(parents=True, exist_ok=True)

    results: dict[str, str] = {}
    combos = [(g, a) for g in genders for a in age_groups]

    scrape_timestamp = datetime.now(timezone.utc).isoformat()

    for idx, (gender, age_group) in enumerate(combos):
        label = f"{GENDER_LABELS.get(gender, gender)}_{age_group}"
        logger.info(
            "=== Scraping %s (%d/%d) ===", label, idx + 1, len(combos),
        )

        try:
            data = fetch_all_rankings(gender, age_group, max_pages=max_pages)
            data["scrape_timestamp"] = scrape_timestamp

            filename = f"rankings_{gender}_{age_group}.json"
            filepath = out_path / filename
            with open(filepath, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False, default=str)

            results[label] = str(filepath)
            logger.info("Saved %s -> %s (%d players)", label, filepath, len(data["players"]))

        except Exception:
            logger.exception("Failed to scrape %s", label)
            results[label] = "ERROR"

        if idx < len(combos) - 1:
            time.sleep(GROUP_DELAY)

    # Write a summary index file
    summary = {
        "scrape_timestamp": scrape_timestamp,
        "combinations": [],
    }
    for label, path in results.items():
        if path != "ERROR" and Path(path).exists():
            with open(path, "r", encoding="utf-8") as f:
                file_data = json.load(f)
            summary["combinations"].append({
                "label": label,
                "file": path,
                "player_count": len(file_data.get("players", [])),
                "metadata": file_data.get("metadata", {}),
            })
        else:
            summary["combinations"].append({
                "label": label,
                "file": path,
                "player_count": 0,
                "error": True,
            })

    summary_path = out_path / "rankings_summary.json"
    with open(summary_path, "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2, ensure_ascii=False, default=str)
    logger.info("Summary written to %s", summary_path)

    return results


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Scrape USTA junior rankings via public API",
    )
    parser.add_argument(
        "--gender", "-g",
        choices=["M", "F"],
        action="append",
        help="Gender to scrape (M=Boys, F=Girls). Can be specified multiple times. Default: both.",
    )
    parser.add_argument(
        "--age", "-a",
        choices=["12", "14", "16", "18"],
        action="append",
        help="Age group to scrape (12/14/16/18). Can be specified multiple times. Default: all.",
    )
    parser.add_argument(
        "--output", "-o",
        default=DEFAULT_OUTPUT_DIR,
        help=f"Output directory for JSON files (default: {DEFAULT_OUTPUT_DIR})",
    )
    parser.add_argument(
        "--max-pages",
        type=int,
        default=None,
        help="Max pages to fetch per combination (for testing)",
    )
    parser.add_argument(
        "--verbose", "-v",
        action="store_true",
        help="Enable debug logging",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> None:
    args = parse_args(argv)

    logging.basicConfig(
        level=logging.DEBUG if args.verbose else logging.INFO,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    )

    genders = args.gender or None
    age_groups = [f"Y{a}" for a in args.age] if args.age else None

    results = scrape_all_rankings(
        genders=genders,
        age_groups=age_groups,
        max_pages=args.max_pages,
        output_dir=args.output,
    )

    # Print summary
    print("\n=== Scrape Complete ===")
    for label, path in results.items():
        if path == "ERROR":
            print(f"  {label}: FAILED")
        else:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)
            print(f"  {label}: {len(data['players'])} players -> {path}")


if __name__ == "__main__":
    main()
