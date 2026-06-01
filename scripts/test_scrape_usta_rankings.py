#!/usr/bin/env python3
"""Tests for scrape_usta_rankings.py.

Two test categories:
  1. Unit tests (mocked) - fast, no network, run by default
  2. Integration tests  - hit the real API, marked with @integration
"""
from __future__ import annotations

import json
import os
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import MagicMock, patch

# Ensure the scripts directory is on the path
sys.path.insert(0, str(Path(__file__).resolve().parent))

import scrape_usta_rankings as scraper

# ---------------------------------------------------------------------------
# Sample API response fixture
# ---------------------------------------------------------------------------

SAMPLE_API_RESPONSE = {
    "catalogId": "JUNIOR_NULL_M_STANDING_Y12_UNDER_NULL_NULL_NULL",
    "publishDate": "2026-05-13T09:43:14.027Z",
    "displayLabel": "Boys' 12 National Standings List (combined)",
    "listType": "STANDING",
    "playerType": "JUNIOR",
    "rankListGender": "M",
    "rankListGenderModifier": None,
    "playerLevel": "NULL",
    "ageRestriction": "Y12",
    "ageRestrictionModifier": "UNDER",
    "matchFormat": None,
    "matchFormatType": None,
    "familyCategory": None,
    "data": [
        {
            "name": "Test Player One",
            "uaid": "1000000001",
            "city": "New York",
            "state": "NY",
            "trend": "no change",
            "points": 5000,
            "rank": {"national": 1, "section": 1, "district": 1},
            "section": {"name": "Eastern"},
            "district": {"name": "Metro Region"},
            "pointsRecord": {
                "singlesPoints": 3000,
                "doublesPoints": 2500,
                "bonusPoints": 1000,
            },
        },
        {
            "name": "Test Player Two",
            "uaid": "1000000002",
            "city": "Los Angeles",
            "state": "CA",
            "trend": "up",
            "points": 4500,
            "rank": {"national": 2, "section": 1, "district": 1},
            "section": {"name": "Southern California"},
            "district": {"name": "Southern California"},
            "pointsRecord": {
                "singlesPoints": 2500,
                "doublesPoints": 2000,
                "bonusPoints": 800,
            },
        },
    ],
    "pagination": {
        "currentPage": 1,
        "pageSize": 100,
        "totalPages": 1,
        "totalResults": 2,
    },
}


def _make_response(
    gender: str = "M",
    age: str = "Y12",
    page: int = 1,
    total_pages: int = 1,
    num_players: int = 2,
) -> dict:
    """Create a sample API response with configurable parameters."""
    resp = json.loads(json.dumps(SAMPLE_API_RESPONSE))  # deep copy
    resp["rankListGender"] = gender
    resp["ageRestriction"] = age
    resp["pagination"]["currentPage"] = page
    resp["pagination"]["totalPages"] = total_pages
    resp["pagination"]["totalResults"] = num_players
    resp["data"] = resp["data"][:num_players]
    return resp


# ---------------------------------------------------------------------------
# Unit tests (mocked, no network)
# ---------------------------------------------------------------------------


class TestBuildRequestBody(unittest.TestCase):
    def test_default_values(self):
        body = scraper.build_request_body("M", "Y12")
        self.assertEqual(body["selection"]["rankListGender"], "M")
        self.assertEqual(body["selection"]["ageRestriction"], "Y12")
        self.assertEqual(body["selection"]["ageRestrictionModifier"], "UNDER")
        self.assertEqual(body["selection"]["playerType"], "JUNIOR")
        self.assertEqual(body["selection"]["listType"], "STANDING")
        self.assertEqual(body["pagination"]["pageSize"], 100)
        self.assertEqual(body["pagination"]["currentPage"], 1)

    def test_custom_page(self):
        body = scraper.build_request_body("F", "Y18", page=5, page_size=50)
        self.assertEqual(body["selection"]["rankListGender"], "F")
        self.assertEqual(body["selection"]["ageRestriction"], "Y18")
        self.assertEqual(body["pagination"]["currentPage"], 5)
        self.assertEqual(body["pagination"]["pageSize"], 50)

    def test_all_genders(self):
        for g in scraper.GENDERS:
            body = scraper.build_request_body(g, "Y14")
            self.assertIn(body["selection"]["rankListGender"], ["M", "F"])

    def test_all_age_groups(self):
        for age in scraper.AGE_GROUPS:
            body = scraper.build_request_body("M", age)
            self.assertIn(body["selection"]["ageRestriction"], scraper.AGE_GROUPS)


class TestFetchRankingsPage(unittest.TestCase):
    @patch("scrape_usta_rankings.urllib.request.urlopen")
    def test_successful_fetch(self, mock_urlopen):
        mock_resp = MagicMock()
        mock_resp.read.return_value = json.dumps(SAMPLE_API_RESPONSE).encode()
        mock_resp.__enter__ = lambda s: s
        mock_resp.__exit__ = MagicMock(return_value=False)
        mock_urlopen.return_value = mock_resp

        result = scraper.fetch_rankings_page("M", "Y12")
        self.assertEqual(result["catalogId"], SAMPLE_API_RESPONSE["catalogId"])
        self.assertEqual(len(result["data"]), 2)
        mock_urlopen.assert_called_once()

    @patch("scrape_usta_rankings.urllib.request.urlopen")
    def test_retry_on_failure(self, mock_urlopen):
        mock_urlopen.side_effect = [
            OSError("connection reset"),
            OSError("connection reset"),
            self._mock_success_response(),
        ]
        # Patch sleep to speed up
        with patch("scrape_usta_rankings.time.sleep"):
            result = scraper.fetch_rankings_page("M", "Y12")
        self.assertEqual(len(result["data"]), 2)
        self.assertEqual(mock_urlopen.call_count, 3)

    @patch("scrape_usta_rankings.urllib.request.urlopen")
    def test_raises_after_max_retries(self, mock_urlopen):
        mock_urlopen.side_effect = OSError("connection reset")
        with patch("scrape_usta_rankings.time.sleep"):
            with self.assertRaises(RuntimeError) as ctx:
                scraper.fetch_rankings_page("M", "Y12")
            self.assertIn("Failed to fetch", str(ctx.exception))
        self.assertEqual(mock_urlopen.call_count, scraper.MAX_RETRIES)

    def _mock_success_response(self):
        mock_resp = MagicMock()
        mock_resp.read.return_value = json.dumps(SAMPLE_API_RESPONSE).encode()
        mock_resp.__enter__ = lambda s: s
        mock_resp.__exit__ = MagicMock(return_value=False)
        return mock_resp


class TestFetchAllRankings(unittest.TestCase):
    @patch("scrape_usta_rankings.fetch_rankings_page")
    def test_single_page(self, mock_fetch):
        mock_fetch.return_value = _make_response(total_pages=1, num_players=2)
        result = scraper.fetch_all_rankings("M", "Y12")
        self.assertEqual(len(result["players"]), 2)
        self.assertEqual(result["metadata"]["totalPages"], 1)
        mock_fetch.assert_called_once()

    @patch("scrape_usta_rankings.fetch_rankings_page")
    @patch("scrape_usta_rankings.time.sleep")
    def test_multi_page(self, mock_sleep, mock_fetch):
        page1 = _make_response(total_pages=3, num_players=2)
        page2 = _make_response(total_pages=3, num_players=2)
        page2["pagination"]["currentPage"] = 2
        page3 = _make_response(total_pages=3, num_players=1)
        page3["pagination"]["currentPage"] = 3
        mock_fetch.side_effect = [page1, page2, page3]

        result = scraper.fetch_all_rankings("M", "Y12")
        self.assertEqual(len(result["players"]), 5)  # 2 + 2 + 1
        self.assertEqual(mock_fetch.call_count, 3)

    @patch("scrape_usta_rankings.fetch_rankings_page")
    def test_max_pages_limit(self, mock_fetch):
        mock_fetch.return_value = _make_response(total_pages=10, num_players=2)
        result = scraper.fetch_all_rankings("M", "Y12", max_pages=2)
        self.assertEqual(mock_fetch.call_count, 2)

    @patch("scrape_usta_rankings.fetch_rankings_page")
    def test_empty_first_page(self, mock_fetch):
        resp = _make_response()
        resp["data"] = []
        mock_fetch.return_value = resp
        result = scraper.fetch_all_rankings("M", "Y12")
        self.assertEqual(len(result["players"]), 0)

    @patch("scrape_usta_rankings.fetch_rankings_page")
    def test_metadata_populated(self, mock_fetch):
        mock_fetch.return_value = _make_response()
        result = scraper.fetch_all_rankings("M", "Y12")
        meta = result["metadata"]
        self.assertEqual(meta["rankListGender"], "M")
        self.assertEqual(meta["ageRestriction"], "Y12")
        self.assertIn("catalogId", meta)
        self.assertIn("displayLabel", meta)
        self.assertIn("totalResults", meta)


class TestScrapeAllRankings(unittest.TestCase):
    @patch("scrape_usta_rankings.fetch_all_rankings")
    @patch("scrape_usta_rankings.time.sleep")
    def test_scrape_single_combo(self, mock_sleep, mock_fetch_all):
        mock_fetch_all.return_value = {
            "metadata": {
                "catalogId": "JUNIOR_NULL_M_STANDING_Y12_UNDER_NULL_NULL_NULL",
                "displayLabel": "Boys' 12",
                "totalResults": 2,
            },
            "players": SAMPLE_API_RESPONSE["data"],
        }

        with tempfile.TemporaryDirectory() as tmpdir:
            results = scraper.scrape_all_rankings(
                genders=["M"],
                age_groups=["Y12"],
                output_dir=tmpdir,
            )

            self.assertIn("Boys_Y12", results)
            filepath = results["Boys_Y12"]
            self.assertTrue(Path(filepath).exists())

            with open(filepath) as f:
                data = json.load(f)
            self.assertEqual(len(data["players"]), 2)
            self.assertIn("metadata", data)
            self.assertIn("scrape_timestamp", data)

            # Check summary file
            summary_path = Path(tmpdir) / "rankings_summary.json"
            self.assertTrue(summary_path.exists())
            with open(summary_path) as f:
                summary = json.load(f)
            self.assertEqual(len(summary["combinations"]), 1)

    @patch("scrape_usta_rankings.fetch_all_rankings")
    @patch("scrape_usta_rankings.time.sleep")
    def test_scrape_all_combos(self, mock_sleep, mock_fetch_all):
        mock_fetch_all.return_value = {
            "metadata": {"totalResults": 1},
            "players": [SAMPLE_API_RESPONSE["data"][0]],
        }

        with tempfile.TemporaryDirectory() as tmpdir:
            results = scraper.scrape_all_rankings(output_dir=tmpdir)
            # 2 genders x 4 age groups = 8 combos
            self.assertEqual(len(results), 8)
            for label, path in results.items():
                self.assertNotEqual(path, "ERROR")
                self.assertTrue(Path(path).exists())

    @patch("scrape_usta_rankings.fetch_all_rankings")
    @patch("scrape_usta_rankings.time.sleep")
    def test_handles_error(self, mock_sleep, mock_fetch_all):
        mock_fetch_all.side_effect = RuntimeError("API error")

        with tempfile.TemporaryDirectory() as tmpdir:
            results = scraper.scrape_all_rankings(
                genders=["M"],
                age_groups=["Y12"],
                output_dir=tmpdir,
            )
            self.assertEqual(results["Boys_Y12"], "ERROR")


class TestParseArgs(unittest.TestCase):
    def test_defaults(self):
        args = scraper.parse_args([])
        self.assertIsNone(args.gender)
        self.assertIsNone(args.age)
        self.assertEqual(args.output, scraper.DEFAULT_OUTPUT_DIR)
        self.assertIsNone(args.max_pages)
        self.assertFalse(args.verbose)

    def test_gender_flag(self):
        args = scraper.parse_args(["--gender", "M"])
        self.assertEqual(args.gender, ["M"])

    def test_multiple_genders(self):
        args = scraper.parse_args(["-g", "M", "-g", "F"])
        self.assertEqual(args.gender, ["M", "F"])

    def test_age_flag(self):
        args = scraper.parse_args(["--age", "14"])
        self.assertEqual(args.age, ["14"])

    def test_multiple_ages(self):
        args = scraper.parse_args(["-a", "12", "-a", "18"])
        self.assertEqual(args.age, ["12", "18"])

    def test_output_dir(self):
        args = scraper.parse_args(["--output", "/tmp/results"])
        self.assertEqual(args.output, "/tmp/results")

    def test_max_pages(self):
        args = scraper.parse_args(["--max-pages", "3"])
        self.assertEqual(args.max_pages, 3)

    def test_verbose(self):
        args = scraper.parse_args(["-v"])
        self.assertTrue(args.verbose)


class TestConstants(unittest.TestCase):
    def test_genders(self):
        self.assertEqual(scraper.GENDERS, ["M", "F"])

    def test_age_groups(self):
        self.assertEqual(scraper.AGE_GROUPS, ["Y12", "Y14", "Y16", "Y18"])

    def test_gender_labels(self):
        self.assertEqual(scraper.GENDER_LABELS["M"], "Boys")
        self.assertEqual(scraper.GENDER_LABELS["F"], "Girls")

    def test_api_url(self):
        self.assertIn("rankings/search/public", scraper.API_URL)


class TestOutputFileFormat(unittest.TestCase):
    """Verify the structure of output JSON files."""

    @patch("scrape_usta_rankings.fetch_all_rankings")
    @patch("scrape_usta_rankings.time.sleep")
    def test_output_structure(self, mock_sleep, mock_fetch_all):
        mock_fetch_all.return_value = {
            "metadata": {
                "catalogId": "JUNIOR_NULL_F_STANDING_Y14_UNDER_NULL_NULL_NULL",
                "displayLabel": "Girls' 14",
                "publishDate": "2026-05-13T09:43:14.027Z",
                "listType": "STANDING",
                "playerType": "JUNIOR",
                "rankListGender": "F",
                "ageRestriction": "Y14",
                "ageRestrictionModifier": "UNDER",
                "matchFormat": None,
                "matchFormatType": None,
                "familyCategory": None,
                "totalPages": 1,
                "totalResults": 1,
                "pageSize": 100,
            },
            "players": [SAMPLE_API_RESPONSE["data"][0]],
        }

        with tempfile.TemporaryDirectory() as tmpdir:
            scraper.scrape_all_rankings(
                genders=["F"], age_groups=["Y14"], output_dir=tmpdir,
            )

            filepath = Path(tmpdir) / "rankings_F_Y14.json"
            self.assertTrue(filepath.exists())

            with open(filepath) as f:
                data = json.load(f)

            # Top-level keys
            self.assertIn("metadata", data)
            self.assertIn("players", data)
            self.assertIn("scrape_timestamp", data)

            # Player structure
            player = data["players"][0]
            self.assertIn("name", player)
            self.assertIn("uaid", player)
            self.assertIn("city", player)
            self.assertIn("state", player)
            self.assertIn("points", player)
            self.assertIn("rank", player)
            self.assertIn("national", player["rank"])
            self.assertIn("section", player["rank"])
            self.assertIn("district", player["rank"])
            self.assertIn("pointsRecord", player)


# ---------------------------------------------------------------------------
# Integration tests (hit real API)
# ---------------------------------------------------------------------------

def integration(test_func):
    """Decorator to mark tests that require network access."""
    return unittest.skipUnless(
        os.environ.get("USTA_INTEGRATION_TESTS", "").lower() in ("1", "true", "yes"),
        "Set USTA_INTEGRATION_TESTS=1 to run integration tests",
    )(test_func)


class TestIntegration(unittest.TestCase):
    """Tests that hit the real USTA API. Run with USTA_INTEGRATION_TESTS=1."""

    @integration
    def test_fetch_single_page(self):
        result = scraper.fetch_rankings_page("M", "Y12", page=1, page_size=5)

        # Verify response structure
        self.assertIn("data", result)
        self.assertIn("pagination", result)
        self.assertIn("catalogId", result)
        self.assertIn("displayLabel", result)

        # Verify pagination
        pagination = result["pagination"]
        self.assertEqual(pagination["currentPage"], 1)
        self.assertEqual(pagination["pageSize"], 5)
        self.assertGreater(pagination["totalPages"], 0)
        self.assertGreater(pagination["totalResults"], 0)

        # Verify data
        players = result["data"]
        self.assertEqual(len(players), 5)
        for player in players:
            self.assertIn("name", player)
            self.assertIn("uaid", player)
            self.assertIn("points", player)
            self.assertIn("rank", player)

    @integration
    def test_fetch_all_genders_and_ages(self):
        """Verify API returns data for all gender/age combos (1 page each)."""
        for gender in scraper.GENDERS:
            for age in scraper.AGE_GROUPS:
                with self.subTest(gender=gender, age=age):
                    result = scraper.fetch_rankings_page(
                        gender, age, page=1, page_size=5,
                    )
                    self.assertGreater(len(result.get("data", [])), 0,
                                       f"No data for {gender} {age}")
                    self.assertEqual(result["rankListGender"], gender)
                    self.assertEqual(result["ageRestriction"], age)

    @integration
    def test_full_scrape_small(self):
        """Scrape 1 page of 1 combo to verify end-to-end."""
        with tempfile.TemporaryDirectory() as tmpdir:
            results = scraper.scrape_all_rankings(
                genders=["M"],
                age_groups=["Y12"],
                max_pages=1,
                output_dir=tmpdir,
            )
            filepath = results["Boys_Y12"]
            self.assertTrue(Path(filepath).exists())
            with open(filepath) as f:
                data = json.load(f)
            self.assertGreater(len(data["players"]), 0)
            self.assertLessEqual(len(data["players"]), 100)


if __name__ == "__main__":
    unittest.main()
