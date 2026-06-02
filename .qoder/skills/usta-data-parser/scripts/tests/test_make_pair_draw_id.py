"""Unit tests for _make_pair_draw_id — deterministic draw ID generation.

Tests the pure function that generates consistent draw IDs for doubles pairs
from sorted player identifiers (USTA ID or name fallback).
"""
import re
import sys
import unittest
from pathlib import Path

# Ensure parent scripts/ is importable
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from pages.tournament_detail_graphql import _make_pair_draw_id


class TestMakePairDrawId(unittest.TestCase):

    def _player(self, usta_id=None, first="John", last="Smith"):
        """Helper to build a player dict matching the Tournaments API schema."""
        cid = {"key": "ustaId", "value": usta_id} if usta_id else {}
        return {"customId": cid, "firstName": first, "lastName": last}

    def test_deterministic_order(self):
        """Same 2 players in different array orders → same draw_id."""
        p1 = self._player("1000001", "Alice", "Racic")
        p2 = self._player("1000002", "Bob", "Yamamoto")
        self.assertEqual(
            _make_pair_draw_id("evt-1", [p1, p2]),
            _make_pair_draw_id("evt-1", [p2, p1]),
        )

    def test_uses_usta_id(self):
        """Draw ID is based on sorted UAIDs when both players have them."""
        p1 = self._player("1000001", "Alice", "Racic")
        p2 = self._player("1000002", "Bob", "Yamamoto")
        result = _make_pair_draw_id("evt-1", [p1, p2])
        # Should be deterministic hex string
        self.assertTrue(re.fullmatch(r"[0-9a-f]{12}", result))

    def test_fallback_to_name(self):
        """Falls back to firstName_lastName when no customId."""
        p1 = {"customId": {}, "firstName": "Alice", "lastName": "Racic"}
        p2 = {"customId": {}, "firstName": "Bob", "lastName": "Yamamoto"}
        result = _make_pair_draw_id("evt-1", [p1, p2])
        self.assertTrue(re.fullmatch(r"[0-9a-f]{12}", result))
        # Verify name-based: should match explicit construction
        p1b = {"customId": {"key": "other", "value": "x"}, "firstName": "Alice", "lastName": "Racic"}
        self.assertEqual(result, _make_pair_draw_id("evt-1", [p1b, p2]))

    def test_mixed_identifiers(self):
        """Player 1 has UAID, Player 2 only name — sorts mix correctly."""
        p1 = self._player("1000001", "Alice", "Racic")
        p2 = {"customId": {}, "firstName": "Bob", "lastName": "Yamamoto"}
        result = _make_pair_draw_id("evt-1", [p1, p2])
        # Order independence
        self.assertEqual(result, _make_pair_draw_id("evt-1", [p2, p1]))

    def test_different_events_different_ids(self):
        """Same pair, different event_id → different draw IDs."""
        p1 = self._player("1000001", "Alice", "Racic")
        p2 = self._player("1000002", "Bob", "Yamamoto")
        self.assertNotEqual(
            _make_pair_draw_id("evt-1", [p1, p2]),
            _make_pair_draw_id("evt-2", [p1, p2]),
        )

    def test_returns_12_char_hex(self):
        """Result is always a 12-character hex string."""
        p1 = self._player("9999999", "Z", "Z")
        p2 = self._player("0000000", "A", "A")
        result = _make_pair_draw_id("event-xyz", [p1, p2])
        self.assertEqual(len(result), 12)
        self.assertTrue(re.fullmatch(r"[0-9a-f]{12}", result))

    def test_empty_custom_id_value(self):
        """customId.key == 'ustaId' but value == '' falls back to name."""
        p1 = {"customId": {"key": "ustaId", "value": ""}, "firstName": "Alice", "lastName": "Racic"}
        p2 = {"customId": {}, "firstName": "Bob", "lastName": "Yamamoto"}
        result = _make_pair_draw_id("evt-1", [p1, p2])
        # Should match the name-based result
        p1_name = {"customId": {}, "firstName": "Alice", "lastName": "Racic"}
        self.assertEqual(result, _make_pair_draw_id("evt-1", [p1_name, p2]))


if __name__ == "__main__":
    unittest.main()
