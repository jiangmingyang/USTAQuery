"""Configuration loaded from environment variables / .env file."""

import os
from pathlib import Path
from dotenv import load_dotenv

# Load .env from project root
_project_root = Path(__file__).resolve().parent.parent.parent.parent.parent
load_dotenv(_project_root / ".env")

# ── MySQL ──────────────────────────────────────────────────────────────
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = int(os.getenv("DB_PORT", "3306"))
DB_NAME = os.getenv("DB_NAME", "usta_tennis")
DB_USER = os.getenv("DB_USER", "usta_app")
DB_PASSWORD = os.getenv("DB_PASSWORD", "usta_password")

# ── USTA Credentials ──────────────────────────────────────────────────
USTA_EMAIL = os.getenv("USTA_EMAIL", "")
USTA_PASSWORD = os.getenv("USTA_PASSWORD", "")

# ── Scraper Behaviour ─────────────────────────────────────────────────
HEADLESS = os.getenv("SCRAPE_HEADLESS", "true").lower() == "true"
PAGE_DELAY = float(os.getenv("SCRAPE_PAGE_DELAY", "2.5"))
TIMEOUT = int(os.getenv("SCRAPE_TIMEOUT", "60000"))
RETRIES = int(os.getenv("SCRAPE_RETRIES", "3"))
DEBUG_DIR = Path(os.getenv("SCRAPE_DEBUG_DIR", "~/.usta-scraper/debug")).expanduser()
STATE_DIR = Path(os.getenv("SCRAPE_STATE_DIR", "~/.usta-scraper/browser-state")).expanduser()
RANKINGS_PAGE_DELAY = float(os.getenv("RANKINGS_PAGE_DELAY", "0.5"))
RANKINGS_GROUP_DELAY = float(os.getenv("RANKINGS_GROUP_DELAY", "3.0"))
BATCH_PLAYER_DELAY = float(os.getenv("BATCH_PLAYER_DELAY", "5.0"))
CONCURRENCY = int(os.getenv("SCRAPER_CONCURRENCY", "4"))
DB_POOL_SIZE = int(os.getenv("DB_POOL_SIZE", "0"))  # 0 = auto (CONCURRENCY + 2)

# ── Scheduler Intervals (seconds) ────────────────────────────────────
SCHEDULE_PLAYER_REFRESH = int(os.getenv("SCHEDULE_PLAYER_REFRESH", "21600"))
SCHEDULE_RANKING_SNAPSHOT = int(os.getenv("SCHEDULE_RANKING_SNAPSHOT", "86400"))
SCHEDULE_TOURNAMENT_LIST = int(os.getenv("SCHEDULE_TOURNAMENT_LIST", "3600"))
SCHEDULE_JOB_POLL = int(os.getenv("SCHEDULE_JOB_POLL", "30"))

# ── USTA URLs ─────────────────────────────────────────────────────────
USTA_LOGIN_URL = "https://www.usta.com/en/home/login.html"
USTA_PLAYER_PROFILE_URL = (
    "https://www.usta.com/en/home/play/player-search/profile.html#uaid={uaid}&tab={tab}"
)
PLAYTENNIS_TOURNAMENT_LIST_URL = (
    "https://playtennis.usta.com/tournaments"
    "?panel=section&level-category=junior&organisation-group={org_guid}"
)
PLAYTENNIS_TOURNAMENT_DETAIL_URL = (
    "https://playtennis.usta.com/Competitions/{org_slug}/Tournaments/overview/{tournament_guid}"
)
USTA_RANKINGS_URL = (
    "https://www.usta.com/en/home/play/rankings.html"
    "#tab=junior&junior-ageRestriction={age}&junior-gender={gender}"
    "&junior-juniorListType={list_type}"
)
RANKINGS_API_URL = "https://services.usta.com/v1/dataexchange/rankings/search/public"

# ── Tournament API ────────────────────────────────────────────────────
USTA_TOURNAMENT_API_URL = (
    "https://prd-usta-kube.clubspark.pro/unified-search-api/api/Search/tournaments/Query"
)
USTA_TOURNAMENT_FILTERS_URL = (
    "https://playtennis.usta.com/Scripts/Data/tournaments-filters.json"
)
TOURNAMENT_API_PAGE_SIZE = int(os.getenv("TOURNAMENT_API_PAGE_SIZE", "100"))
TOURNAMENT_API_DELAY = float(os.getenv("TOURNAMENT_API_DELAY", "0.5"))
TOURNAMENT_SECTION_DELAY = float(os.getenv("TOURNAMENT_SECTION_DELAY", "2.0"))

# Ensure directories exist
DEBUG_DIR.mkdir(parents=True, exist_ok=True)
STATE_DIR.mkdir(parents=True, exist_ok=True)
