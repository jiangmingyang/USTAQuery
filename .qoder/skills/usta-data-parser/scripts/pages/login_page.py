"""USTA login page flow using stored credentials."""

import logging
import time

from playwright.sync_api import Page

import config
import browser as br

logger = logging.getLogger(__name__)


def login(page: Page) -> bool:
    """
    Navigate to USTA login, fill credentials, submit, and wait for redirect.
    Returns True on success, False on failure.
    """
    if not config.USTA_EMAIL or not config.USTA_PASSWORD:
        logger.error("USTA credentials not configured — set USTA_EMAIL and USTA_PASSWORD in .env")
        return False

    try:
        logger.info("Navigating to USTA login page...")
        page.goto(config.USTA_LOGIN_URL, wait_until="domcontentloaded", timeout=config.TIMEOUT)
        time.sleep(3)  # Wait for SPA hydration

        # Wait for login form to appear
        email_field = page.locator(
            "input[type='email'], input[name='email'], input[id*='email'], "
            "input[placeholder*='email' i], input[placeholder*='Email']"
        ).first
        email_field.wait_for(timeout=config.TIMEOUT)
        email_field.fill(config.USTA_EMAIL)

        password_field = page.locator(
            "input[type='password'], input[name='password'], input[id*='password']"
        ).first
        password_field.wait_for(timeout=10000)
        password_field.fill(config.USTA_PASSWORD)

        # Submit the form
        submit_btn = page.locator(
            "button[type='submit'], input[type='submit'], "
            "button:has-text('Sign In'), button:has-text('Log In'), "
            "button:has-text('Login')"
        ).first
        submit_btn.click()

        # Wait for redirect after login (profile page or dashboard)
        page.wait_for_url("**/home/**", timeout=config.TIMEOUT)
        time.sleep(3)

        logger.info("Login successful")
        return True

    except Exception as e:
        logger.error("Login failed: %s", e)
        br.screenshot_on_error(page, "login_failed")
        return False
