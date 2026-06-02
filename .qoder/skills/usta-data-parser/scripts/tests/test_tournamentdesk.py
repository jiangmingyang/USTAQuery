"""Unit tests for _fetch_participants_tournamentdesk — TournamentDesk API processing.

Uses real GraphQL response fixtures captured from the USTA TournamentDesk API
(tournament 28832: El Conquistador Tennis).
"""
import json
import sys
import unittest
from pathlib import Path
from unittest.mock import MagicMock, patch

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from pages.tournament_detail_graphql import _fetch_participants_tournamentdesk

MODULE = "pages.tournament_detail_graphql"
FIXTURES = Path(__file__).resolve().parent / "fixtures"


def _load(name: str) -> dict:
    """Load a JSON fixture file."""
    with open(FIXTURES / name) as f:
        return json.load(f)


# ── Pre-loaded fixtures ─────────────────────────────────────────────
# All captured from real USTA TournamentDesk API responses

TD_TWO = _load("td_two_competitors.json")         # 2 real COMPETITOR participants
TD_OFFICIAL = _load("td_with_official.json")       # 1 COMPETITOR + 1 OFFICIAL (real)
TD_ALL_STATUSES = _load("td_all_statuses.json")    # 3 real: DIRECT_ACCEPTANCE, WITHDRAWN, UNGROUPED
TD_UNGROUPED = _load("td_ungrouped.json")          # 1 real UNGROUPED player
TD_MULTI = _load("td_multi_events.json")           # 1 real player with 2 events
TD_EMPTY = _load("td_empty.json")                  # {"getTournamentParticipants": []}
TD_NULL = _load("td_null.json")                    # {"getTournamentParticipants": null}
TD_LOWERCASE = _load("td_lowercase_empty.json")    # Real lowercase UUID error response
TD_NONEXIST = _load("td_nonexistent.json")         # Real nonexistent tournament response

# Derived fixtures (synthetic edge cases based on real data structure)
TD_NULL_ADDRESS = {"getTournamentParticipants": [{
    **TD_TWO["getTournamentParticipants"][0],
    "person": {
        **TD_TWO["getTournamentParticipants"][0]["person"],
        "addresses": None,
    },
}]}

TD_NULL_PERSON = {"getTournamentParticipants": [{
    "participantId": "p-null",
    "participantType": "SINGLES",
    "participantName": "Null Fields",
    "participantRole": "COMPETITOR",
    "participantStatus": "ACTIVE",
    "person": {
        "addresses": [{}],
        "personId": "person-null",
        "personOtherIds": [],
        "sex": None,
        "standardGivenName": None,
        "standardFamilyName": None,
    },
    "draws": [],
    "events": TD_TWO["getTournamentParticipants"][0]["events"],
}]}

# Real participant with extra ITF id added for testing
_real_p = TD_TWO["getTournamentParticipants"][0]
TD_MULTI_IDS = {"getTournamentParticipants": [{
    **_real_p,
    "person": {
        **_real_p["person"],
        "personOtherIds": _real_p["person"]["personOtherIds"] + [
            {"personId": "ITF-999", "uniqueOrganisationName": "ITF"},
        ],
    },
}]}

# Real participant with USTA ID removed
TD_NO_USTA_ID = {"getTournamentParticipants": [{
    **_real_p,
    "person": {
        **_real_p["person"],
        "personOtherIds": [],
    },
}]}


class TestFetchParticipantsTournamentDesk(unittest.TestCase):
    """Tests use real USTA TournamentDesk API responses from tournament 28832."""

    TOURNAMENT_ID = "94F38D3B-2F9B-4038-9A60-28EDA4E8BF3D"

    def _call(self, fixture, tid=None):
        page = MagicMock()
        with patch(f"{MODULE}._call_graphql", return_value=fixture):
            return _fetch_participants_tournamentdesk(page, tid or self.TOURNAMENT_ID)

    # ── Basic extraction (real data) ────────────────────────────────

    def test_extracts_singles_entries(self):
        """Real: 2 COMPETITOR participants from tournament 28832."""
        entries = self._call(TD_TWO)
        self.assertEqual(len(entries), 2)
        # Verify real player data
        self.assertEqual(entries[0]["player_name"], "James O'Grady")
        self.assertEqual(entries[0]["player_uaid"], "2019156302")
        self.assertEqual(entries[0]["entry_stage"], "MAIN")
        self.assertEqual(entries[0]["entry_status"], "DIRECT_ACCEPTANCE")
        self.assertEqual(entries[0]["first_name"], "James")
        self.assertEqual(entries[0]["last_name"], "O'Grady")

    def test_skips_officials(self):
        """Real: 1 COMPETITOR (James O'Grady) + 1 OFFICIAL (Jennifer Fuchs)."""
        entries = self._call(TD_OFFICIAL)
        self.assertEqual(len(entries), 1)
        self.assertEqual(entries[0]["participantRole"] if "participantRole" in entries[0] else "filtered", "filtered")
        self.assertEqual(entries[0]["player_name"], "James O'Grady")

    # ── UNGROUPED status (real data) ────────────────────────────────

    def test_ungrouped_status_preserved(self):
        """Real: Alejandro Pazos has entryStatus=UNGROUPED from USTA API."""
        entries = self._call(TD_UNGROUPED)
        self.assertEqual(len(entries), 1)
        self.assertEqual(entries[0]["entry_status"], "UNGROUPED")
        self.assertEqual(entries[0]["player_name"], "Alejandro Pazos")

    def test_all_entry_statuses(self):
        """Real: 3 players with DIRECT_ACCEPTANCE, WITHDRAWN, UNGROUPED."""
        entries = self._call(TD_ALL_STATUSES)
        statuses = {e["entry_status"] for e in entries}
        self.assertEqual(statuses, {"DIRECT_ACCEPTANCE", "WITHDRAWN", "UNGROUPED"})
        # Verify all are preserved as-is from the API
        for e in entries:
            self.assertIn(e["entry_status"], ("DIRECT_ACCEPTANCE", "WITHDRAWN", "UNGROUPED"))

    # ── UUID case retry (real responses) ────────────────────────────

    def test_uppercase_uuid_retry(self):
        """Real: lowercase UUID returns error/null, uppercase returns 2 players."""
        page = MagicMock()
        tid_lower = "94f38d3b-2f9b-4038-9a60-28eda4e8bf3d"
        tid_upper = tid_lower.upper()

        def side_effect(pg, query, variables, url, **kwargs):
            if variables.get("tournamentId") == tid_lower:
                return TD_LOWERCASE  # Real: {"data": {"getTournamentParticipants": null}, "errors": [...]}
            return TD_TWO  # Real: uppercase returns data

        with patch(f"{MODULE}._call_graphql", side_effect=side_effect) as mock:
            entries = _fetch_participants_tournamentdesk(page, tid_lower)

        self.assertEqual(len(entries), 2)
        self.assertEqual(mock.call_count, 2)
        self.assertEqual(mock.call_args_list[0].args[2]["tournamentId"], tid_lower)
        self.assertEqual(mock.call_args_list[1].args[2]["tournamentId"], tid_upper)

    def test_no_retry_when_already_uppercase(self):
        """Real: uppercase UUID returns data on first call."""
        page = MagicMock()

        with patch(f"{MODULE}._call_graphql", return_value=TD_TWO) as mock:
            entries = _fetch_participants_tournamentdesk(page, self.TOURNAMENT_ID)

        self.assertEqual(len(entries), 2)
        self.assertEqual(mock.call_count, 1)

    def test_mixed_case_triggers_retry(self):
        """Mixed case UUID triggers retry since tid != tid.upper()."""
        page = MagicMock()
        tid_mixed = "94F38d3b-2f9b-4038-9a60-28eda4e8bf3d"
        call_count = 0

        def side_effect(pg, query, variables, url, **kwargs):
            nonlocal call_count
            call_count += 1
            if call_count == 1:
                return TD_EMPTY
            return TD_TWO

        with patch(f"{MODULE}._call_graphql", side_effect=side_effect) as mock:
            entries = _fetch_participants_tournamentdesk(page, tid_mixed)

        self.assertEqual(len(entries), 2)
        self.assertEqual(mock.call_count, 2)

    # ── Empty/null responses (real) ─────────────────────────────────

    def test_empty_response_returns_empty(self):
        """_call_graphql returns None → []."""
        page = MagicMock()
        with patch(f"{MODULE}._call_graphql", return_value=None):
            result = _fetch_participants_tournamentdesk(page, "some-uuid")
        self.assertEqual(result, [])

    def test_null_participants_returns_empty(self):
        """Real: getTournamentParticipants: null (from lowercase UUID)."""
        entries = self._call(TD_NULL)
        self.assertEqual(entries, [])

    def test_missing_key_returns_empty(self):
        """Response without getTournamentParticipants key → []."""
        entries = self._call({})
        self.assertEqual(entries, [])

    def test_nonexistent_tournament_returns_empty(self):
        """Real: nonexistent UUID returns null with error."""
        entries = self._call(TD_NONEXIST)
        self.assertEqual(entries, [])

    # ── USTA ID extraction (real + derived) ─────────────────────────

    def test_usta_id_from_person_other_ids(self):
        """Real: USTA ID extracted from personOtherIds where org == 'USTA'."""
        entries = self._call(TD_TWO)
        self.assertEqual(entries[0]["player_uaid"], "2019156302")

    def test_usta_id_ignores_non_usta_orgs(self):
        """Derived: USTA ID chosen over ITF ID in personOtherIds."""
        entries = self._call(TD_MULTI_IDS)
        self.assertEqual(entries[0]["player_uaid"], "2019156302")

    def test_no_usta_id(self):
        """Derived: empty personOtherIds → player_uaid == ''."""
        entries = self._call(TD_NO_USTA_ID)
        self.assertEqual(entries[0]["player_uaid"], "")

    # ── Multiple events (real data) ─────────────────────────────────

    def test_multiple_events_per_participant(self):
        """Real: Adrian Choi in 2 events (TEAM + TEAM)."""
        entries = self._call(TD_MULTI)
        self.assertEqual(len(entries), 2)
        event_ids = {e["event_id"] for e in entries}
        self.assertEqual(len(event_ids), 2)
        # Same participant_id for both
        self.assertEqual(entries[0]["participant_id"], entries[1]["participant_id"])
        self.assertEqual(entries[0]["player_name"], "Adrian Choi")

    def test_draw_id_mapped_per_event(self):
        """Real: draw_id maps correctly to each event."""
        entries = self._call(TD_MULTI)
        # Adrian has 1 draw for event E6B9CE1A...
        evt_with_draw = next(e for e in entries if e["draw_id"] is not None)
        self.assertEqual(evt_with_draw["event_id"], "E6B9CE1A-0628-44F6-A983-0FE208BB09FF")

    # ── Null handling (derived from real) ───────────────────────────

    def test_null_address_handling(self):
        """Derived from real: addresses: null → city/state default to ''."""
        entries = self._call(TD_NULL_ADDRESS)
        self.assertEqual(entries[0]["city"], "")
        self.assertEqual(entries[0]["state"], "")

    def test_null_person_fields(self):
        """Null standardGivenName/standardFamilyName/sex → default to ''."""
        entries = self._call(TD_NULL_PERSON)
        self.assertEqual(entries[0]["first_name"], "")
        self.assertEqual(entries[0]["last_name"], "")
        self.assertEqual(entries[0]["gender"], "")


if __name__ == "__main__":
    unittest.main()
