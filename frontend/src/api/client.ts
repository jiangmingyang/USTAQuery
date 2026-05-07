import type { PagedResponse, PlayerSummary, PlayerDetail, PlayerStats, Tournament, TournamentFilterOptions, TournamentEntry, PlayerTournamentEntry, Registration, Match, Ranking, RankingHistory } from "@/types"

const BASE_URL = "/api/v1"

async function fetchJson<T>(url: string): Promise<T> {
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(`API error: ${res.status} ${res.statusText}`)
  }
  return res.json()
}

function qs(params: Record<string, string | number | boolean | null | undefined>): string {
  const entries = Object.entries(params).filter(([, v]) => v != null && v !== "")
  if (entries.length === 0) return ""
  return "?" + entries.map(([k, v]) => `${k}=${encodeURIComponent(String(v))}`).join("&")
}

// Players
export function searchPlayers(q: string, page = 0, size = 20) {
  return fetchJson<PagedResponse<PlayerSummary>>(`${BASE_URL}/players/search${qs({ q, page, size })}`)
}

export function getPlayer(uaid: string) {
  return fetchJson<PlayerDetail>(`${BASE_URL}/players/${uaid}`)
}

export function getPlayerStats(uaid: string) {
  return fetchJson<PlayerStats>(`${BASE_URL}/players/${uaid}/stats`)
}

// Tournaments
export function searchTournaments(params: {
  q?: string; section?: string[]; level?: string[]; state?: string;
  year?: number; gender?: string[]; ageCategory?: string[]; eventType?: string[];
  page?: number; size?: number
}) {
  const flat: Record<string, string | number | boolean | null | undefined> = {
    q: params.q,
    state: params.state,
    year: params.year,
    page: params.page,
    size: params.size,
    section: params.section?.length ? params.section.join(",") : undefined,
    level: params.level?.length ? params.level.join(",") : undefined,
    gender: params.gender?.length ? params.gender.join(",") : undefined,
    ageCategory: params.ageCategory?.length ? params.ageCategory.join(",") : undefined,
    eventType: params.eventType?.length ? params.eventType.join(",") : undefined,
  }
  return fetchJson<PagedResponse<Tournament>>(`${BASE_URL}/tournaments/search${qs(flat)}`)
}

export function getTournamentFilters() {
  return fetchJson<TournamentFilterOptions>(`${BASE_URL}/tournaments/filters`)
}

export function getTournament(id: number) {
  return fetchJson<Tournament>(`${BASE_URL}/tournaments/${id}`)
}

export function getTournamentEntries(id: number, eventId?: string) {
  return fetchJson<TournamentEntry[]>(`${BASE_URL}/tournaments/${id}/entries${qs({ eventId })}`)
}

export function getPlayerTournaments(uaid: string, page = 0, size = 20) {
  return fetchJson<PagedResponse<Tournament>>(`${BASE_URL}/players/${uaid}/tournaments${qs({ page, size })}`)
}

export function getPlayerTournamentEntries(uaid: string) {
  return fetchJson<PlayerTournamentEntry[]>(`${BASE_URL}/players/${uaid}/tournament-entries`)
}

// Registrations
export function getPlayerRegistrations(uaid: string, status?: string, page = 0, size = 20) {
  return fetchJson<PagedResponse<Registration>>(`${BASE_URL}/players/${uaid}/registrations${qs({ status, page, size })}`)
}

// Matches
export function getPlayerMatches(uaid: string, page = 0, size = 20) {
  return fetchJson<PagedResponse<Match>>(`${BASE_URL}/players/${uaid}/matches${qs({ page, size })}`)
}

// Rankings
export function getPlayerRankings(uaid: string, listType?: string, ageRestriction?: string) {
  return fetchJson<Ranking[]>(`${BASE_URL}/players/${uaid}/rankings${qs({ listType, ageRestriction })}`)
}

export function getPlayerRankingHistory(uaid: string, catalogId: string) {
  return fetchJson<RankingHistory>(`${BASE_URL}/players/${uaid}/rankings/history${qs({ catalogId })}`)
}

export function getLeaderboard(params: { catalogId: string; page?: number; size?: number; publishDate?: string }) {
  return fetchJson<PagedResponse<Ranking>>(`${BASE_URL}/rankings${qs(params)}`)
}

export function getRankingVersions(catalogId: string) {
  return fetchJson<string[]>(`${BASE_URL}/rankings/versions${qs({ catalogId })}`)
}
