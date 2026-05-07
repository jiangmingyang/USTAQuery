"""Parse player profile data from USTA API JSON responses."""
from __future__ import annotations

import logging
from datetime import date
from decimal import Decimal, InvalidOperation

logger = logging.getLogger(__name__)


def parse_from_api(player_info_response: dict, uaid: str) -> dict:
    """
    Extract player data from the playerInfo API response.
    Returns a dict matching the players table columns.
    """
    data = {"uaid": uaid}

    # playerInfo response: { "data": [ { ... } ] }
    info_list = player_info_response.get('data', [])
    if not info_list:
        logger.warning("No playerInfo data found for uaid=%s", uaid)
        data["first_name"] = ""
        data["last_name"] = ""
        data["gender"] = "M"
        return data

    info = info_list[0]

    # Name
    full_name = info.get('name', '')
    parts = full_name.split(None, 1)
    data["first_name"] = parts[0] if parts else full_name
    data["last_name"] = parts[1] if len(parts) > 1 else ""

    # Gender — API returns "MALE" or "FEMALE"
    gender_raw = info.get('gender', '')
    data["gender"] = 'M' if gender_raw.upper() == 'MALE' else 'F' if gender_raw.upper() == 'FEMALE' else 'M'

    # Location
    data["city"] = info.get('city')
    data["state"] = info.get('state')

    # Section & District — nested objects with code and name
    section_obj = info.get('section') or {}
    data["section"] = section_obj.get('name')
    data["section_code"] = section_obj.get('code')

    district_obj = info.get('district') or {}
    data["district"] = district_obj.get('name')
    data["district_code"] = district_obj.get('code')

    # Nationality, ITF ID, age category, wheelchair
    data["nationality"] = info.get('nationality', 'USA')
    data["itf_tennis_id"] = info.get('itfTennisId')
    data["age_category"] = info.get('ageCategory')
    data["wheelchair"] = info.get('wheelchair', False)

    # WTN ratings — nested under ratings.wtn array
    ratings = info.get('ratings', {})
    wtn_list = ratings.get('wtn', [])
    for wtn in wtn_list:
        wtn_type = wtn.get('type', '')
        if wtn_type == 'SINGLE':
            data["wtn_singles"] = _to_decimal(wtn.get('tennisNumber'))
            data["wtn_singles_confidence"] = wtn.get('confidence')
            data["wtn_singles_last_played"] = wtn.get('lastPlayed')
            data["wtn_singles_game_zone_upper"] = _to_decimal(wtn.get('gameZoneUpper'))
            data["wtn_singles_game_zone_lower"] = _to_decimal(wtn.get('gameZoneLower'))
        elif wtn_type == 'DOUBLE':
            data["wtn_doubles"] = _to_decimal(wtn.get('tennisNumber'))
            data["wtn_doubles_confidence"] = wtn.get('confidence')
            data["wtn_doubles_last_played"] = wtn.get('lastPlayed')
            data["wtn_doubles_game_zone_upper"] = _to_decimal(wtn.get('gameZoneUpper'))
            data["wtn_doubles_game_zone_lower"] = _to_decimal(wtn.get('gameZoneLower'))

    # NTRP rating
    ntrp = ratings.get('ntrp', {})
    if isinstance(ntrp, dict) and ntrp:
        # ntrp may have a rating value in various structures
        data["rating_ntrp"] = ntrp.get('ratingValue') or ntrp.get('rating')
    else:
        data["rating_ntrp"] = None

    # Fields not in API — set as None
    data.setdefault("utr_id", None)
    data.setdefault("utr_singles", None)
    data.setdefault("utr_doubles", None)
    data.setdefault("profile_image_url", None)
    data.setdefault("membership_type", None)
    data.setdefault("membership_expiry", None)

    return data


def _to_decimal(value) -> str | None:
    """Convert a numeric value to a string suitable for Decimal DB column."""
    if value is None:
        return None
    try:
        return str(Decimal(str(value)))
    except (InvalidOperation, ValueError):
        return None
