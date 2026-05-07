"""Parse match result DOM elements into structured dicts, including set/tiebreak scores."""
from __future__ import annotations

import logging
import re

from playwright.sync_api import Page

logger = logging.getLogger(__name__)


def parse_match_results(page: Page) -> list[dict]:
    """
    Parse match results from a player profile 'results' tab.
    Returns a list of match dicts, each containing a nested 'sets' list.
    """
    matches = []

    rows = page.locator(
        "table tbody tr, .match-row, .result-row, [class*='matchRow'], "
        "[class*='resultRow'], [class*='match-card']"
    )
    count = rows.count()
    logger.info("Found %d match result rows", count)

    for i in range(count):
        row = rows.nth(i)
        try:
            match = _parse_match_row(row)
            if match:
                matches.append(match)
        except Exception as e:
            logger.warning("Failed to parse match row %d: %s", i, e)

    return matches


def parse_tournament_matches(page: Page) -> list[dict]:
    """
    Parse match results from a tournament detail 'results' tab.
    Returns a list of match dicts grouped by division/round.
    """
    matches = []

    # Look for result sections per division
    sections = page.locator(
        ".draw-results, [class*='drawResults'], [class*='bracketResults'], "
        ".result-section, [class*='resultSection']"
    )

    if sections.count() > 0:
        for i in range(sections.count()):
            section = sections.nth(i)
            division = _get_section_division(section)
            rows = section.locator(
                "tr, .match-row, [class*='matchRow'], [class*='match-card']"
            )
            for j in range(rows.count()):
                match = _parse_match_row(rows.nth(j))
                if match:
                    match["division_name"] = division
                    matches.append(match)
    else:
        # Fallback: parse all match rows on the page
        rows = page.locator(
            "table tbody tr, .match-row, [class*='matchRow'], [class*='match-card']"
        )
        for i in range(rows.count()):
            match = _parse_match_row(rows.nth(i))
            if match:
                matches.append(match)

    return matches


def _get_section_division(section) -> str:
    """Get the division name from a result section header."""
    try:
        header = section.locator("h2, h3, h4, .draw-title, [class*='drawName']").first
        return header.inner_text().strip()
    except Exception:
        return "Unknown Division"


def _parse_match_row(row) -> dict | None:
    """Parse a single match row element into a match dict."""
    text = row.inner_text().strip()
    if not text or len(text) < 5:
        return None

    match = {
        "division_name": None,
        "round": None,
        "match_type": "SINGLES",
        "player1_name": None,
        "player2_name": None,
        "opponent1_name": None,
        "opponent2_name": None,
        "winner_side": None,
        "win_type": "COMPLETED",
        "score_summary": None,
        "sets": [],
        "raw_text": text,
    }

    lines = [l.strip() for l in text.split("\n") if l.strip()]

    # Try to extract round
    match["round"] = _extract_round(text)

    # Try to extract score
    score_text = _find_score_text(text)
    if score_text:
        match["score_summary"] = score_text
        match["sets"] = parse_score_to_sets(score_text)

    # Try to extract player names
    _extract_player_names(lines, match)

    # Detect win type
    match["win_type"] = _detect_win_type(text)

    # Detect doubles
    if _is_doubles(text):
        match["match_type"] = "DOUBLES"

    return match


def _extract_round(text: str) -> str | None:
    """Extract match round from text."""
    round_patterns = [
        (r"\bFinal\b", "F"),
        (r"\bSemifinal\b|\bSF\b|\bSemi-?Final\b", "SF"),
        (r"\bQuarterfinal\b|\bQF\b|\bQuarter-?Final\b", "QF"),
        (r"\bR(?:ound\s*(?:of\s*)?)?16\b", "R16"),
        (r"\bR(?:ound\s*(?:of\s*)?)?32\b", "R32"),
        (r"\bR(?:ound\s*(?:of\s*)?)?64\b", "R64"),
        (r"\bR(?:ound\s*(?:of\s*)?)?128\b", "R128"),
        (r"\b1st\s*Round\b|\bR1\b|\bFirst\s*Round\b", "R1"),
        (r"\b2nd\s*Round\b|\bR2\b|\bSecond\s*Round\b", "R2"),
        (r"\b3rd\s*Round\b|\bR3\b|\bThird\s*Round\b", "R3"),
        (r"\bConsolation\b", "CON"),
        (r"\bRR\b|\bRound\s*Robin\b", "RR"),
    ]
    for pattern, label in round_patterns:
        if re.search(pattern, text, re.IGNORECASE):
            return label
    return None


def _find_score_text(text: str) -> str | None:
    """
    Find a tennis score pattern in text, like '6-3, 4-6, 7-6(5)'
    or '6-3 4-6 7-6(5)'.
    """
    # Match patterns like "6-3, 4-6, 7-6(5)" or "6-3 4-6 7-6(5)"
    score_pattern = re.compile(
        r"(\d{1,2}-\d{1,2}(?:\(\d{1,2}\))?(?:[,\s]+\d{1,2}-\d{1,2}(?:\(\d{1,2}\))?)*)"
    )
    match = score_pattern.search(text)
    if match:
        return match.group(1).strip()
    return None


def parse_score_to_sets(score_text: str) -> list[dict]:
    """
    Parse a score summary string into individual set dicts.
    e.g. '6-3, 4-6, 7-6(5)' → [
        {set_number: 1, player_games: 6, opponent_games: 3, tiebreak_player: None, tiebreak_opponent: None},
        {set_number: 2, player_games: 4, opponent_games: 6, tiebreak_player: None, tiebreak_opponent: None},
        {set_number: 3, player_games: 7, opponent_games: 6, tiebreak_player: 7, tiebreak_opponent: 5},
    ]
    """
    sets = []
    # Split by comma or whitespace
    set_texts = re.split(r"[,\s]+", score_text.strip())

    for idx, set_text in enumerate(set_texts):
        set_text = set_text.strip()
        if not set_text:
            continue

        # Pattern: "6-3" or "7-6(5)" or "7-6(12-10)"
        m = re.match(r"(\d{1,2})-(\d{1,2})(?:\((\d{1,2})(?:-(\d{1,2}))?\))?", set_text)
        if not m:
            continue

        player_games = int(m.group(1))
        opponent_games = int(m.group(2))
        tb_player = None
        tb_opponent = None

        if m.group(3) is not None:
            # Tiebreak score present
            if m.group(4) is not None:
                # Full tiebreak like 7-6(12-10) — both sides
                tb_player = int(m.group(3))
                tb_opponent = int(m.group(4))
            else:
                # Single number like 7-6(5) — this is the loser's score
                loser_tb = int(m.group(3))
                # The winner of the set won the tiebreak
                if player_games > opponent_games:
                    tb_opponent = loser_tb
                    tb_player = loser_tb + 2 if loser_tb < 6 else loser_tb + 2
                    # Standard tiebreak: winner needs loser+2 with min 7
                    tb_player = max(7, loser_tb + 2)
                else:
                    tb_player = loser_tb
                    tb_opponent = max(7, loser_tb + 2)

        sets.append({
            "set_number": idx + 1,
            "player_games": player_games,
            "opponent_games": opponent_games,
            "tiebreak_player": tb_player,
            "tiebreak_opponent": tb_opponent,
        })

    return sets


def _extract_player_names(lines: list[str], match: dict):
    """Try to extract player/opponent names from text lines."""
    # Look for "vs" or "def." separators
    full_text = " ".join(lines)
    vs_match = re.search(r"(.+?)\s+(?:vs\.?|defeated|def\.?|lost\s+to)\s+(.+?)(?:\s+\d+-\d+|$)", full_text, re.IGNORECASE)
    if vs_match:
        match["player1_name"] = vs_match.group(1).strip()
        match["opponent1_name"] = vs_match.group(2).strip()
        return

    # Fallback: first two name-like strings in lines
    name_like = [l for l in lines if re.match(r"^[A-Za-z\s,.-]+$", l) and len(l) > 2]
    if len(name_like) >= 2:
        match["player1_name"] = name_like[0]
        match["opponent1_name"] = name_like[1]
    elif len(name_like) == 1:
        match["opponent1_name"] = name_like[0]


def _detect_win_type(text: str) -> str:
    """Detect how the match was won."""
    text_lower = text.lower()
    if "retired" in text_lower or "ret." in text_lower:
        return "RETIRED"
    if "default" in text_lower or "def." in text_lower:
        return "DEFAULT"
    if "walkover" in text_lower or "w/o" in text_lower or "w.o." in text_lower:
        return "WALKOVER"
    return "COMPLETED"


def _is_doubles(text: str) -> bool:
    """Detect if the match is doubles based on text content."""
    # Look for doubles indicators: two names separated by "/" or "&"
    if re.search(r"\w+\s*/\s*\w+\s+(?:vs|def|lost)", text, re.IGNORECASE):
        return True
    if "doubles" in text.lower():
        return True
    return False
