"""Unit tests for date range resolution in scrape_tournament_details_batch.

Covers: year defaults, explicit date overrides, partial overrides,
force and limit passthrough.
"""
import sys
import unittest
from pathlib import Path
from unittest.mock import MagicMock, patch

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

MODULE = "scraper_main"


class TestDateRangeResolution(unittest.TestCase):

    def _call(self, year=None, date_from=None, date_to=None,
              limit=None, force=False):
        """Call scrape_tournament_details_batch with mocked internals."""
        from scraper_main import scrape_tournament_details_batch

        captured = {}

        def fake_get_tournaments(df, dt, lim, force=False):
            captured["date_from"] = df
            captured["date_to"] = dt
            captured["limit"] = lim
            captured["force"] = force
            return []  # No tournaments to process

        with patch("db.get_tournaments_to_scrape", side_effect=fake_get_tournaments), \
             patch("db.create_scrape_job", return_value=1), \
             patch("db.update_scrape_job"):
            scrape_tournament_details_batch(
                year=year, date_from=date_from, date_to=date_to,
                limit=limit, delay=0, force=force,
            )
        return captured

    def test_year_sets_defaults(self):
        """year=2025, no explicit dates → 2025-01-01 to 2025-12-31."""
        c = self._call(year=2025)
        self.assertEqual(c["date_from"], "2025-01-01")
        self.assertEqual(c["date_to"], "2025-12-31")

    def test_explicit_overrides_year(self):
        """year=2025 + explicit dates → explicit dates used."""
        c = self._call(year=2025, date_from="2025-03-01", date_to="2025-06-30")
        self.assertEqual(c["date_from"], "2025-03-01")
        self.assertEqual(c["date_to"], "2025-06-30")

    def test_partial_override_from_only(self):
        """year=2025 + date_from only → from overridden, to defaults."""
        c = self._call(year=2025, date_from="2025-06-01", date_to=None)
        self.assertEqual(c["date_from"], "2025-06-01")
        self.assertEqual(c["date_to"], "2025-12-31")

    def test_partial_override_to_only(self):
        """year=2025 + date_to only → from defaults, to overridden."""
        c = self._call(year=2025, date_from=None, date_to="2025-03-31")
        self.assertEqual(c["date_from"], "2025-01-01")
        self.assertEqual(c["date_to"], "2025-03-31")

    def test_no_year_no_dates(self):
        """All None → both dates passed as None."""
        c = self._call()
        self.assertIsNone(c["date_from"])
        self.assertIsNone(c["date_to"])

    def test_force_passed_through(self):
        """force=True forwarded to get_tournaments_to_scrape."""
        c = self._call(year=2025, force=True)
        self.assertTrue(c["force"])

    def test_limit_passed_through(self):
        """limit=50 forwarded to get_tournaments_to_scrape."""
        c = self._call(year=2025, limit=50)
        self.assertEqual(c["limit"], 50)


if __name__ == "__main__":
    unittest.main()
