"""Parse ranking data from USTA API JSON responses."""
from __future__ import annotations

import logging
from datetime import datetime, timezone

logger = logging.getLogger(__name__)


def parse_from_api(rankings_response: dict) -> list[dict]:
    """
    Extract ranking entries from the playerRankings API response.
    Returns a list of dicts matching the rankings table columns.

    API response structure:
    {
        "player": {
            "uaid": "...",
            "rankings": [ { ... }, ... ]
        }
    }
    """
    rankings = []

    player_data = rankings_response.get('player', {})
    ranking_list = player_data.get('rankings', [])

    for entry in ranking_list:
        ranking = _parse_ranking_entry(entry)
        if ranking:
            rankings.append(ranking)

    logger.info("Parsed %d ranking entries from API", len(rankings))
    return rankings


def _parse_ranking_entry(entry: dict) -> dict | None:
    """Parse a single ranking entry from the API JSON."""
    catalog_id = entry.get('catalogId')
    if not catalog_id:
        return None

    rank = entry.get('rank', {})
    record = entry.get('record', {})
    points_record = entry.get('pointsRecord', {})

    # Normalize "NULL" strings to None
    match_format = entry.get('matchFormat')
    if match_format == 'NULL':
        match_format = None

    match_format_type = entry.get('matchFormatType')
    if match_format_type == 'NULL':
        match_format_type = None

    return {
        "catalog_id": catalog_id,
        "display_label": entry.get('displayLabel'),
        "player_type": entry.get('playerType'),
        "age_restriction": entry.get('ageRestriction'),
        "age_restriction_modifier": entry.get('ageRestrictionModifier'),
        "rank_list_gender": entry.get('rankListGender'),
        "list_type": entry.get('listType'),
        "match_format": match_format,
        "match_format_type": match_format_type,
        "family_category": entry.get('familyCategory'),
        "national_rank": rank.get('national'),
        "section_rank": rank.get('section'),
        "district_rank": rank.get('district'),
        "points": entry.get('points'),
        "singles_points": points_record.get('singlesPoints'),
        "doubles_points": points_record.get('doublesPoints'),
        "bonus_points": points_record.get('bonusPoints'),
        "wins": record.get('win'),
        "losses": record.get('loss'),
        "trend_direction": entry.get('trendDirection'),
        "publish_date": _parse_datetime(entry.get('publishDate')),
        "section": entry.get('section'),
        "district": entry.get('district'),
        "state": entry.get('state'),
    }


def _parse_datetime(value: str | None) -> datetime | None:
    """Parse ISO 8601 datetime string (e.g. '2026-04-02T17:08:33.463Z') to Python datetime."""
    if not value:
        return None
    try:
        # Handle 'Z' suffix and milliseconds
        return datetime.fromisoformat(value.replace('Z', '+00:00'))
    except (ValueError, AttributeError):
        logger.warning("Could not parse datetime: %s", value)
        return None
