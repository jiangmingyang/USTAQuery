"""Parse rankings list data from the USTA public rankings search API."""
from __future__ import annotations

import logging
from datetime import date

logger = logging.getLogger(__name__)


def parse_rankings_response(
    response: dict, age_group: str, gender: str
) -> tuple[list[dict], str, int]:
    """
    Parse a response from /rankings/search/public.

    Returns (entries, catalog_id, total_pages) where each entry is
    {"player": {...}, "ranking": {...}}.

    API response structure::

        {
            "data": [ { "name": "...", "uaid": "...", ... }, ... ],
            "pagination": { "totalPages": N, "currentPage": 1, "pageSize": 20 },
            "catalogId": "JUNIOR_NULL_M_STANDING_Y12_UNDER_NULL_NULL_NULL"
        }
    """
    catalog_id = response.get("catalogId", "")
    pagination = response.get("pagination", {})
    total_pages = pagination.get("totalPages", 1)

    entries: list[dict] = []
    for player_data in response.get("data", []):
        player = _parse_player_basic(player_data, gender)
        ranking = _parse_ranking_entry(player_data, catalog_id, age_group, gender)
        if player and player.get("uaid"):
            entries.append({"player": player, "ranking": ranking})

    logger.info(
        "Parsed %d players from rankings page (catalog=%s, page %d/%d)",
        len(entries),
        catalog_id,
        pagination.get("currentPage", 1),
        total_pages,
    )
    return entries, catalog_id, total_pages


def _parse_player_basic(player_data: dict, gender: str) -> dict:
    """Extract basic player info from a rankings list entry."""
    name = player_data.get("name", "")
    parts = name.split(None, 1)
    first_name = parts[0] if parts else name
    last_name = parts[1] if len(parts) > 1 else ""

    section_info = player_data.get("section") or {}
    district_info = player_data.get("district") or {}

    return {
        "uaid": player_data.get("uaid", ""),
        "first_name": first_name,
        "last_name": last_name,
        "gender": gender,
        "city": player_data.get("city"),
        "state": player_data.get("state"),
        "section": section_info.get("name") if isinstance(section_info, dict) else None,
        "district": district_info.get("name") if isinstance(district_info, dict) else None,
    }


def _parse_ranking_entry(
    player_data: dict, catalog_id: str, age_group: str, gender: str
) -> dict:
    """Extract ranking info from a rankings list entry."""
    rank = player_data.get("rank") or {}
    points_record = player_data.get("pointsRecord") or {}

    return {
        "catalog_id": catalog_id,
        "player_type": "JUNIOR",
        "age_restriction": age_group,
        "age_restriction_modifier": "UNDER",
        "rank_list_gender": gender,
        "list_type": _extract_list_type(catalog_id),
        "match_format": _extract_field(catalog_id, 6),
        "match_format_type": _extract_field(catalog_id, 7),
        "family_category": _extract_field(catalog_id, 8),
        "national_rank": rank.get("national"),
        "section_rank": rank.get("section"),
        "district_rank": rank.get("district"),
        "points": player_data.get("points"),
        "singles_points": points_record.get("singlesPoints"),
        "doubles_points": points_record.get("doublesPoints"),
        "bonus_points": points_record.get("bonusPoints"),
        "trend_direction": player_data.get("trend"),
        "publish_date": date.today(),
        "section": (player_data.get("section") or {}).get("name")
        if isinstance(player_data.get("section"), dict)
        else None,
        "district": (player_data.get("district") or {}).get("name")
        if isinstance(player_data.get("district"), dict)
        else None,
        "state": player_data.get("state"),
    }


# Catalog ID format: JUNIOR_NULL_{gender}_{listType}_{age}_UNDER_{matchFormat}_{matchFormatType}_{familyCategory}
# e.g. JUNIOR_NULL_F_STANDING_Y14_UNDER_NULL_NULL_NULL
#      JUNIOR_NULL_M_BONUS_POINTS_Y12_UNDER_NULL_NULL_NULL
#      JUNIOR_NULL_F_SEEDING_Y16_UNDER_DOUBLES_INDIVIDUAL_NULL
#      JUNIOR_NULL_F_QUOTA_Y18_UNDER_NULL_NULL_S05
_LIST_TYPE_MAP = {
    "STANDING": "STANDING",
    "SEEDING": "SEEDING",
    "QUOTA": "QUOTA",
    "BONUS_POINTS": "BONUS_POINTS",
    "YEAR_END": "YEAR_END",
}


def _extract_list_type(catalog_id: str) -> str:
    """Extract list_type from a catalog ID string."""
    for key in _LIST_TYPE_MAP:
        if f"_{key}_" in catalog_id:
            return _LIST_TYPE_MAP[key]
    return "STANDING"


def _extract_field(catalog_id: str, index: int) -> str | None:
    """Extract a trailing field from catalog ID by splitting on '_UNDER_'.

    After '_UNDER_', the remaining fields are:
      index 6 = matchFormat, 7 = matchFormatType, 8 = familyCategory
    """
    parts = catalog_id.split("_UNDER_", 1)
    if len(parts) < 2:
        return None
    suffix_parts = parts[1].split("_")
    # index 6 -> suffix_parts[0], 7 -> [1], 8 -> [2]
    pos = index - 6
    if 0 <= pos < len(suffix_parts):
        val = suffix_parts[pos]
        return None if val == "NULL" else val
    return None
