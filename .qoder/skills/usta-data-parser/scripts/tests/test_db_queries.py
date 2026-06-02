"""Unit tests for db.get_tournaments_to_scrape — SQL query logic.

Covers: normal mode skip logic, force mode, date range, limit, cancelled skip.
Mocks the DB cursor to capture SQL and params without a real MySQL connection.
"""
import sys
import unittest
from contextlib import contextmanager
from pathlib import Path
from unittest.mock import MagicMock, patch

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

MODULE_DB = "db"


@contextmanager
def _mock_cursor_ctx():
    """Helper: create a mock cursor context manager."""
    cur = MagicMock()
    cur.fetchall.return_value = []
    yield cur


class TestGetTournamentsToScrape(unittest.TestCase):

    def _call(self, date_from=None, date_to=None, limit=None, force=False,
              mock_rows=None):
        """Call get_tournaments_to_scrape with mocked DB cursor."""
        from db import get_tournaments_to_scrape

        cur = MagicMock()
        cur.fetchall.return_value = mock_rows or []

        @contextmanager
        def fake_cursor():
            yield cur

        with patch(f"{MODULE_DB}.get_cursor", side_effect=fake_cursor):
            result = get_tournaments_to_scrape(date_from, date_to, limit, force=force)
        return result, cur

    def _sql(self, cur):
        return cur.execute.call_args.args[0]

    def _params(self, cur):
        return cur.execute.call_args.args[1]

    # ── Normal mode skip logic ──────────────────────────────────────

    def test_normal_excludes_completed_success(self):
        """Normal mode SQL has NOT(... SUCCESS ... Completed/Cancelled)."""
        _, cur = self._call(force=False)
        sql = self._sql(cur)
        self.assertIn("NOT", sql)
        self.assertIn("SUCCESS", sql)
        self.assertIn("Completed", sql)

    def test_normal_excludes_cancelled_success(self):
        """Normal mode SQL excludes Cancelled + SUCCESS."""
        _, cur = self._call(force=False)
        sql = self._sql(cur)
        self.assertIn("Cancelled", sql)

    def test_normal_includes_failed(self):
        """Normal mode SQL does NOT exclude FAILED status."""
        _, cur = self._call(force=False)
        sql = self._sql(cur)
        # The NOT clause only excludes SUCCESS, so FAILED should not appear
        # in an exclusion condition
        self.assertNotIn("FAILED", sql)

    def test_normal_includes_null_status(self):
        """Normal mode SQL does NOT exclude NULL registration_status."""
        _, cur = self._call(force=False)
        sql = self._sql(cur)
        # COALESCE handles NULL, and the NOT clause only excludes specific values
        self.assertIn("COALESCE", sql)

    # ── Force mode ──────────────────────────────────────────────────

    def test_force_filters_completed_only(self):
        """Force mode SQL targets registration_status = 'Completed'."""
        _, cur = self._call(force=True)
        sql = self._sql(cur)
        self.assertIn("Completed", sql)
        # Force mode should NOT have the NOT exclusion clause
        self.assertNotIn("NOT (", sql)

    def test_force_excludes_cancelled(self):
        """Force mode only matches 'Completed', not 'Cancelled'."""
        _, cur = self._call(force=True)
        sql = self._sql(cur)
        # Force SQL uses = 'Completed', not IN ('Completed', 'Cancelled')
        self.assertNotIn("Cancelled", sql)

    # ── Date range ──────────────────────────────────────────────────

    def test_date_range_params(self):
        """date_from and date_to are passed correctly."""
        _, cur = self._call(date_from="2025-01-01", date_to="2025-12-31")
        params = self._params(cur)
        self.assertEqual(params[0], "2025-01-01")  # from
        self.assertEqual(params[1], "2025-01-01")  # from (duplicated)
        self.assertEqual(params[2], "2025-12-31")  # to
        self.assertEqual(params[3], "2025-12-31")  # to (duplicated)

    def test_no_date_range(self):
        """Both None → NULL-safe filter params."""
        _, cur = self._call(date_from=None, date_to=None)
        params = self._params(cur)
        self.assertEqual(params, [None, None, None, None])

    # ── Limit ───────────────────────────────────────────────────────

    def test_limit_applied(self):
        """limit=10 → SQL has LIMIT clause."""
        _, cur = self._call(limit=10)
        sql = self._sql(cur)
        self.assertIn("LIMIT", sql)
        params = self._params(cur)
        self.assertIn(10, params)

    def test_no_limit(self):
        """limit=None → no LIMIT clause."""
        _, cur = self._call(limit=None)
        sql = self._sql(cur)
        self.assertNotIn("LIMIT", sql)

    # ── Return shape ────────────────────────────────────────────────

    def test_returns_dicts_with_keys(self):
        """Result items have expected keys."""
        sample_row = {
            "id": 1, "tournament_id": "UUID-123", "org_slug": "test-club",
            "name": "Test Tournament", "start_date": "2025-06-01",
            "end_date": "2025-06-03", "detail_scraped_at": None,
            "detail_scrape_status": None,
        }
        result, _ = self._call(mock_rows=[sample_row])
        self.assertEqual(len(result), 1)
        for key in ("id", "tournament_id", "org_slug", "name", "start_date", "end_date"):
            self.assertIn(key, result[0])


if __name__ == "__main__":
    unittest.main()
