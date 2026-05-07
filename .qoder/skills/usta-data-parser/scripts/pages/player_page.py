"""Scrape a player's profile page on usta.com via API response interception."""
from __future__ import annotations

import logging
import re
import time

from playwright.sync_api import Page

import config
from parsers import player_parser, ranking_parser

logger = logging.getLogger(__name__)


def scrape_player(page: Page, uaid: str) -> dict:
    """
    Navigate to player profile and extract data from intercepted API responses.
    Returns a dict with keys: player, rankings.
    """
    result = {"player": {}, "rankings": []}

    # Set up API response interception
    api_responses = {}

    def handle_response(response):
        url = response.url
        if '/usta/api?' in url:
            try:
                body = response.json()
                api_type = re.search(r'type=([^&]+)', url)
                if api_type:
                    api_responses[api_type.group(1)] = body
            except Exception:
                pass

    page.on('response', handle_response)

    # Navigate to the player profile page (about tab loads all API data)
    profile_url = config.USTA_PLAYER_PROFILE_URL.format(uaid=uaid, tab="about")
    logger.info("Scraping player profile: %s  url=%s", uaid, profile_url)

    page.goto(profile_url, wait_until="domcontentloaded", timeout=config.TIMEOUT)

    # Wait for page SPA to hydrate and trigger API requests
    time.sleep(5)

    # Try waiting for a key element as a signal the page is loaded
    try:
        page.wait_for_selector('h1, .player-name', timeout=10000)
    except Exception:
        pass

    # Extra wait for API responses to complete
    time.sleep(3)

    # If no API data captured, wait a bit more
    if not api_responses:
        logger.warning("No API data captured yet, waiting longer...")
        time.sleep(5)

    logger.info("Captured API response types: %s", list(api_responses.keys()))

    # Parse player info from API response
    player_info_data = api_responses.get('playerInfo', {})
    result["player"] = player_parser.parse_from_api(player_info_data, uaid)
    logger.info(
        "Parsed player info: %s %s",
        result["player"].get("first_name"),
        result["player"].get("last_name"),
    )

    # Parse rankings from API response
    rankings_data = api_responses.get('playerRankings', {})
    result["rankings"] = ranking_parser.parse_from_api(rankings_data)
    logger.info("Parsed %d ranking entries", len(result["rankings"]))

    return result
