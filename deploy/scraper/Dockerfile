FROM python:3.11-slim-bookworm

# Install system dependencies for Chromium
RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium \
    libnss3 \
    libatk-bridge2.0-0 \
    libdrm2 \
    libxkbcommon0 \
    libgbm1 \
    libxshmfence1 \
    libx11-xcb1 \
    fonts-liberation \
    fonts-noto-cjk \
    && rm -rf /var/lib/apt/lists/*

# Tell Playwright to use system Chromium
ENV PLAYWRIGHT_BROWSERS_PATH=0
ENV PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/usr/bin/chromium

WORKDIR /app

# Install Python dependencies
COPY .qoder/skills/usta-data-parser/scripts/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt \
    && playwright install-deps

# Copy scraper source
COPY .qoder/skills/usta-data-parser/scripts/ ./

# Create state directories
RUN mkdir -p /app/browser-state /app/debug

ENV SCRAPE_HEADLESS=true
ENV SCRAPE_STATE_DIR=/app/browser-state
ENV SCRAPE_DEBUG_DIR=/app/debug

ENTRYPOINT ["python", "scraper_main.py", "--mode", "scheduler"]
