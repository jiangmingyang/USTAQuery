import { useParams, Link } from "react-router-dom"
import { useState, useEffect } from "react"
import { getPlayer, getPlayerStats, getPlayerMatches, getPlayerRankings, getPlayerRegistrations, getPlayerTournaments, getPlayerTournamentEntries } from "@/api/client"
import type { PlayerDetail, PlayerStats, Match, Ranking, Registration, Tournament, PagedResponse, PlayerTournamentEntry } from "@/types"
import { PlayerInfoSection } from "@/components/player/PlayerInfoSection"
import { MatchScoreDisplay, WinLossIndicator } from "@/components/match/MatchScoreDisplay"
import { LoadingSection, EmptyState, ErrorAlert, StatusBadge, LevelBadge, CategoryBadge } from "@/components/shared/StatusComponents"
import { cn, formatDate } from "@/lib/utils"
import { AGE_RESTRICTIONS, LIST_TYPES, formatRankingListName } from "@/lib/constants"

type Tab = "info" | "tournaments" | "registrations" | "matches" | "rankings"

export function PlayerProfilePage() {
  const { uaid } = useParams<{ uaid: string }>()
  const [tab, setTab] = useState<Tab>("info")
  const [player, setPlayer] = useState<PlayerDetail | null>(null)
  const [stats, setStats] = useState<PlayerStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!uaid) return
    setLoading(true)
    Promise.all([getPlayer(uaid), getPlayerStats(uaid)])
      .then(([p, s]) => { setPlayer(p); setStats(s) })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [uaid])

  if (loading) return <LoadingSection />
  if (error) return <div className="container py-8"><ErrorAlert message={error} /></div>
  if (!player) return <div className="container py-8"><EmptyState title="Player not found" /></div>

  const tabs: { key: Tab; label: string }[] = [
    { key: "info", label: "Overview" },
    { key: "tournaments", label: "Tournaments" },
    { key: "registrations", label: "Registrations" },
    { key: "matches", label: "Matches" },
    { key: "rankings", label: "Rankings" },
  ]

  return (
    <div className="container py-8">
      <div className="mb-6">
        <PlayerInfoSection player={player} stats={stats} />
      </div>

      {/* Tabs */}
      <div className="flex gap-4 border-b mb-6 overflow-x-auto">
        {tabs.map(({ key, label }) => (
          <button
            key={key}
            onClick={() => setTab(key)}
            className={cn("pb-2 px-1 text-sm font-medium whitespace-nowrap transition-colors",
              tab === key ? "tab-active" : "tab-inactive"
            )}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="animate-fade-in">
        {tab === "info" && <PlayerInfoTab player={player} />}
        {tab === "tournaments" && <TournamentsTab uaid={player.uaid} />}
        {tab === "registrations" && <RegistrationsTab uaid={player.uaid} />}
        {tab === "matches" && <MatchesTab uaid={player.uaid} />}
        {tab === "rankings" && <RankingsTab uaid={player.uaid} />}
      </div>
    </div>
  )
}

function PlayerInfoTab({ player }: { player: PlayerDetail }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <InfoRow label="Gender" value={player.gender === "M" ? "Male" : "Female"} />
      <InfoRow label="Section" value={player.section} />
      <InfoRow label="District" value={player.district} />
      <InfoRow label="Nationality" value={player.nationality} />
      <InfoRow label="Age Category" value={player.ageCategory} />
      <InfoRow label="ITF Tennis ID" value={player.itfTennisId} />
      <InfoRow label="Membership" value={player.membershipType} />
      <InfoRow label="Membership Expiry" value={formatDate(player.membershipExpiry)} />
      <InfoRow label="UTR ID" value={player.utrId} />
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <div className="flex items-center justify-between py-2 border-b border-border/50">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span className="text-sm font-medium">{value || "\u2014"}</span>
    </div>
  )
}

function TournamentsTab({ uaid }: { uaid: string }) {
  const [entries, setEntries] = useState<PlayerTournamentEntry[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getPlayerTournamentEntries(uaid).then(setEntries).catch(() => {}).finally(() => setLoading(false))
  }, [uaid])

  if (loading) return <LoadingSection />
  if (entries.length === 0) return <EmptyState title="No tournament entries" description="No tournament registrations found for this player." />

  // Group entries by tournament
  const grouped = new Map<number, { name: string; level: string | null; category: string | null; startDate: string | null; endDate: string | null; city: string | null; state: string | null; section: string | null; entries: PlayerTournamentEntry[] }>()
  for (const e of entries) {
    const tid = e.tournamentInternalId
    if (!grouped.has(tid)) {
      grouped.set(tid, {
        name: e.tournamentName,
        level: e.tournamentLevel,
        category: e.tournamentCategory,
        startDate: e.startDate,
        endDate: e.endDate,
        city: e.city,
        state: e.state,
        section: e.section,
        entries: [],
      })
    }
    grouped.get(tid)!.entries.push(e)
  }

  // Determine overall tournament status from entries
  function tournamentStatus(tEntries: PlayerTournamentEntry[]): "accepted" | "alternate" | "withdrawn" {
    for (const e of tEntries) {
      const s = (e.entryStatus || "").toUpperCase()
      if (s.includes("DIRECT") || s === "REGISTERED") return "accepted"
    }
    for (const e of tEntries) {
      const s = (e.entryStatus || "").toUpperCase()
      if (s.includes("ALTERNATE")) return "alternate"
    }
    for (const e of tEntries) {
      const s = (e.entryStatus || "").toUpperCase()
      if (s.includes("WITHDRAWN")) return "withdrawn"
    }
    return "accepted"
  }

  const statusConfig = {
    accepted:  { label: "Accepted",  border: "border-primary/30",     badge: "bg-primary/10 text-primary" },
    alternate: { label: "Alternate", border: "border-accent/50",      badge: "bg-accent text-accent-foreground" },
    withdrawn: { label: "Withdrawn", border: "border-destructive/30", badge: "bg-destructive/10 text-destructive" },
  }

  return (
    <div className="space-y-3">
      {Array.from(grouped.entries()).map(([tid, t]) => {
        const st = tournamentStatus(t.entries)
        const cfg = statusConfig[st]
        return (
          <Link key={tid} to={`/tournaments/${tid}`} className={cn("block rounded-lg border p-4 hover:bg-muted/30 transition-colors", cfg.border)}>
            <div className="flex items-start justify-between gap-3 mb-1.5">
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <p className="font-medium text-sm">{t.name}</p>
                  <span className={cn("inline-flex items-center rounded-md px-1.5 py-0.5 text-[10px] font-medium", cfg.badge)}>
                    {cfg.label}
                  </span>
                </div>
                <p className="text-xs text-muted-foreground mt-0.5">
                  {formatDate(t.startDate)}{t.endDate ? ` \u2013 ${formatDate(t.endDate)}` : ""}
                  {(t.city || t.state) && ` \u00b7 ${t.city && t.state ? `${t.city}, ${t.state}` : t.state || t.city}`}
                  {t.section && ` \u00b7 ${t.section}`}
                </p>
              </div>
              {t.level && <LevelBadge level={t.level} />}
            </div>
            <div className="flex flex-wrap gap-1.5 mt-2">
              {t.entries.map((e, i) => {
                const es = (e.entryStatus || "").toUpperCase()
                const entryColor = es.includes("DIRECT") || es === "REGISTERED"
                  ? "bg-primary/10 text-primary"
                  : es.includes("WITHDRAWN")
                    ? "bg-destructive/10 text-destructive"
                    : es.includes("ALTERNATE")
                      ? "bg-accent text-accent-foreground"
                      : "bg-muted text-muted-foreground"
                return (
                  <span key={`${e.eventId}-${i}`} className={cn("inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium", entryColor)}>
                    {e.eventType || "Event"}
                  </span>
                )
              })}
            </div>
          </Link>
        )
      })}
    </div>
  )
}

function RegistrationsTab({ uaid }: { uaid: string }) {
  const [data, setData] = useState<PagedResponse<Registration> | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getPlayerRegistrations(uaid).then(setData).catch(() => {}).finally(() => setLoading(false))
  }, [uaid])

  if (loading) return <LoadingSection />
  if (!data || data.content.length === 0) return <EmptyState title="No registrations found" />

  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr className="border-b">
            <th className="table-header text-left py-3 px-4">Tournament</th>
            <th className="table-header text-left py-3 px-4">Division</th>
            <th className="table-header text-left py-3 px-4">Type</th>
            <th className="table-header text-left py-3 px-4">Partner</th>
            <th className="table-header text-left py-3 px-4">Seed</th>
            <th className="table-header text-left py-3 px-4">Status</th>
          </tr>
        </thead>
        <tbody>
          {data.content.map((r) => (
            <tr key={r.id} className="border-b border-border/50 hover:bg-muted/30 transition-colors">
              <td className="table-cell">
                <p className="font-medium">{r.tournament.name}</p>
                <p className="text-xs text-muted-foreground">{formatDate(r.tournament.startDate)}</p>
              </td>
              <td className="table-cell">{r.divisionName}</td>
              <td className="table-cell">
                <span className={cn("badge-level", r.matchType === "DOUBLES" ? "bg-accent text-accent-foreground" : "bg-muted text-foreground")}>
                  {r.matchType}
                </span>
              </td>
              <td className="table-cell">
                {r.matchType === "DOUBLES"
                  ? (r.player2 ? `${r.player2.firstName} ${r.player2.lastName}` : <span className="text-muted-foreground italic">Partner TBD</span>)
                  : "\u2014"
                }
              </td>
              <td className="table-cell font-mono">{r.seed || "\u2014"}</td>
              <td className="table-cell"><StatusBadge status={r.status} /></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function MatchesTab({ uaid }: { uaid: string }) {
  const [data, setData] = useState<PagedResponse<Match> | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getPlayerMatches(uaid).then(setData).catch(() => {}).finally(() => setLoading(false))
  }, [uaid])

  if (loading) return <LoadingSection />
  if (!data || data.content.length === 0) return <EmptyState title="No match results" />

  return (
    <div className="space-y-2">
      {data.content.map((m) => (
        <div key={m.id} className="rounded-lg border p-4 hover:bg-muted/30 transition-colors">
          <div className="flex items-start justify-between gap-4 mb-2">
            <div>
              <p className="text-xs text-muted-foreground">{m.tournamentName} · {m.round} · {m.divisionName}</p>
              <div className="flex items-center gap-2 mt-1">
                <WinLossIndicator winnerSide={m.winnerSide} />
                <span className="text-sm font-medium">
                  vs {m.opponent1Name || "\u2014"}
                  {m.matchType === "DOUBLES" && m.opponent2Name && ` / ${m.opponent2Name}`}
                </span>
              </div>
              {m.matchType === "DOUBLES" && m.player2 && (
                <p className="text-xs text-muted-foreground mt-0.5">
                  Partner: {m.player2.firstName} {m.player2.lastName}
                </p>
              )}
            </div>
            <div className="text-right flex-shrink-0">
              <MatchScoreDisplay sets={m.sets} />
              {m.matchDate && <p className="text-xs text-muted-foreground mt-1">{formatDate(m.matchDate)}</p>}
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

function RankingsTab({ uaid }: { uaid: string }) {
  const [rankings, setRankings] = useState<Ranking[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedAgeRestriction, setSelectedAgeRestriction] = useState<string>("")
  const [selectedListType, setSelectedListType] = useState<string>("")

  useEffect(() => {
    setLoading(true)
    getPlayerRankings(uaid, selectedListType || undefined, selectedAgeRestriction || undefined)
      .then(setRankings)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [uaid, selectedAgeRestriction, selectedListType])

  return (
    <div>
      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6">
        <div>
          <label className="text-xs font-medium text-muted-foreground block mb-1">Age</label>
          <div className="flex gap-1 flex-wrap">
            <button
              onClick={() => setSelectedAgeRestriction("")}
              className={cn("px-3 py-1 rounded-md text-xs font-medium transition-colors",
                selectedAgeRestriction === "" ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground hover:bg-accent"
              )}
            >
              All
            </button>
            {AGE_RESTRICTIONS.map((ar) => (
              <button
                key={ar}
                onClick={() => setSelectedAgeRestriction(ar)}
                className={cn("px-3 py-1 rounded-md text-xs font-medium transition-colors",
                  selectedAgeRestriction === ar ? "bg-primary text-primary-foreground" : "bg-muted text-muted-foreground hover:bg-accent"
                )}
              >
                {ar}
              </button>
            ))}
          </div>
        </div>
        <div>
          <label className="text-xs font-medium text-muted-foreground block mb-1">List Type</label>
          <select
            value={selectedListType}
            onChange={(e) => setSelectedListType(e.target.value)}
            className="h-8 rounded-md border bg-background px-2 text-xs"
          >
            <option value="">All Types</option>
            {LIST_TYPES.map((lt) => (
              <option key={lt.value} value={lt.value}>{lt.label}</option>
            ))}
          </select>
        </div>
      </div>

      {loading && <LoadingSection />}
      {!loading && rankings.length === 0 && <EmptyState title="No rankings found" description="Try different filters" />}
      {!loading && rankings.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b">
                <th className="table-header text-left py-3 px-4">Ranking</th>
                <th className="table-header text-center py-3 px-4">District</th>
                <th className="table-header text-center py-3 px-4">Section</th>
                <th className="table-header text-center py-3 px-4">National</th>
                <th className="table-header text-right py-3 px-4">Points</th>
                <th className="table-header text-center py-3 px-4">W/L</th>
                <th className="table-header text-center py-3 px-4">Trend</th>
              </tr>
            </thead>
            <tbody>
              {rankings.map((r) => (
                <tr key={r.id} className="border-b border-border/50 hover:bg-muted/30 transition-colors">
                  <td className="table-cell">
                    <p className="font-medium text-sm">{formatRankingListName(r)}</p>
                    <p className="text-xs text-muted-foreground">{r.ageRestriction}</p>
                  </td>
                  <td className="table-cell text-center font-mono">{r.districtRank ?? "\u2014"}</td>
                  <td className="table-cell text-center font-mono">{r.sectionRank ?? "\u2014"}</td>
                  <td className="table-cell text-center font-mono font-bold">{r.nationalRank ?? "\u2014"}</td>
                  <td className="table-cell text-right font-mono">{r.points ?? "\u2014"}</td>
                  <td className="table-cell text-center text-sm">
                    {r.wins != null && r.losses != null ? `${r.wins}-${r.losses}` : "\u2014"}
                  </td>
                  <td className="table-cell text-center">
                    {r.trendDirection === "down" && <span className="text-win">&#9650;</span>}
                    {r.trendDirection === "up" && <span className="text-loss">&#9660;</span>}
                    {r.trendDirection === "no change" && <span className="text-muted-foreground">&#8212;</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
