"""Parse raw API response items into tournament + event dicts for DB upsert."""
from __future__ import annotations

import logging
import re
from typing import Optional

logger = logging.getLogger(__name__)

# Regex to extract GUID from tournament URL path
_GUID_RE = re.compile(r"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", re.I)


def parse_tournament_results(
    api_items: list[dict],
    section_name: str | None = None,
) -> list[dict]:
    """Parse a list of API searchResult items.

    Returns list of {"tournament": {...}, "events": [...]}.
    """
    parsed = []
    for item in api_items:
        try:
            result = _parse_single(item, section_name)
            if result:
                parsed.append(result)
        except Exception as e:
            code = item.get("item", {}).get("identificationCode", "?")
            logger.warning("Failed to parse tournament %s: %s", code, e)
    return parsed


def _parse_single(item: dict, section_name: str | None) -> Optional[dict]:
    """Parse one API result item into tournament + events."""
    data = item.get("item", {})
    if not data:
        return None

    # Extract tournament GUID from URL
    url_path = data.get("url", "")
    full_url = f"https://playtennis.usta.com{url_path}" if url_path else ""
    tournament_id = _extract_guid(url_path) or data.get("identificationCode", "")

    # Location
    primary_loc = data.get("primaryLocation", {})
    location_info = data.get("location", {})
    geo = location_info.get("geo", {})

    # Dates — strip time part
    start_date = _date_only(data.get("startDateTime", ""))
    end_date = _date_only(data.get("endDateTime", ""))

    # Entry deadline
    reg = data.get("registrationRestrictions", {})
    entry_deadline = _date_only(reg.get("entriesCloseDateTime", ""))

    # Level
    level_obj = data.get("level", {})
    level_name = level_obj.get("name", "")

    # Category (level categories)
    level_cats = data.get("levelCategories", [])
    category = level_cats[0].get("name", "") if level_cats else ""

    # Organization
    org = data.get("organization", {})

    # Extract org slug for URL construction
    # ClubSpark API provides websiteUrl (e.g. "/orgname") or slug field
    org_slug = _extract_org_slug(org)

    # Status
    status = "cancelled" if data.get("isCancelled") else "active"

    # Determine accepting_entries from entry deadline
    accepting = False
    if entry_deadline:
        from datetime import date
        try:
            deadline = date.fromisoformat(entry_deadline)
            accepting = deadline >= date.today()
        except ValueError:
            pass

    # Parse events
    raw_events = data.get("events", [])
    events = []
    for e in raw_events:
        event = _parse_event(e)
        if event:
            events.append(event)

    tournament = {
        "tournament_id": tournament_id,
        "code": data.get("identificationCode", ""),
        "name": data.get("name", ""),
        "level": level_name,
        "category": category,
        "start_date": start_date or None,
        "end_date": end_date or None,
        "entry_deadline": entry_deadline or None,
        "accepting_entries": accepting,
        "venue_name": location_info.get("name", ""),
        "city": primary_loc.get("town", ""),
        "state": primary_loc.get("county", ""),
        "section": section_name,
        "organization": org.get("name", ""),
        "org_slug": org_slug,
        "postcode": primary_loc.get("postcode", ""),
        "latitude": geo.get("latitude", 0.0),
        "longitude": geo.get("longitude", 0.0),
        "timezone": data.get("timeZone", ""),
        "status": status,
        "events_count": len(events),
        "surface": None,
        "url": full_url,
        "director_name": None,
        "director_email": None,
        "director_phone": None,
        "total_draws": None,
    }

    return {"tournament": tournament, "events": events}


def _parse_event(e: dict) -> Optional[dict]:
    """Parse a single event from the API response."""
    division = e.get("division", {})
    age_cat = division.get("ageCategory", {})
    pricing = e.get("pricing", {})
    entry_fee_obj = pricing.get("entryFee", {})

    amount = entry_fee_obj.get("amount", 0)
    # API returns fee in cents
    entry_fee = amount / 100 if amount else 0

    return {
        "event_id": e.get("id", ""),
        "gender": division.get("gender", ""),
        "event_type": division.get("eventType", ""),
        "age_category": age_cat.get("todsCode", ""),
        "min_age": age_cat.get("minimumAge"),
        "max_age": age_cat.get("maximumAge"),
        "surface": e.get("surface", ""),
        "court_location": e.get("courtLocation", ""),
        "entry_fee": entry_fee,
        "currency": entry_fee_obj.get("currency", "USD"),
        "level": e.get("level", {}).get("name", ""),
        "ball_color": e.get("ballColour", ""),
    }


def _extract_guid(url_path: str) -> str:
    """Extract GUID from a URL path like /Tournaments/Overview/be6eb6ea-..."""
    match = _GUID_RE.search(url_path)
    return match.group(0) if match else ""


def _date_only(dt_str: str) -> str:
    """Extract YYYY-MM-DD from an ISO datetime string."""
    if not dt_str:
        return ""
    return dt_str.split("T")[0]


def _extract_org_slug(org: dict) -> str:
    """Extract the organization URL slug from the API response.

    The ClubSpark API may provide the slug in different fields.
    Tries: websiteUrl, slug, then derives from name as fallback.
    """
    # Try websiteUrl first (e.g. "/orgname" or "orgname")
    website_url = org.get("websiteUrl", "") or ""
    if website_url:
        return website_url.strip("/").split("/")[-1]

    # Try slug field directly
    slug = org.get("slug", "") or ""
    if slug:
        return slug

    # Derive from name as fallback: lowercase, remove non-alnum
    name = org.get("name", "") or ""
    if name:
        return re.sub(r"[^a-z0-9]", "", name.lower())

    return ""
