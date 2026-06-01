#!/usr/bin/env python3
"""Generate a self-contained HTML page from USTA rankings JSON files.

Reads all rankings_*.json files from the output directory and produces
a single HTML file with the data embedded, viewable in any browser.

Usage:
    python generate_rankings_html.py                          # default paths
    python generate_rankings_html.py --input my_data/ --output rankings.html
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

DEFAULT_INPUT_DIR = "usta_rankings_output"
DEFAULT_OUTPUT_FILE = "usta_rankings.html"

HTML_TEMPLATE = r"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>USTA Junior Rankings</title>
<style>
  :root {
    --navy: #00205b;
    --green: #007a33;
    --blue: #0057b8;
    --light-bg: #f5f7fa;
    --border: #d1d5db;
    --row-hover: #e8f0fe;
    --white: #ffffff;
    --text: #1f2937;
    --text-secondary: #6b7280;
    --up: #16a34a;
    --down: #dc2626;
    --radius: 8px;
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    background: var(--light-bg);
    color: var(--text);
    line-height: 1.5;
  }

  /* ── Header ── */
  .header {
    background: var(--navy);
    color: var(--white);
    padding: 24px 32px;
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
  }
  .header h1 { font-size: 24px; font-weight: 700; letter-spacing: -0.02em; }
  .header .subtitle { color: #94a3b8; font-size: 14px; }

  /* ── Controls ── */
  .controls {
    background: var(--white);
    border-bottom: 1px solid var(--border);
    padding: 16px 32px;
    display: flex;
    gap: 16px;
    align-items: center;
    flex-wrap: wrap;
    position: sticky;
    top: 0;
    z-index: 10;
  }
  .tab-group { display: flex; gap: 4px; }
  .tab-group label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
    margin-right: 8px;
    align-self: center;
  }
  .tab-btn {
    padding: 6px 16px;
    border: 1px solid var(--border);
    border-radius: var(--radius);
    background: var(--white);
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    color: var(--text);
    transition: all 0.15s;
  }
  .tab-btn:hover { background: var(--light-bg); }
  .tab-btn.active {
    background: var(--navy);
    color: var(--white);
    border-color: var(--navy);
  }
  .search-box {
    margin-left: auto;
    position: relative;
  }
  .search-box input {
    padding: 7px 12px 7px 34px;
    border: 1px solid var(--border);
    border-radius: var(--radius);
    font-size: 14px;
    width: 240px;
    outline: none;
    transition: border-color 0.15s;
  }
  .search-box input:focus { border-color: var(--blue); }
  .search-box svg {
    position: absolute; left: 10px; top: 50%;
    transform: translateY(-50%);
    color: var(--text-secondary);
  }

  /* ── Info bar ── */
  .info-bar {
    padding: 12px 32px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }
  .info-bar .list-title { font-size: 16px; font-weight: 600; }
  .info-bar .stats { font-size: 13px; color: var(--text-secondary); }
  .info-bar .stats span { margin-left: 16px; }

  /* ── Table ── */
  .table-wrap {
    padding: 0 32px 32px;
    overflow-x: auto;
  }
  table {
    width: 100%;
    border-collapse: collapse;
    background: var(--white);
    border-radius: var(--radius);
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0,0,0,0.06);
    font-size: 14px;
  }
  thead th {
    background: var(--navy);
    color: var(--white);
    padding: 10px 14px;
    text-align: left;
    font-weight: 600;
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    white-space: nowrap;
    cursor: pointer;
    user-select: none;
    position: relative;
  }
  thead th:hover { background: #1a3a7a; }
  thead th .sort-arrow { margin-left: 4px; font-size: 10px; opacity: 0.5; }
  thead th.sorted .sort-arrow { opacity: 1; }
  tbody tr { border-bottom: 1px solid #f0f0f0; }
  tbody tr:hover { background: var(--row-hover); }
  tbody td {
    padding: 9px 14px;
    white-space: nowrap;
  }
  .rank-cell { font-weight: 700; color: var(--navy); text-align: center; min-width: 48px; }
  .name-cell { font-weight: 600; }
  .pts-cell { text-align: right; font-variant-numeric: tabular-nums; }
  .trend-up { color: var(--up); }
  .trend-down { color: var(--down); }
  .trend-cell { text-align: center; font-size: 15px; }
  .loc-cell { color: var(--text-secondary); }

  /* ── Pagination ── */
  .pagination {
    padding: 16px 32px;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
  }
  .page-btn {
    padding: 6px 12px;
    border: 1px solid var(--border);
    border-radius: var(--radius);
    background: var(--white);
    cursor: pointer;
    font-size: 13px;
    min-width: 36px;
    text-align: center;
  }
  .page-btn:hover { background: var(--light-bg); }
  .page-btn.active { background: var(--navy); color: var(--white); border-color: var(--navy); }
  .page-btn:disabled { opacity: 0.4; cursor: default; }
  .page-info { font-size: 13px; color: var(--text-secondary); margin: 0 8px; }

  /* ── Responsive ── */
  @media (max-width: 768px) {
    .header, .controls, .info-bar, .table-wrap, .pagination { padding-left: 16px; padding-right: 16px; }
    .search-box input { width: 160px; }
    .search-box { margin-left: 0; }
    .controls { gap: 8px; }
  }
</style>
</head>
<body>

<div class="header">
  <div>
    <h1>USTA Junior Rankings</h1>
    <div class="subtitle" id="scrapeDate"></div>
  </div>
</div>

<div class="controls">
  <div class="tab-group">
    <label>Gender</label>
    <button class="tab-btn active" data-filter="gender" data-value="M">Boys</button>
    <button class="tab-btn" data-filter="gender" data-value="F">Girls</button>
  </div>
  <div class="tab-group">
    <label>Age</label>
    <button class="tab-btn active" data-filter="age" data-value="Y12">12U</button>
    <button class="tab-btn" data-filter="age" data-value="Y14">14U</button>
    <button class="tab-btn" data-filter="age" data-value="Y16">16U</button>
    <button class="tab-btn" data-filter="age" data-value="Y18">18U</button>
  </div>
  <div class="search-box">
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
    <input type="text" id="searchInput" placeholder="Search player name...">
  </div>
</div>

<div class="info-bar">
  <div class="list-title" id="listTitle">Boys' 12 National Standings</div>
  <div class="stats">
    <span id="statShowing"></span>
    <span id="statPublished"></span>
  </div>
</div>

<div class="table-wrap">
  <table>
    <thead>
      <tr>
        <th data-sort="nationalRank" class="sorted">Rank <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="name">Name <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="trend">Trend <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="points">Points <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="singlesPoints">Singles <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="doublesPoints">Doubles <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="bonusPoints">Bonus <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="sectionRank">Section Rk <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="section">Section <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="state">State <span class="sort-arrow">&#9650;</span></th>
        <th data-sort="city">City <span class="sort-arrow">&#9650;</span></th>
      </tr>
    </thead>
    <tbody id="tableBody"></tbody>
  </table>
</div>

<div class="pagination" id="pagination"></div>

<script>
// ── Embedded data ──
const RANKINGS_DATA = __RANKINGS_DATA__;

// ── State ──
let currentGender = 'M';
let currentAge = 'Y12';
let searchQuery = '';
let sortField = 'nationalRank';
let sortAsc = true;
let currentPage = 1;
const PAGE_SIZE = 50;

// ── Init ──
function init() {
  // Set scrape date
  const firstKey = Object.keys(RANKINGS_DATA)[0];
  if (firstKey && RANKINGS_DATA[firstKey].scrape_timestamp) {
    const d = new Date(RANKINGS_DATA[firstKey].scrape_timestamp);
    document.getElementById('scrapeDate').textContent =
      'Data scraped: ' + d.toLocaleDateString('en-US', { year:'numeric', month:'long', day:'numeric' });
  }

  // Tab buttons
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const filter = btn.dataset.filter;
      const value = btn.dataset.value;
      btn.parentElement.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      if (filter === 'gender') currentGender = value;
      else currentAge = value;
      currentPage = 1;
      render();
    });
  });

  // Search
  document.getElementById('searchInput').addEventListener('input', e => {
    searchQuery = e.target.value.toLowerCase();
    currentPage = 1;
    render();
  });

  // Sort
  document.querySelectorAll('thead th[data-sort]').forEach(th => {
    th.addEventListener('click', () => {
      const field = th.dataset.sort;
      if (sortField === field) {
        sortAsc = !sortAsc;
      } else {
        sortField = field;
        sortAsc = field === 'name' || field === 'section' || field === 'state' || field === 'city';
      }
      document.querySelectorAll('thead th').forEach(h => h.classList.remove('sorted'));
      th.classList.add('sorted');
      th.querySelector('.sort-arrow').innerHTML = sortAsc ? '&#9650;' : '&#9660;';
      currentPage = 1;
      render();
    });
  });

  render();
}

function getDataKey(gender, age) {
  return gender + '_' + age;
}

function normalizePlayer(p) {
  return {
    name: p.name || '',
    uaid: p.uaid || '',
    city: p.city || '',
    state: p.state || '',
    trend: p.trend || 'no change',
    points: p.points || 0,
    nationalRank: (p.rank && p.rank.national) || 0,
    sectionRank: (p.rank && p.rank.section) || 0,
    districtRank: (p.rank && p.rank.district) || 0,
    section: (p.section && p.section.name) || '',
    district: (p.district && p.district.name) || '',
    singlesPoints: (p.pointsRecord && p.pointsRecord.singlesPoints) || 0,
    doublesPoints: (p.pointsRecord && p.pointsRecord.doublesPoints) || 0,
    bonusPoints: (p.pointsRecord && p.pointsRecord.bonusPoints) || 0,
  };
}

function render() {
  const key = getDataKey(currentGender, currentAge);
  const dataset = RANKINGS_DATA[key];

  if (!dataset || !dataset.players || dataset.players.length === 0) {
    document.getElementById('listTitle').textContent = 'No data available';
    document.getElementById('tableBody').innerHTML =
      '<tr><td colspan="11" style="text-align:center;padding:32px;color:#6b7280;">No data for this combination.</td></tr>';
    document.getElementById('pagination').innerHTML = '';
    document.getElementById('statShowing').textContent = '';
    document.getElementById('statPublished').textContent = '';
    return;
  }

  const meta = dataset.metadata || {};
  document.getElementById('listTitle').textContent = meta.displayLabel || (key + ' Rankings');
  if (meta.publishDate) {
    const pd = new Date(meta.publishDate);
    document.getElementById('statPublished').textContent =
      'Published: ' + pd.toLocaleDateString('en-US', { year:'numeric', month:'short', day:'numeric' });
  }

  // Normalize, filter, sort
  let players = dataset.players.map(normalizePlayer);

  if (searchQuery) {
    players = players.filter(p => p.name.toLowerCase().includes(searchQuery));
  }

  players.sort((a, b) => {
    let va = a[sortField], vb = b[sortField];
    if (typeof va === 'string') va = va.toLowerCase();
    if (typeof vb === 'string') vb = vb.toLowerCase();
    if (va < vb) return sortAsc ? -1 : 1;
    if (va > vb) return sortAsc ? 1 : -1;
    return 0;
  });

  const totalPlayers = players.length;
  const totalPages = Math.max(1, Math.ceil(totalPlayers / PAGE_SIZE));
  if (currentPage > totalPages) currentPage = totalPages;
  const start = (currentPage - 1) * PAGE_SIZE;
  const pageSlice = players.slice(start, start + PAGE_SIZE);

  document.getElementById('statShowing').textContent =
    'Showing ' + (start + 1) + '-' + (start + pageSlice.length) + ' of ' + totalPlayers +
    (meta.totalResults && meta.totalResults > totalPlayers ? ' (fetched) / ' + meta.totalResults.toLocaleString() + ' total' : '');

  // Render rows
  const tbody = document.getElementById('tableBody');
  tbody.innerHTML = pageSlice.map(p => {
    let trendIcon = '';
    let trendClass = 'trend-cell';
    if (p.trend === 'up') { trendIcon = '&#9650;'; trendClass += ' trend-up'; }
    else if (p.trend === 'down') { trendIcon = '&#9660;'; trendClass += ' trend-down'; }
    else { trendIcon = '&mdash;'; }

    return '<tr>' +
      '<td class="rank-cell">' + p.nationalRank + '</td>' +
      '<td class="name-cell">' + escapeHtml(p.name) + '</td>' +
      '<td class="' + trendClass + '">' + trendIcon + '</td>' +
      '<td class="pts-cell">' + p.points.toLocaleString() + '</td>' +
      '<td class="pts-cell">' + p.singlesPoints.toLocaleString() + '</td>' +
      '<td class="pts-cell">' + p.doublesPoints.toLocaleString() + '</td>' +
      '<td class="pts-cell">' + p.bonusPoints.toLocaleString() + '</td>' +
      '<td class="rank-cell">' + p.sectionRank + '</td>' +
      '<td class="loc-cell">' + escapeHtml(p.section) + '</td>' +
      '<td class="loc-cell">' + escapeHtml(p.state) + '</td>' +
      '<td class="loc-cell">' + escapeHtml(p.city) + '</td>' +
    '</tr>';
  }).join('');

  // Pagination
  renderPagination(totalPages);
}

function renderPagination(totalPages) {
  const container = document.getElementById('pagination');
  if (totalPages <= 1) { container.innerHTML = ''; return; }

  let html = '';
  html += '<button class="page-btn" onclick="goPage(1)" ' + (currentPage===1?'disabled':'') + '>&laquo;</button>';
  html += '<button class="page-btn" onclick="goPage(' + (currentPage-1) + ')" ' + (currentPage===1?'disabled':'') + '>&lsaquo;</button>';

  const range = getPageRange(currentPage, totalPages, 7);
  for (const p of range) {
    if (p === '...') {
      html += '<span class="page-info">...</span>';
    } else {
      html += '<button class="page-btn' + (p===currentPage?' active':'') + '" onclick="goPage(' + p + ')">' + p + '</button>';
    }
  }

  html += '<button class="page-btn" onclick="goPage(' + (currentPage+1) + ')" ' + (currentPage===totalPages?'disabled':'') + '>&rsaquo;</button>';
  html += '<button class="page-btn" onclick="goPage(' + totalPages + ')" ' + (currentPage===totalPages?'disabled':'') + '>&raquo;</button>';

  container.innerHTML = html;
}

function getPageRange(current, total, maxVisible) {
  if (total <= maxVisible) return Array.from({length:total}, (_,i) => i+1);
  const pages = [];
  const half = Math.floor(maxVisible / 2);
  let start = Math.max(2, current - half);
  let end = Math.min(total - 1, current + half);
  if (current <= half + 1) end = Math.min(total - 1, maxVisible - 1);
  if (current >= total - half) start = Math.max(2, total - maxVisible + 2);
  pages.push(1);
  if (start > 2) pages.push('...');
  for (let i = start; i <= end; i++) pages.push(i);
  if (end < total - 1) pages.push('...');
  pages.push(total);
  return pages;
}

function goPage(p) {
  currentPage = p;
  render();
  window.scrollTo({top: 0, behavior: 'smooth'});
}

function escapeHtml(s) {
  const d = document.createElement('div');
  d.appendChild(document.createTextNode(s));
  return d.innerHTML;
}

document.addEventListener('DOMContentLoaded', init);
</script>
</body>
</html>
"""


def load_rankings_data(input_dir: str) -> dict:
    """Load all rankings JSON files into a combined dict keyed by 'M_Y12' etc."""
    data = {}
    input_path = Path(input_dir)

    for filepath in sorted(input_path.glob("rankings_[MF]_Y*.json")):
        # Extract key from filename: rankings_M_Y12.json -> M_Y12
        stem = filepath.stem  # rankings_M_Y12
        parts = stem.split("_", 1)  # ['rankings', 'M_Y12']
        key = parts[1] if len(parts) > 1 else stem

        with open(filepath, "r", encoding="utf-8") as f:
            data[key] = json.load(f)

    return data


def generate_html(input_dir: str, output_file: str) -> None:
    """Read JSON files and produce a self-contained HTML file."""
    data = load_rankings_data(input_dir)

    if not data:
        print(f"No rankings JSON files found in {input_dir}", file=sys.stderr)
        sys.exit(1)

    total_players = sum(len(d.get("players", [])) for d in data.values())
    print(f"Loaded {len(data)} ranking files with {total_players} total players")

    # Embed data as JSON in the HTML
    data_json = json.dumps(data, ensure_ascii=False, default=str)
    html = HTML_TEMPLATE.replace("__RANKINGS_DATA__", data_json)

    output_path = Path(output_file)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(html)

    print(f"Generated {output_path} ({output_path.stat().st_size / 1024:.1f} KB)")


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate HTML rankings viewer from JSON files")
    parser.add_argument("--input", "-i", default=DEFAULT_INPUT_DIR,
                        help=f"Input directory with rankings JSON files (default: {DEFAULT_INPUT_DIR})")
    parser.add_argument("--output", "-o", default=DEFAULT_OUTPUT_FILE,
                        help=f"Output HTML file (default: {DEFAULT_OUTPUT_FILE})")
    args = parser.parse_args()

    generate_html(args.input, args.output)


if __name__ == "__main__":
    main()
