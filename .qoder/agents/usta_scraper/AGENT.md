# USTA Scraper Agent

An autonomous agent that scrapes USTA (United States Tennis Association) player profiles, tournament data, match results, and rankings from usta.com and playtennis.usta.com using Playwright browser automation.

## Capabilities

- **Player Profile Scraping**: Extracts player info, WTN/UTR/NTRP ratings, rankings, tournament history, and match results from usta.com player profiles
- **Tournament List Scraping**: Crawls tournament listings from playtennis.usta.com by section/district
- **Tournament Detail Scraping**: Extracts full tournament data including draws, registrations, and match results
- **Scheduled Batch Operations**: Runs on configurable intervals to keep data fresh
- **On-Demand Scraping**: Processes scrape requests queued in the database by the backend API
- **Session Persistence**: Maintains USTA login session across runs using stored browser state

## Usage

```bash
# Navigate to the scripts directory
cd .qoder/skills/usta-data-parser/scripts

# Install dependencies
pip install -r requirements.txt
playwright install chromium

# Configure credentials (copy .env.example to .env at project root)
cp ../../../../.env.example ../../../../.env
# Edit .env with your USTA credentials and DB config

# Scrape a single player
python scraper_main.py --mode single --uaid 2019267792

# Scrape tournament list for a section
python scraper_main.py --mode tournaments --org-guid 42ee52e4-9e12-46c0-80d4-add64afa1d73

# Scrape a tournament's full detail
python scraper_main.py --mode tournament-detail --org-slug desertchampionsllc --guid 78CE4C97-6EAE-4CF4-969E-9238DA03B64F

# Re-scrape all stale data
python scraper_main.py --mode full-sync

# Run the scheduler daemon
python scraper_main.py --mode scheduler

# Process pending on-demand jobs
python scraper_main.py --mode poll-jobs

# Enable debug logging
python scraper_main.py --mode single --uaid 2019267792 --debug
```

## Target Sites

| Site | Data |
|---|---|
| usta.com | Player profiles, ratings (WTN/UTR/NTRP), rankings, tournament history, match results |
| playtennis.usta.com | Tournament listings, tournament details, draws, registrations, match results |

## Authentication

The scraper requires a valid USTA member account. Credentials are stored in `.env` (gitignored). The browser session is persisted to `~/.usta-scraper/browser-state/` and reused across runs. Auto re-login occurs when the session expires.

## Data Flow

```
Playwright Browser → Parse DOM → Structured Dicts → MySQL Upserts → Spring Boot API
```

## Error Handling

- Failed scrapes are logged to the `scrape_errors` table
- Debug screenshots are saved to `~/.usta-scraper/debug/` on failure
- Each scrape job is tracked in the `scrape_jobs` table with status and record counts
- Graceful degradation: if one tab/section fails, the scraper continues with others
