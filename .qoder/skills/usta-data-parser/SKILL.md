# USTA Data Parser Skill

A data parsing and extraction skill for USTA tennis data. Provides parsers that convert raw HTML/DOM content from USTA websites into structured data for database storage.

## Components

### Parsers (`scripts/parsers/`)

- **player_parser.py** — Extracts player profile info (name, location, gender, WTN/UTR/NTRP ratings, membership) from usta.com player profile pages
- **tournament_parser.py** — Extracts tournament metadata (name, level L1-L7, category OPEN/CLOSED, dates, venue, director, entry status) from playtennis.usta.com tournament detail pages
- **match_parser.py** — Extracts match results with per-set scores including tiebreak parsing. Handles both singles and doubles. Converts score strings like "6-3, 4-6, 7-6(5)" into structured set data
- **ranking_parser.py** — Extracts ranking data across age groups (B12-G18), 5 ranking types, and 3 scope levels (District/Section/National)

### Core Modules (`scripts/`)

- **config.py** — Configuration from environment variables
- **db.py** — MySQL connection pool and upsert helpers for all tables
- **browser.py** — Playwright browser lifecycle, persistent session, auto-login
- **scraper_main.py** — CLI entry point and orchestrator
- **scheduler.py** — APScheduler cron jobs for periodic scraping

### Page Objects (`scripts/pages/`)

- **login_page.py** — USTA SSO login flow
- **player_page.py** — Player profile page navigation and data extraction
- **tournament_list_page.py** — Tournament listing page with pagination
- **tournament_detail_page.py** — Tournament detail page with draws/results tabs

## Dependencies

```
playwright>=1.44.0
mysql-connector-python>=8.4.0
apscheduler>=3.10.4
python-dotenv>=1.0.1
```

## When to Use

Use this skill when you need to:
- Parse USTA player profile data from DOM elements
- Extract tournament information from playtennis.usta.com
- Convert tennis score strings to structured set/tiebreak data
- Process ranking data across multiple age groups and ranking types
