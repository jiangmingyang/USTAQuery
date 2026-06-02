"""Unit tests for _fetch_registrations_upcoming — Tournaments API processing.

Uses real GraphQL response fixtures captured from the USTA Tournaments API
(tournament 28832: El Conquistador Tennis).
"""
import json
import sys
import unittest
from pathlib import Path
from unittest.mock import MagicMock, patch

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from pages.tournament_detail_graphql import _fetch_registrations_upcoming

MODULE = "pages.tournament_detail_graphql"
FIXTURES = Path(__file__).resolve().parent / "fixtures"


def _load(name: str) -> dict:
    """Load a JSON fixture file."""
    with open(FIXTURES / name) as f:
        return json.load(f)


# ── Pre-loaded fixtures ─────────────────────────────────────────────
# All captured from real USTA Tournaments API responses

TA_TWO = _load("ta_two_singles.json")        # 2 real singles players (Caleb Wang, Reagan Chou)
TA_EMPTY = _load("ta_empty.json")            # Real empty response
TA_NONEXIST = _load("ta_nonexistent.json")   # Real error for nonexistent UUID
TA_PAGE1 = _load("ta_page1.json")            # Real page 1: 2 items (totalItems=3)
TA_PAGE2 = _load("ta_page2.json")            # Real page 2: 1 item (Devansh Patra)

# ── Derived fixtures (based on real data structure) ─────────────────
# Doubles: based on real players Caleb Wang (UAID=2018905072) and Reagan Chou (UAID=2018608139)
_real_caleb = TA_TWO["paginatedPublicTournamentRegistrations"]["items"][0]
_real_reagan = TA_TWO["paginatedPublicTournamentRegistrations"]["items"][1]
_dbl_event_id = "E6B9CE1A-0628-44F6-A983-0FE208BB09FF"
_dbl_players = [
    {
        "customId": {"key": "ustaId", "value": "2018905072"},
        "firstName": "Caleb",
        "lastName": "Wang",
    },
    {
        "customId": {"key": "ustaId", "value": "2018608139"},
        "firstName": "Reagan",
        "lastName": "Chou",
    },
]

TA_DOUBLES = {
    "paginatedPublicTournamentRegistrations": {
        "totalItems": 2,
        "items": [
            {
                **_real_caleb,
                "eventEntries": [{
                    "eventId": _dbl_event_id,
                    "partnershipStatus": "CONFIRMED",
                    "players": _dbl_players,
                }],
            },
            {
                **_real_reagan,
                "eventEntries": [{
                    "eventId": _dbl_event_id,
                    "partnershipStatus": "CONFIRMED",
                    "players": _dbl_players,
                }],
            },
        ],
    }
}

# Multi-event: 1 real player (Caleb Wang) in 2 events
TA_MULTI_EVENT = {
    "paginatedPublicTournamentRegistrations": {
        "totalItems": 1,
        "items": [{
            **_real_caleb,
            "events": [
                {"id": "E6B9CE1A-0628-44F6-A983-0FE208BB09FF"},
                {"id": "A1B2C3D4-1111-2222-3333-444455556666"},
            ],
            "eventEntries": [
                _real_caleb["eventEntries"][0],
                {
                    "eventId": "A1B2C3D4-1111-2222-3333-444455556666",
                    "partnershipStatus": "NONE",
                    "players": [_real_caleb["eventEntries"][0]["players"][0]],
                },
            ],
        }],
    }
}

# No top-level ustaId, but eventEntries.players has it (fallback path)
TA_NO_TOP_USTA = {
    "paginatedPublicTournamentRegistrations": {
        "totalItems": 1,
        "items": [{
            **_real_caleb,
            "playerCustomIds": [
                {"key": "userId", "value": "96D09847-4703-4724-85CF-9235CD758D44"},
            ],
            "eventEntries": [{
                "eventId": _dbl_event_id,
                "partnershipStatus": "NONE",
                "players": [{
                    "customId": {"key": "ustaId", "value": "2018905072"},
                    "firstName": "Caleb",
                    "lastName": "Wang",
                }],
            }],
        }],
    }
}

# Null playerName (derived from real — triggers name construction from first+last)
TA_NULL_NAME = {
    "paginatedPublicTournamentRegistrations": {
        "totalItems": 1,
        "items": [{
            **_real_caleb,
            "playerName": None,
        }],
    }
}

# Event with no matching eventEntries entry (PENDING status path)
TA_PENDING = {
    "paginatedPublicTournamentRegistrations": {
        "totalItems": 1,
        "items": [{
            **_real_caleb,
            "events": [
                {"id": _dbl_event_id},
                {"id": "FFFFFFFF-0000-0000-0000-000000000000"},
            ],
        }],
    }
}


class TestFetchRegistrationsUpcoming(unittest.TestCase):
    """Tests use real USTA Tournaments API responses from tournament 28832."""

    TOURNAMENT_ID = "94F38D3B-2F9B-4038-9A60-28EDA4E8BF3D"

    def _call(self, fixture, tid=None):
        page = MagicMock()
        with patch(f"{MODULE}._call_graphql", return_value=fixture):
            return _fetch_registrations_upcoming(page, tid or self.TOURNAMENT_ID)

    # ── Basic extraction (real data) ────────────────────────────────

    def test_singles_extraction(self):
        """Real: 2 singles players from tournament 28832."""
        entries = self._call(TA_TWO)
        self.assertEqual(len(entries), 2)
        # Verify real player data
        self.assertEqual(entries[0]["player_name"], "Caleb Wang")
        self.assertEqual(entries[0]["player_uaid"], "2018905072")
        self.assertEqual(entries[0]["first_name"], "Caleb")
        self.assertEqual(entries[0]["last_name"], "Wang")
        self.assertEqual(entries[0]["city"], "Bellevue")
        self.assertEqual(entries[0]["state"], "WA")
        self.assertEqual(entries[0]["gender"], "MALE")
        self.assertEqual(entries[0]["entry_stage"], "MAIN")
        self.assertEqual(entries[0]["entry_status"], "REGISTERED")
        self.assertEqual(entries[0]["event_type"], "")
        self.assertIsNone(entries[0]["draw_id"])

    def test_second_player(self):
        """Real: Reagan Chou from NY."""
        entries = self._call(TA_TWO)
        self.assertEqual(entries[1]["player_name"], "Reagan Chou")
        self.assertEqual(entries[1]["player_uaid"], "2018608139")
        self.assertEqual(entries[1]["state"], "NY")

    # ── Doubles handling (derived from real players) ────────────────

    def test_doubles_creates_team_summary(self):
        """2 players in doubles -> 1 team summary + 2 individuals = 3 entries."""
        entries = self._call(TA_DOUBLES)
        self.assertEqual(len(entries), 3)
        team = [e for e in entries if e["first_name"] == "" and "/" in e["player_name"]]
        self.assertEqual(len(team), 1)
        self.assertEqual(team[0]["player_name"], "Wang/Chou")

    def test_doubles_shared_draw_id(self):
        """Team summary + both individuals share the same draw_id."""
        entries = self._call(TA_DOUBLES)
        draw_ids = {e["draw_id"] for e in entries}
        self.assertEqual(len(draw_ids), 1)  # All same
        self.assertIsNotNone(entries[0]["draw_id"])

    def test_doubles_team_summary_not_duplicated(self):
        """Two registrations reference same pair -> only 1 team summary."""
        entries = self._call(TA_DOUBLES)
        teams = [e for e in entries if e["participant_id"].startswith("team-")]
        self.assertEqual(len(teams), 1)

    def test_doubles_event_type_set(self):
        """All doubles entries have event_type == 'DOUBLES'."""
        entries = self._call(TA_DOUBLES)
        for e in entries:
            self.assertEqual(e["event_type"], "DOUBLES")

    def test_doubles_individuals_have_real_data(self):
        """Individual doubles entries retain real player data."""
        entries = self._call(TA_DOUBLES)
        individuals = [e for e in entries if e["first_name"]]
        self.assertEqual(len(individuals), 2)
        names = {e["player_name"] for e in individuals}
        self.assertEqual(names, {"Caleb Wang", "Reagan Chou"})

    # ── Entry status defaults ───────────────────────────────────────

    def test_entry_status_registered(self):
        """Items with matching eventEntries -> REGISTERED."""
        entries = self._call(TA_TWO)
        self.assertTrue(all(e["entry_status"] == "REGISTERED" for e in entries))

    def test_entry_status_pending(self):
        """Event in events but no matching eventEntries -> PENDING."""
        entries = self._call(TA_PENDING)
        pending = [e for e in entries if e["entry_status"] == "PENDING"]
        self.assertEqual(len(pending), 1)
        self.assertEqual(pending[0]["event_id"], "FFFFFFFF-0000-0000-0000-000000000000")

    def test_entry_stage_always_main(self):
        """All entries have entry_stage == 'MAIN' (API limitation)."""
        entries = self._call(TA_TWO)
        self.assertTrue(all(e["entry_stage"] == "MAIN" for e in entries))

    # ── USTA ID extraction (real + derived) ─────────────────────────

    def test_usta_id_from_custom_ids(self):
        """Real: USTA ID from playerCustomIds."""
        entries = self._call(TA_TWO)
        self.assertEqual(entries[0]["player_uaid"], "2018905072")
        self.assertEqual(entries[1]["player_uaid"], "2018608139")

    def test_usta_id_fallback_from_event_entry(self):
        """Derived: top-level customIds has no ustaId -> extracted from eventEntries.players."""
        entries = self._call(TA_NO_TOP_USTA)
        self.assertEqual(entries[0]["player_uaid"], "2018905072")

    # ── Empty/error responses (real) ────────────────────────────────

    def test_empty_response_returns_empty(self):
        """Real: empty items list from Tournaments API."""
        entries = self._call(TA_EMPTY)
        self.assertEqual(entries, [])

    def test_nonexistent_tournament_returns_empty(self):
        """Real: nonexistent UUID returns error (no data key)."""
        entries = self._call(TA_NONEXIST)
        self.assertEqual(entries, [])

    def test_null_response_returns_empty(self):
        """_call_graphql returns None -> []."""
        page = MagicMock()
        with patch(f"{MODULE}._call_graphql", return_value=None):
            result = _fetch_registrations_upcoming(page, "some-uuid")
        self.assertEqual(result, [])

    # ── UUID case (real) ────────────────────────────────────────────

    def test_uppercase_applied_to_tournament_id(self):
        """Pass lowercase tid -> _call_graphql receives uppercase."""
        page = MagicMock()
        tid_lower = "94f38d3b-2f9b-4038-9a60-28eda4e8bf3d"

        with patch(f"{MODULE}._call_graphql", return_value=TA_EMPTY) as mock:
            _fetch_registrations_upcoming(page, tid_lower)

        call_vars = mock.call_args.args[2]
        self.assertEqual(call_vars["tournamentId"], tid_lower.upper())

    # ── Multiple events (derived from real) ─────────────────────────

    def test_multiple_events_per_item(self):
        """Derived: 1 item (Caleb Wang) in 2 events -> 2 entries."""
        entries = self._call(TA_MULTI_EVENT)
        self.assertEqual(len(entries), 2)
        event_ids = {e["event_id"] for e in entries}
        self.assertEqual(event_ids, {
            "E6B9CE1A-0628-44F6-A983-0FE208BB09FF",
            "A1B2C3D4-1111-2222-3333-444455556666",
        })
        # Both entries should be for the same player
        self.assertTrue(all(e["player_name"] == "Caleb Wang" for e in entries))

    # ── Pagination (real pages) ─────────────────────────────────────

    def test_single_call_processes_partial_page(self):
        """Real page1 fixture: 2 items with totalItems=3.

        limit=0 (hardcoded) breaks after first call — verifies correct
        processing of a partial page from real API data.
        """
        entries = self._call(TA_PAGE1)
        self.assertEqual(len(entries), 2)
        # Verify real players from page 1
        names = [e["player_name"] for e in entries]
        self.assertIn("Caleb Wang", names)
        self.assertIn("Reagan Chou", names)

    # ── Player name fallback (derived) ──────────────────────────────

    def test_player_name_fallback(self):
        """Derived: playerName is null -> constructed from first+last."""
        entries = self._call(TA_NULL_NAME)
        self.assertEqual(entries[0]["player_name"], "Caleb Wang")


if __name__ == "__main__":
    unittest.main()
