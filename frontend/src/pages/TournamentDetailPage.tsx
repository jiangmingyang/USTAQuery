import { useState, useEffect } from "react"
import { useParams, Link } from "react-router-dom"
import { getTournament, getTournamentEntries } from "@/api/client"
import type { Tournament, TournamentEvent, TournamentEntry } from "@/types"
import { Card, CardContent } from "@/components/ui/card"
import { LoadingSection, EmptyState, ErrorAlert, LevelBadge } from "@/components/shared/StatusComponents"
import { cn, formatDate } from "@/lib/utils"
import { ExternalLink, Calendar, MapPin, Trophy, Users, ArrowLeft } from "lucide-react"

const GENDER_MAP: Record<string, string> = {
  Male: "Boys", Female: "Girls", Coed: "Coed", Mixed: "Mixed",
  male: "Boys", female: "Girls", coed: "Coed", mixed: "Mixed",
  Boys: "Boys", Girls: "Girls",
}
const BALL_COLOR: Record<string, string> = {
  Yellow: "#eab308", Green: "#22c55e", Orange: "#f97316", Red: "#ef4444",
  yellow: "#eab308", green: "#22c55e", orange: "#f97316", red: "#ef4444",
}

function eventLabel(ev: TournamentEvent): string {
  const gender = GENDER_MAP[ev.gender || ""] || ev.gender || ""
  const age = ev.ageCategory || ""
  const type = ev.eventType || ""
  return [gender, age, type].filter(Boolean).join(" ")
}

function BallDot({ color }: { color: string | null }) {
  if (!color) return null
  const hex = BALL_COLOR[color] || BALL_COLOR[color.toLowerCase()] || null
  if (!hex) return null
  return <span className="inline-block h-2 w-2 rounded-full shrink-0" style={{ backgroundColor: hex }} />
}

export function TournamentDetailPage() {
  const { id } = useParams<{ id: string }>()
  const [tournament, setTournament] = useState<Tournament | null>(null)
  const [entries, setEntries] = useState<TournamentEntry[]>([])
  const [selectedEventId, setSelectedEventId] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [entriesLoading, setEntriesLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const numericId = Number(id)

  // Load tournament
  useEffect(() => {
    if (!id) return
    setLoading(true)
    setError(null)
    getTournament(numericId)
      .then(setTournament)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [id, numericId])

  // Load entries when tournament loads or event selection changes
  useEffect(() => {
    if (!id) return
    setEntriesLoading(true)
    getTournamentEntries(numericId, selectedEventId || undefined)
      .then(setEntries)
      .catch(() => setEntries([]))
      .finally(() => setEntriesLoading(false))
  }, [id, numericId, selectedEventId])

  if (loading) return <LoadingSection />
  if (error) return <div className="max-w-4xl mx-auto p-4"><ErrorAlert message={error} /></div>
  if (!tournament) return <div className="max-w-4xl mx-auto p-4"><ErrorAlert message="Tournament not found" /></div>

  const ustaUrl = tournament.tournamentId
    ? `https://playtennis.usta.com/Competitions/${tournament.orgSlug || "abc"}/Tournaments/Overview/${tournament.tournamentId}`
    : null

  const events = tournament.events || []

  // Build a map from eventId -> event for labeling in the "All" view
  const eventMap = new Map<string, TournamentEvent>()
  for (const ev of events) {
    eventMap.set(ev.eventId, ev)
  }

  return (
    <div className="max-w-4xl mx-auto p-4 space-y-4">
      {/* Back link + USTA link */}
      <div className="flex items-center justify-between">
        <Link to="/tournaments" className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors">
          <ArrowLeft className="h-4 w-4" />
          Back to tournaments
        </Link>
        {ustaUrl && (
          <a href={ustaUrl} target="_blank" rel="noopener noreferrer"
            className="inline-flex items-center gap-1 text-xs text-primary hover:underline">
            View on USTA
            <ExternalLink className="h-3 w-3" />
          </a>
        )}
      </div>

      {/* Tournament Info Card */}
      <Card>
        <CardContent className="p-5">
          <div className="flex items-start justify-between gap-3 mb-3">
            <div>
              <h2 className="text-xl font-bold leading-tight">{tournament.name}</h2>
              {tournament.code && <p className="text-xs text-muted-foreground mt-0.5">{tournament.code}</p>}
            </div>
            <LevelBadge level={tournament.level} />
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 gap-x-6 gap-y-2 text-sm">
            <InfoItem icon={<Calendar className="h-3.5 w-3.5" />} label="Dates"
              value={`${formatDate(tournament.startDate)}${tournament.endDate ? ` \u2013 ${formatDate(tournament.endDate)}` : ""}`} />
            {tournament.entryDeadline && (
              <InfoItem label="Entry Deadline" value={formatDate(tournament.entryDeadline)} />
            )}
            {(tournament.city || tournament.state) && (
              <InfoItem icon={<MapPin className="h-3.5 w-3.5" />} label="Location"
                value={tournament.city && tournament.state ? `${tournament.city}, ${tournament.state}` : tournament.state || tournament.city || ""} />
            )}
            {tournament.venueName && <InfoItem label="Venue" value={tournament.venueName} />}
            {tournament.section && <InfoItem label="Section" value={tournament.section} />}
            {tournament.surface && <InfoItem label="Surface" value={tournament.surface} />}
            {tournament.organization && <InfoItem label="Organization" value={tournament.organization} />}
            {tournament.directorName && <InfoItem label="Director" value={tournament.directorName} />}
            <InfoItem icon={<Trophy className="h-3.5 w-3.5" />} label="Events" value={String(tournament.eventsCount || events.length)} />
            <InfoItem label="Status" value={
              tournament.status === "cancelled" ? "Cancelled" :
              tournament.acceptingEntries ? "Accepting Entries" : "Entries Closed"
            } />
          </div>
        </CardContent>
      </Card>

      {/* Event Selector Pills */}
      {events.length > 0 && (
        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => setSelectedEventId(null)}
            className={cn(
              "inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium transition-colors",
              selectedEventId === null
                ? "bg-primary text-primary-foreground"
                : "bg-muted text-muted-foreground hover:bg-accent hover:text-accent-foreground"
            )}
          >
            <Users className="h-3 w-3" />
            All ({entries.length})
          </button>
          {events.map((ev) => (
            <button
              key={ev.eventId}
              onClick={() => setSelectedEventId(ev.eventId)}
              className={cn(
                "inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium transition-colors",
                selectedEventId === ev.eventId
                  ? "bg-primary text-primary-foreground"
                  : "bg-muted text-muted-foreground hover:bg-accent hover:text-accent-foreground"
              )}
            >
              <BallDot color={ev.ballColor} />
              {eventLabel(ev)}
            </button>
          ))}
        </div>
      )}

      {/* Entries Table */}
      {entriesLoading ? (
        <LoadingSection />
      ) : entries.length === 0 ? (
        <EmptyState title="No entries found" description="No player registrations are available for this tournament yet." />
      ) : (
        <EntriesGrouped entries={entries} eventMap={eventMap} selectedEventId={selectedEventId} />
      )}
    </div>
  )
}

function InfoItem({ icon, label, value }: { icon?: React.ReactNode; label: string; value: string }) {
  return (
    <div className="flex flex-col gap-0.5">
      <span className="text-xs text-muted-foreground">{label}</span>
      <span className="flex items-center gap-1 text-sm font-medium">
        {icon}
        {value}
      </span>
    </div>
  )
}

type StatusGroup = "accepted" | "alternate" | "withdrawn" | "other"

function classifyStatus(status: string | null): StatusGroup {
  if (!status) return "other"
  const s = status.toUpperCase()
  if (s.includes("DIRECT") || s === "REGISTERED") return "accepted"
  if (s.includes("ALTERNATE")) return "alternate"
  if (s.includes("WITHDRAWN")) return "withdrawn"
  return "other"
}

const STATUS_GROUP_CONFIG: Record<StatusGroup, { label: string; color: string; headerColor: string }> = {
  accepted:  { label: "Acceptance", color: "bg-primary/10 text-primary", headerColor: "text-primary" },
  alternate: { label: "Alternates", color: "bg-accent text-accent-foreground", headerColor: "text-accent-foreground" },
  withdrawn: { label: "Withdrawn",  color: "bg-destructive/10 text-destructive", headerColor: "text-destructive" },
  other:     { label: "Other",      color: "bg-muted text-muted-foreground", headerColor: "text-muted-foreground" },
}

const STATUS_GROUP_ORDER: StatusGroup[] = ["accepted", "alternate", "withdrawn", "other"]

/** A display row: either one player (singles) or two partners (doubles). */
interface DisplayRow {
  entries: TournamentEntry[]
  eventId: string
  entryPosition: number | null
  entryStatus: string | null
  /** Sum of ranking points for all entries in the row (for sorting). */
  totalPoints: number
}

/** Build display rows from raw entries.
 *
 *  The scraper creates three kinds of entries for each doubles team:
 *    1. A "team summary" (playerName = "Racic/Yamamoto", firstName empty)
 *    2. Two individual entries with full player details
 *
 *  Team summaries carry the pairing info (which last-names go together).
 *  Individual entries carry the player details (UAID, city/state).
 *
 *  Strategy:
 *    - Use team summaries as a pairing lookup: parse the two last-names
 *      and match each to an individual entry with the same eventId + drawId.
 *    - Matched pairs become a single display row with two entries.
 *    - Unmatched individuals become solo rows.
 *    - Singles events are passed through as-is. */
function buildDisplayRows(entries: TournamentEntry[]): DisplayRow[] {
  const rows: DisplayRow[] = []
  const teamEntries: TournamentEntry[] = []
  const individualsByKey = new Map<string, TournamentEntry[]>() // key = eventId::drawId::lastNameLower
  const singlesOrOther: TournamentEntry[] = []

  // Classify entries
  for (const e of entries) {
    const isDoubles = (e.eventType || "").toUpperCase().includes("DOUBLES")
    const isTeam = (e.playerName || "").includes("/") && !e.firstName?.trim()

    if (isDoubles && isTeam) {
      teamEntries.push(e)
    } else if (isDoubles && e.firstName?.trim()) {
      const ln = (e.lastName || "").trim().toLowerCase()
      const key = `${e.eventId}::${e.drawId || ""}::${ln}`
      if (!individualsByKey.has(key)) individualsByKey.set(key, [])
      individualsByKey.get(key)!.push(e)
    } else {
      singlesOrOther.push(e)
    }
  }

  // Pair doubles using team entries as lookup
  const pairedIds = new Set<string>() // participantIds already used in a pair

  for (const team of teamEntries) {
    const names = (team.playerName || "").split("/").map(n => n.trim().toLowerCase())
    if (names.length !== 2) continue

    const drawId = team.drawId || ""
    const pair: TournamentEntry[] = []

    for (const ln of names) {
      const key = `${team.eventId}::${drawId}::${ln}`
      const candidates = individualsByKey.get(key)
      if (candidates) {
        // Prefer candidate whose entryPosition matches the team's (disambiguates
        // duplicate last names like Liu at positions 3, 16, 17).
        const unused = candidates.filter(c => !pairedIds.has(c.participantId || ""))
        const pick =
          unused.find(c => c.entryPosition === team.entryPosition) || unused[0]
        if (pick) {
          pair.push(pick)
          if (pick.participantId) pairedIds.add(pick.participantId)
        }
      }
    }

    if (pair.length === 2) {
      rows.push({
        entries: pair,
        eventId: team.eventId,
        entryPosition: team.entryPosition,
        entryStatus: team.entryStatus,
        totalPoints: sumPoints(pair),
      })
    } else {
      // Couldn't fully match — fall back to showing team summary entry
      rows.push({
        entries: [team],
        eventId: team.eventId,
        entryPosition: team.entryPosition,
        entryStatus: team.entryStatus,
        totalPoints: sumPoints([team]),
      })
    }
  }

  // Add unmatched individual doubles entries as solo rows
  for (const list of individualsByKey.values()) {
    for (const e of list) {
      if (!pairedIds.has(e.participantId || "")) {
        rows.push({
          entries: [e],
          eventId: e.eventId,
          entryPosition: e.entryPosition,
          entryStatus: e.entryStatus,
          totalPoints: sumPoints([e]),
        })
      }
    }
  }

  // Add singles / non-doubles entries
  for (const e of singlesOrOther) {
    rows.push({
      entries: [e],
      eventId: e.eventId,
      entryPosition: e.entryPosition,
      entryStatus: e.entryStatus,
      totalPoints: sumPoints([e]),
    })
  }

  // Sort by entryPosition ascending (USTA acceptance list order)
  // Only sort when entryPosition data is available; otherwise preserve backend order
  const hasPositions = rows.some(r => r.entryPosition != null)
  if (hasPositions) {
    rows.sort((a, b) => {
      const posA = a.entryPosition ?? Infinity
      const posB = b.entryPosition ?? Infinity
      return posA - posB
    })
  }
  return rows
}

function sumPoints(entries: TournamentEntry[]): number {
  return entries.reduce((sum, e) => sum + (e.rankingPoints || 0), 0)
}

function PlayerLink({ entry }: { entry: TournamentEntry }) {
  const name = entry.firstName && entry.lastName
    ? `${entry.firstName} ${entry.lastName}`
    : entry.playerName || `${entry.firstName || ""} ${entry.lastName || ""}`.trim() || "-"

  if (entry.playerUaid) {
    return (
      <Link to={`/players/${entry.playerUaid}`}
        className="font-medium text-foreground hover:text-primary transition-colors">
        {name}
      </Link>
    )
  }
  return <span className="font-medium">{name}</span>
}

function LocationText({ entry }: { entry: TournamentEntry }) {
  if (entry.city && entry.state) return <>{entry.city}, {entry.state}</>
  return <>{entry.state || entry.city || "-"}</>
}

function EntriesGrouped({ entries, eventMap, selectedEventId }: {
  entries: TournamentEntry[]
  eventMap: Map<string, TournamentEvent>
  selectedEventId: string | null
}) {
  const displayRows = buildDisplayRows(entries)

  const groups = new Map<StatusGroup, DisplayRow[]>()
  for (const row of displayRows) {
    const g = classifyStatus(row.entryStatus)
    if (!groups.has(g)) groups.set(g, [])
    groups.get(g)!.push(row)
  }

  return (
    <div className="space-y-5">
      {STATUS_GROUP_ORDER.filter(g => groups.has(g)).map(g => {
        const cfg = STATUS_GROUP_CONFIG[g]
        const groupRows = groups.get(g)!
        return (
          <div key={g}>
            <div className="flex items-center gap-2 mb-2">
              <h3 className={cn("text-sm font-semibold", cfg.headerColor)}>{cfg.label}</h3>
              <span className={cn("inline-flex items-center rounded-md px-1.5 py-0.5 text-[10px] font-medium", cfg.color)}>
                {groupRows.length}
              </span>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-border text-left text-xs text-muted-foreground">
                    <th className="py-2 px-3 w-10 text-right">#</th>
                    <th className="py-2 px-3">Player</th>
                    <th className="py-2 px-3 text-right">Pts</th>
                    <th className="py-2 px-3 hidden sm:table-cell">City, State</th>
                    {!selectedEventId && <th className="py-2 px-3 hidden md:table-cell">Event</th>}
                  </tr>
                </thead>
                <tbody>
                  {groupRows.map((row, idx) => {
                    const first = row.entries[0]
                    const ev = eventMap.get(row.eventId)
                    const isPair = row.entries.length === 2
                    return (
                      <tr key={`${row.eventId}-${first.participantId || idx}`}
                        className="border-b border-border/50 hover:bg-muted/30 transition-colors">
                        <td className="py-1.5 px-3 text-right font-mono text-xs text-muted-foreground align-top">
                          {row.entryPosition ?? idx + 1}
                        </td>
                        <td className="py-1.5 px-3">
                          {isPair ? (
                            <span className="inline-flex items-center gap-1 flex-wrap">
                              <PlayerLink entry={row.entries[0]} />
                              <span className="text-muted-foreground">/</span>
                              <PlayerLink entry={row.entries[1]} />
                            </span>
                          ) : (
                            <PlayerLink entry={first} />
                          )}
                        </td>
                        <td className="py-1.5 px-3 text-right font-mono text-xs text-muted-foreground align-top">
                          {isPair ? (
                            <span>
                              {row.entries[0].rankingPoints ?? "-"}
                              {" / "}
                              {row.entries[1].rankingPoints ?? "-"}
                            </span>
                          ) : (
                            <span>{first.rankingPoints ?? "-"}</span>
                          )}
                        </td>
                        <td className="py-1.5 px-3 text-muted-foreground hidden sm:table-cell align-top">
                          {isPair ? (
                            <span className="inline-flex items-center gap-1 flex-wrap">
                              <LocationText entry={row.entries[0]} />
                              <span>/</span>
                              <LocationText entry={row.entries[1]} />
                            </span>
                          ) : (
                            <LocationText entry={first} />
                          )}
                        </td>
                        {!selectedEventId && (
                          <td className="py-1.5 px-3 text-xs text-muted-foreground hidden md:table-cell align-top">
                            {ev ? eventLabel(ev) : first.eventType || "-"}
                          </td>
                        )}
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )
      })}
    </div>
  )
}
