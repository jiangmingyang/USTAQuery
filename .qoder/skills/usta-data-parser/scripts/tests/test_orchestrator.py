"""Unit tests for scrape_tournament_detail_graphql — the orchestrator.

Covers: dual API fallback, truly_empty detection, registration status,
and _detect_registration_status / _check_page_truly_empty directly.
"""
import sys
import unittest
from pathlib import Path
from unittest.mock import MagicMock, patch

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from pages.tournament_detail_graphql import (
    scrape_tournament_detail_graphql,
    _detect_registration_status,
    _check_page_truly_empty,
)

MODULE = "pages.tournament_detail_graphql"

SAMPLE_ENTRY = {
    "participant_id": "p-001", "player_uaid": "2019000001",
    "player_name": "John Smith", "first_name": "John", "last_name": "Smith",
    "gender": "MALE", "city": "New York", "state": "NY",
    "event_id": "evt-001", "event_type": "SINGLES",
    "entry_stage": "MAIN", "entry_status": "DIRECT_ACCEPTANCE",
    "entry_position": 1, "status_detail": None, "draw_id": "draw-001",
}


def _mock_page(body_text=""):
    page = MagicMock()
    page.text_content.return_value = body_text
    return page


class TestScrapeTournamentDetailGraphql(unittest.TestCase):

    def _call(self, page, tid="some-uuid", org="abc",
              td_entries=None, t_entries=None,
              reg_status="Completed", truly_empty=False):
        with patch(f"{MODULE}._fetch_participants_tournamentdesk", return_value=td_entries or []), \
             patch(f"{MODULE}._fetch_registrations_upcoming", return_value=t_entries or []), \
             patch(f"{MODULE}._detect_registration_status", return_value=reg_status), \
             patch(f"{MODULE}._check_page_truly_empty", return_value=truly_empty), \
             patch(f"{MODULE}._wait_for_page_ready"):
            return scrape_tournament_detail_graphql(page, tid, org)

    def test_tournamentdesk_succeeds_no_fallback(self):
        page = _mock_page()
        result = self._call(page, td_entries=[SAMPLE_ENTRY])
        self.assertEqual(len(result["entries"]), 1)
        self.assertFalse(result["truly_empty"])

    def test_fallback_when_tournamentdesk_empty(self):
        page = _mock_page()
        fallback_entry = {**SAMPLE_ENTRY, "player_name": "Fallback Player"}
        result = self._call(page, td_entries=[], t_entries=[fallback_entry])
        self.assertEqual(len(result["entries"]), 1)
        self.assertEqual(result["entries"][0]["player_name"], "Fallback Player")

    def test_both_empty_truly_empty_true(self):
        page = _mock_page()
        result = self._call(page, td_entries=[], t_entries=[], truly_empty=True)
        self.assertEqual(result["entries"], [])
        self.assertTrue(result["truly_empty"])

    def test_both_empty_truly_empty_false(self):
        page = _mock_page()
        result = self._call(page, td_entries=[], t_entries=[], truly_empty=False)
        self.assertEqual(result["entries"], [])
        self.assertFalse(result["truly_empty"])

    def test_registration_status_open(self):
        page = _mock_page()
        result = self._call(page, td_entries=[SAMPLE_ENTRY], reg_status="Registrations open")
        self.assertEqual(result["registration_status"], "Registrations open")

    def test_registration_status_completed(self):
        page = _mock_page()
        result = self._call(page, td_entries=[SAMPLE_ENTRY], reg_status="Completed")
        self.assertEqual(result["registration_status"], "Completed")

    def test_registration_status_none(self):
        page = _mock_page()
        result = self._call(page, td_entries=[SAMPLE_ENTRY], reg_status=None)
        self.assertIsNone(result["registration_status"])

    def test_navigates_to_correct_url(self):
        page = _mock_page()
        tid = "94F38D3B-2F9B-4038-9A60-28EDA4E8BF3D"
        org = "el-conquistador-tennis"
        self._call(page, tid=tid, org=org, td_entries=[SAMPLE_ENTRY])
        page.goto.assert_called_once()
        url = page.goto.call_args.args[0]
        self.assertIn(f"/{org}/Tournaments/players/{tid}", url)


class TestDetectRegistrationStatus(unittest.TestCase):

    def test_detects_registrations_open(self):
        page = _mock_page("USTA Tournament — Registrations open — 64 players")
        self.assertEqual(_detect_registration_status(page), "Registrations open")

    def test_detects_registrations_closed(self):
        page = _mock_page("Championship — Registrations closed — 128 players")
        self.assertEqual(_detect_registration_status(page), "Registrations closed")

    def test_detects_completed(self):
        page = _mock_page("Winter Junior Invite — Completed — Final results")
        self.assertEqual(_detect_registration_status(page), "Completed")

    def test_returns_none_when_absent(self):
        page = _mock_page("Welcome to the tournament page.")
        self.assertIsNone(_detect_registration_status(page))

    def test_case_insensitive(self):
        page = _mock_page("REGISTRATIONS OPEN for this tournament")
        self.assertEqual(_detect_registration_status(page), "Registrations open")

    def test_page_error_returns_none(self):
        page = MagicMock()
        page.text_content.side_effect = Exception("DOM error")
        self.assertIsNone(_detect_registration_status(page))

    def test_empty_page_returns_none(self):
        page = _mock_page("")
        self.assertIsNone(_detect_registration_status(page))

    def test_priority_open_over_completed(self):
        """'Registrations open' matches before 'Completed'."""
        page = _mock_page("Registrations open — but also Completed somewhere")
        self.assertEqual(_detect_registration_status(page), "Registrations open")


class TestCheckPageTrulyEmpty(unittest.TestCase):

    def test_detects_no_players(self):
        page = _mock_page("No players registered for this tournament yet.")
        self.assertTrue(_check_page_truly_empty(page))

    def test_not_empty(self):
        page = _mock_page("64 players registered for this tournament.")
        self.assertFalse(_check_page_truly_empty(page))

    def test_error_returns_false(self):
        page = MagicMock()
        page.text_content.side_effect = Exception("DOM error")
        self.assertFalse(_check_page_truly_empty(page))


if __name__ == "__main__":
    unittest.main()
