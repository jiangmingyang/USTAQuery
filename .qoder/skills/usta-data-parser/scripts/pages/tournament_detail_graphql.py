"""Fetch tournament participant entries via dual GraphQL APIs.

Started/ended tournaments use the TournamentDesk API which provides richer data
(entryStage, entryStatus, entryPosition, drawId).

The Tournaments API (paginatedPublicTournamentRegistrations) works for all
tournament statuses (upcoming, started, completed).

Strategy: try TournamentDesk API first; if it returns empty, fall back to
the Tournaments API.
"""
from __future__ import annotations

import hashlib
import json
import logging
from typing import Optional

from playwright.sync_api import Page, TimeoutError as PlaywrightTimeout

import config

logger = logging.getLogger(__name__)

# Started/ended tournaments use this API
TOURNAMENTDESK_API_URL = "https://prd-usta-kube-tournamentdesk-public-api.clubspark.pro/"
# Upcoming tournaments use this API
TOURNAMENTS_API_URL = "https://prd-usta-kube-tournaments.clubspark.pro/"


def scrape_tournament_detail_graphql(
    page: Page,
    tournament_id: str,
    org_slug: str,
) -> dict:
    """Scrape tournament participant entries via GraphQL.

    First navigates to the tournament players page to establish a browser session,
    then tries the TournamentDesk API (for started tournaments). If that returns
    empty, falls back to the Tournaments API (for upcoming tournaments).

    Returns {"entries": [...], "truly_empty": bool}.
    truly_empty=True means the page itself shows no players registered.
    """
    # Navigate to establish browser session / cookies
    players_url = (
        f"https://playtennis.usta.com/Competitions/"
        f"{org_slug}/Tournaments/players/{tournament_id}"
    )
    logger.debug("Navigating to %s", players_url)
    page.goto(players_url, timeout=config.TIMEOUT)
    _wait_for_page_ready(page)

    # Try TournamentDesk API first (richer data for started/ended tournaments)
    entries = _fetch_participants_tournamentdesk(page, tournament_id)
    if entries:
        logger.debug("Found %d entries via TournamentDesk API", len(entries))
        return {"entries": entries, "truly_empty": False}

    # Fall back to Tournaments API (works for all tournament statuses)
    logger.debug("TournamentDesk API unavailable, trying Tournaments API...")
    entries = _fetch_registrations_upcoming(page, tournament_id)
    if entries:
        logger.debug("Found %d entries via Tournaments API", len(entries))
        return {"entries": entries, "truly_empty": False}

    # Both APIs returned empty — check if the page itself shows "no players"
    truly_empty = _check_page_truly_empty(page)
    logger.debug("No entries from either API, truly_empty=%s", truly_empty)
    return {"entries": [], "truly_empty": truly_empty}


def _check_page_truly_empty(page: Page) -> bool:
    """Check if the tournament players page shows a 'no players' message."""
    try:
        content = page.text_content("body") or ""
        return "no players registered" in content.lower()
    except Exception:
        return False


def _wait_for_page_ready(page: Page):
    """Wait for the SPA to fully render and establish session cookies.

    The tournament players page is a React SPA.  A bare page.goto only waits
    for the initial HTML; we need the JS bundle to execute and the app shell
    to render before the browser has valid session cookies for GraphQL calls.
    """
    try:
        page.wait_for_load_state("networkidle", timeout=config.TIMEOUT)
    except PlaywrightTimeout:
        logger.debug("networkidle timed out, continuing anyway")

    # Wait for the SPA app shell — the player list or the "no players" message
    try:
        page.wait_for_selector(
            "[class*='player'], [class*='Player'], "
            "[class*='participant'], [class*='Participant'], "
            "[class*='registration'], [class*='Registration'], "
            "[class*='no-results'], [class*='NoResults'], "
            "[class*='empty'], table, .MuiTable-root",
            timeout=15000,
        )
    except PlaywrightTimeout:
        logger.debug("SPA content selector timed out, falling back to fixed wait")
        page.wait_for_timeout(5000)


# ── TournamentDesk API (started/ended tournaments) ───────────────────


def _fetch_participants_tournamentdesk(
    page: Page, tournament_id: str
) -> list[dict]:
    """Fetch participants via the TournamentDesk public API.

    This API works for tournaments that have started or ended.
    Returns richer data with entryStage, entryStatus, entryPosition, drawId.
    """
    query = """
    query getTournamentParticipants($tournamentId: ID!) {
      getTournamentParticipants(tournamentId: $tournamentId) {
        participantId
        participantType
        participantName
        participantRole
        participantStatus
        person {
          addresses {
            city
            state
          }
          personId
          personOtherIds {
            personId
            uniqueOrganisationName
          }
          sex
          standardGivenName
          standardFamilyName
        }
        draws {
          drawId
          eventId
        }
        events {
          eventId
          eventType
          entryStage
          entryStatus
          entryPosition
          statusDetail
        }
      }
    }
    """
    data = _call_graphql(
        page, query, {"tournamentId": tournament_id}, TOURNAMENTDESK_API_URL,
        silent=True,
    )
    if not data or "getTournamentParticipants" not in data:
        return []

    entries: list[dict] = []
    for p in data["getTournamentParticipants"]:
        # Only process competitors, skip officials
        if p.get("participantRole") != "COMPETITOR":
            continue

        person = p.get("person") or {}
        addresses = person.get("addresses") or [{}]
        address = addresses[0] if addresses else {}

        # Extract USTA ID
        usta_id = ""
        for other_id in person.get("personOtherIds", []):
            if other_id.get("uniqueOrganisationName") == "USTA":
                usta_id = other_id.get("personId", "")
                break

        participant_id = p.get("participantId", "")
        player_name = p.get("participantName", "")
        first_name = (person.get("standardGivenName", "") or "")
        last_name = (person.get("standardFamilyName", "") or "")
        gender = (person.get("sex", "") or "")
        city = (address.get("city", "") or "")
        state = (address.get("state", "") or "")

        # Build draw_id map: eventId -> drawId
        draws = {
            d["eventId"]: d["drawId"]
            for d in p.get("draws", [])
            if d.get("eventId")
        }

        for ev in p.get("events", []):
            event_id = ev.get("eventId", "")
            entries.append({
                "participant_id": participant_id,
                "player_uaid": usta_id,
                "player_name": player_name,
                "first_name": first_name,
                "last_name": last_name,
                "gender": gender,
                "city": city,
                "state": state,
                "event_id": event_id,
                "event_type": ev.get("eventType", ""),
                "entry_stage": ev.get("entryStage") or "",
                "entry_status": ev.get("entryStatus", ""),
                "entry_position": ev.get("entryPosition"),
                "status_detail": ev.get("statusDetail"),
                "draw_id": draws.get(event_id),
            })

    return entries


# ── Tournaments API (upcoming tournaments) ────────────────────────────


def _fetch_registrations_upcoming(
    page: Page, tournament_id: str
) -> list[dict]:
    """Fetch registrations via the Tournaments API.

    This API works for both upcoming and completed tournaments.
    Uses paginatedPublicTournamentRegistrations.

    For doubles events (detected when eventEntries.players has 2+ players),
    creates team summary entries with "LastName1/LastName2" format and shared
    draw_id to enable proper pair display on the frontend.
    """
    query = """
    query paginatedPublicTournamentRegistrations(
      $tournamentId: UUID!,
      $queryParameters: QueryParametersPaged!
    ) {
      paginatedPublicTournamentRegistrations(
        tournamentId: $tournamentId
        queryParameters: $queryParameters
      ) {
        totalItems
        items {
          playerCity
          playerFirstName
          playerGender
          playerLastName
          playerId {
            key
            value
          }
          playerCustomIds {
            key
            value
          }
          playerName
          playerState
          eventEntries {
            eventId
            partnershipStatus
            players {
              customId {
                key
                value
              }
              firstName
              lastName
            }
          }
          events {
            id
          }
        }
      }
    }
    """

    tid = tournament_id.upper()
    all_entries: list[dict] = []
    # Track which team summaries have been created (by draw_id) to avoid duplicates
    team_summaries_created: set[str] = set()
    offset = 0
    limit = 0  # 0 = fetch all at once (matches actual page behaviour)

    while True:
        variables = {
            "tournamentId": tid,
            "queryParameters": {
                "limit": limit,
                "offset": offset,
                "sorts": [],
                "filters": [],
            },
        }

        data = _call_graphql(page, query, variables, TOURNAMENTS_API_URL)
        if not data or "paginatedPublicTournamentRegistrations" not in data:
            break

        regs = data["paginatedPublicTournamentRegistrations"]
        items = regs.get("items", [])
        total = regs.get("totalItems", 0)

        if not items:
            break

        for item in items:
            first_name = item.get("playerFirstName", "") or ""
            last_name = item.get("playerLastName", "") or ""
            player_name = item.get("playerName", "") or f"{first_name} {last_name}".strip()
            gender = item.get("playerGender", "") or ""
            city = item.get("playerCity", "") or ""
            state = item.get("playerState", "") or ""

            # Extract USTA ID from playerCustomIds
            usta_id = ""
            for cid in item.get("playerCustomIds", []):
                if cid.get("key") == "ustaId":
                    usta_id = cid.get("value", "")
                    break

            # Build eventId map from eventEntries for status/players lookup
            event_entries = {
                ee["eventId"]: ee for ee in item.get("eventEntries", [])
            }

            # Create one entry per event
            for ev in item.get("events", []):
                event_id = ev.get("id", "")

                event_entry = event_entries.get(event_id, {})
                entry_status = "REGISTERED" if event_entry else "PENDING"

                # Try to get USTA ID from eventEntries.players if not found
                entry_usta_id = usta_id
                if not entry_usta_id and event_entry:
                    for p in event_entry.get("players", []):
                        cid = p.get("customId") or {}
                        if cid.get("key") == "ustaId":
                            entry_usta_id = cid.get("value", "")
                            break

                # Detect doubles: eventEntries.players has 2+ players
                players_in_entry = event_entry.get("players", [])
                is_doubles = len(players_in_entry) >= 2

                if is_doubles:
                    # Generate a consistent draw_id from sorted player identifiers
                    draw_id = _make_pair_draw_id(event_id, players_in_entry)

                    # Create team summary entry (once per pair per event)
                    if draw_id not in team_summaries_created:
                        pair_last_names = [
                            (p.get("lastName") or "").strip()
                            for p in players_in_entry
                        ]
                        team_name = "/".join(pair_last_names)
                        all_entries.append({
                            "participant_id": f"team-{draw_id}",
                            "player_uaid": "",
                            "player_name": team_name,
                            "first_name": "",
                            "last_name": "",
                            "gender": gender,
                            "city": "",
                            "state": "",
                            "event_id": event_id,
                            "event_type": "DOUBLES",
                            "entry_stage": "MAIN",
                            "entry_status": entry_status,
                            "entry_position": None,
                            "status_detail": None,
                            "draw_id": draw_id,
                        })
                        team_summaries_created.add(draw_id)

                    # Individual entry with shared draw_id and DOUBLES type
                    all_entries.append({
                        "participant_id": entry_usta_id or player_name,
                        "player_uaid": entry_usta_id,
                        "player_name": player_name,
                        "first_name": first_name,
                        "last_name": last_name,
                        "gender": gender,
                        "city": city,
                        "state": state,
                        "event_id": event_id,
                        "event_type": "DOUBLES",
                        "entry_stage": "MAIN",
                        "entry_status": entry_status,
                        "entry_position": None,
                        "status_detail": None,
                        "draw_id": draw_id,
                    })
                else:
                    # Singles or unpartnered entry
                    all_entries.append({
                        "participant_id": entry_usta_id or player_name,
                        "player_uaid": entry_usta_id,
                        "player_name": player_name,
                        "first_name": first_name,
                        "last_name": last_name,
                        "gender": gender,
                        "city": city,
                        "state": state,
                        "event_id": event_id,
                        "event_type": "",
                        "entry_stage": "MAIN",
                        "entry_status": entry_status,
                        "entry_position": None,
                        "status_detail": None,
                        "draw_id": None,
                    })

        logger.debug(
            "  Fetched %d items, cumulative %d/%d", len(items), len(all_entries), total
        )

        # limit=0 fetches everything in one shot
        if limit == 0 or len(items) < limit or len(all_entries) >= total:
            break

        offset += limit

    return all_entries


def _make_pair_draw_id(event_id: str, players: list[dict]) -> str:
    """Generate a consistent draw_id from sorted player identifiers.

    Uses UAID if available, otherwise falls back to firstName+lastName.
    The result is deterministic regardless of player order in the array.
    """
    ids = []
    for p in players:
        cid = p.get("customId") or {}
        if cid.get("key") == "ustaId" and cid.get("value"):
            ids.append(cid["value"])
        else:
            ids.append(
                f"{(p.get('firstName') or '').strip()}_{(p.get('lastName') or '').strip()}".lower()
            )
    ids.sort()
    raw = f"{event_id}::{'::'.join(ids)}"
    return hashlib.md5(raw.encode()).hexdigest()[:12]


# ── Common GraphQL helper ─────────────────────────────────────────────


def _call_graphql(
    page: Page, query: str, variables: dict, api_url: str,
    *, silent: bool = False,
) -> Optional[dict]:
    """Execute a GraphQL query via browser fetch with timeout and status check.

    If *silent* is True, errors are logged at DEBUG level (used for the
    TournamentDesk probe where "not found" is expected).
    """
    _log = logger.debug if silent else logger.error
    try:
        safe_query = query.replace("`", "\\`")
        response = page.evaluate(f"""
            async () => {{
                const controller = new AbortController();
                const timer = setTimeout(() => controller.abort(), {config.TIMEOUT});
                try {{
                    const res = await fetch('{api_url}', {{
                        method: 'POST',
                        headers: {{
                            'Content-Type': 'application/json',
                            'Accept': 'application/json'
                        }},
                        body: JSON.stringify({{
                            query: `{safe_query}`,
                            variables: {json.dumps(variables)}
                        }}),
                        signal: controller.signal
                    }});
                    clearTimeout(timer);
                    if (!res.ok) {{
                        return {{error: `HTTP ${{res.status}} ${{res.statusText}}`, status: res.status}};
                    }}
                    return await res.json();
                }} catch (e) {{
                    clearTimeout(timer);
                    return {{error: e.name === 'AbortError' ? 'Request timed out' : e.message}};
                }}
            }}
        """)

        if not response:
            _log("Empty GraphQL response from %s", api_url)
            return None

        if "error" in response:
            status = response.get("status", "")
            _log("GraphQL error (HTTP %s): %s", status, response["error"])
            return None

        if "errors" in response:
            _log("GraphQL errors: %s", response["errors"])
            return None

        return response.get("data")

    except Exception as e:
        logger.error("GraphQL call failed: %s", e)
        return None
