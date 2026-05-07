"""Playwright browser lifecycle, persistent session, and auto-login.

Each thread gets its own Playwright instance + Browser + BrowserContext because
Playwright's sync API uses greenlets that are bound to the creating thread.
Thread-local storage ensures proper isolation for concurrent scraping.
"""
from __future__ import annotations

import logging
import threading
import time

from playwright.sync_api import sync_playwright, Playwright, Browser, BrowserContext, Page

import config

logger = logging.getLogger(__name__)

# Thread-local storage: each thread owns its own playwright/browser/context
_local = threading.local()

# Track all thread-local instances for cleanup at exit
_instances: list[dict] = []
_instances_lock = threading.Lock()

STATE_FILE = config.STATE_DIR / "state.json"


_CONTEXT_OPTS = {
    "user_agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    ),
    "viewport": {"width": 1440, "height": 900},
    "locale": "en-US",
    "timezone_id": "America/New_York",
}


def _get_context() -> BrowserContext:
    """Get or create a BrowserContext for the current thread."""
    ctx = getattr(_local, "context", None)
    if ctx is not None:
        return ctx

    pw = sync_playwright().start()
    browser = pw.chromium.launch(
        headless=config.HEADLESS,
        args=[
            "--disable-blink-features=AutomationControlled",
        ],
    )

    ctx_opts = dict(_CONTEXT_OPTS)
    # Only the main thread loads saved state; worker threads get fresh
    # contexts to avoid cookie/session conflicts with the server.
    is_main = threading.current_thread() is threading.main_thread()
    if is_main and STATE_FILE.exists():
        logger.info("Loading saved browser state from %s", STATE_FILE)
        ctx_opts["storage_state"] = str(STATE_FILE)
    elif not is_main:
        logger.debug("Worker thread — creating fresh browser context")
    else:
        logger.info("No saved browser state found, creating fresh context")

    ctx = browser.new_context(**ctx_opts)
    ctx.add_init_script("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})")

    _local.playwright = pw
    _local.browser = browser
    _local.context = ctx
    _local.is_main = is_main

    # Track for cleanup
    with _instances_lock:
        _instances.append({"playwright": pw, "browser": browser, "context": ctx})

    return ctx


def launch() -> BrowserContext:
    """Launch Chromium and return a BrowserContext for the current thread."""
    return _get_context()


def get_page() -> Page:
    """Return a new page from the current thread's context."""
    ctx = _get_context()
    return ctx.new_page()


def save_session():
    """Persist cookies + localStorage to disk for session reuse (main thread only)."""
    ctx = getattr(_local, "context", None)
    is_main = getattr(_local, "is_main", False)
    if ctx is not None and is_main:
        ctx.storage_state(path=str(STATE_FILE))
        logger.info("Browser state saved to %s", STATE_FILE)


def ensure_logged_in(page: Page) -> bool:
    """
    Check if page shows logged-in state on usta.com.
    If not, perform login with stored credentials.
    Returns True if logged in successfully.
    """
    from pages.login_page import login

    # Check if already logged in by visiting a profile page indicator
    page.goto(
        "https://www.usta.com/en/home/play/player-search.html",
        wait_until="domcontentloaded",
        timeout=config.TIMEOUT,
    )
    _wait_for_load(page)

    # Look for signs of being logged in (user menu / account link)
    logged_in = page.locator("[data-testid='user-menu'], .user-menu, .account-link, .logged-in").first
    try:
        logged_in.wait_for(timeout=5000)
        logger.info("Already logged in to USTA")
        return True
    except Exception:
        pass

    # Not logged in; perform login
    logger.info("Not logged in, performing login...")
    success = login(page)
    if success:
        save_session()
    return success


def _wait_for_load(page: Page):
    """Wait for page to finish loading dynamic content."""
    try:
        page.wait_for_load_state("domcontentloaded", timeout=config.TIMEOUT)
    except Exception:
        pass
    # Give the SPA a moment to hydrate
    time.sleep(2)


def delay():
    """Pause between navigations to avoid rate limiting."""
    time.sleep(config.PAGE_DELAY)


def screenshot_on_error(page: Page, name: str):
    """Save a debug screenshot when something goes wrong."""
    try:
        path = config.DEBUG_DIR / f"{name}_{int(time.time())}.png"
        page.screenshot(path=str(path))
        logger.info("Debug screenshot saved: %s", path)
    except Exception as e:
        logger.warning("Failed to save debug screenshot: %s", e)


def close():
    """Shut down all browser instances across all threads."""
    # Close current thread's instance
    ctx = getattr(_local, "context", None)
    if ctx:
        save_session()

    # Close all tracked instances
    with _instances_lock:
        for inst in _instances:
            try:
                inst["context"].close()
            except Exception:
                pass
            try:
                inst["browser"].close()
            except Exception:
                pass
            try:
                inst["playwright"].stop()
            except Exception:
                pass
        _instances.clear()

    # Clear thread-local
    _local.playwright = None
    _local.browser = None
    _local.context = None

    logger.info("All browser instances closed")
