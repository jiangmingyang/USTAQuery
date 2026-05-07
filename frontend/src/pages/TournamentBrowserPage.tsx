import { useState, useEffect, useRef } from "react"
import { useSearchParams, Link } from "react-router-dom"
import { searchTournaments, getTournamentFilters } from "@/api/client"
import type { PagedResponse, Tournament, TournamentFilterOptions } from "@/types"
import { Card, CardContent } from "@/components/ui/card"
import { Pagination } from "@/components/shared/Pagination"
import { LoadingSection, EmptyState, ErrorAlert, LevelBadge } from "@/components/shared/StatusComponents"
import { formatDate } from "@/lib/utils"
import { MapPin, Calendar, ChevronDown, X } from "lucide-react"

function parseMulti(val: string | null): string[] {
  if (!val) return []
  return val.split(",").filter(Boolean)
}

const MAIN_AGE_GROUPS = ["U8", "U10", "U12", "U14", "U16", "U18"]
const AGE_FILTER_OPTIONS = [...MAIN_AGE_GROUPS, "Other"]

export function TournamentBrowserPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [data, setData] = useState<PagedResponse<Tournament> | null>(null)
  const [filters, setFilters] = useState<TournamentFilterOptions | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const section = parseMulti(searchParams.get("section"))
  const year = searchParams.get("year") || "2026"
  const level = parseMulti(searchParams.get("level"))
  const gender = parseMulti(searchParams.get("gender"))
  const ageCategory = parseMulti(searchParams.get("ageCategory"))
  const eventType = parseMulti(searchParams.get("eventType"))
  const q = searchParams.get("q") || ""
  const page = parseInt(searchParams.get("page") || "0")

  // Serialize multi-select arrays for useEffect dependency
  const sectionKey = section.join(",")
  const levelKey = level.join(",")
  const genderKey = gender.join(",")
  const ageCategoryKey = ageCategory.join(",")
  const eventTypeKey = eventType.join(",")

  useEffect(() => {
    getTournamentFilters().then(setFilters).catch(() => {})
  }, [])

  useEffect(() => {
    setLoading(true)
    setError(null)

    // Resolve "Other" in ageCategory to actual non-standard values from filters
    let resolvedAgeCategory = ageCategory
    if (ageCategory.includes("Other") && filters) {
      const otherValues = (filters.ageCategories || []).filter((a) => !MAIN_AGE_GROUPS.includes(a))
      resolvedAgeCategory = [
        ...ageCategory.filter((a) => a !== "Other"),
        ...otherValues,
      ]
    }

    searchTournaments({
      q: q || undefined,
      section: section.length ? section : undefined,
      level: level.length ? level : undefined,
      year: year ? parseInt(year) : undefined,
      gender: gender.length ? gender : undefined,
      ageCategory: resolvedAgeCategory.length ? resolvedAgeCategory : undefined,
      eventType: eventType.length ? eventType : undefined,
      page,
      size: 20,
    })
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sectionKey, year, levelKey, genderKey, ageCategoryKey, eventTypeKey, q, page])

  function updateMultiParam(key: string, values: string[]) {
    const params = new URLSearchParams(searchParams)
    if (values.length > 0) {
      params.set(key, values.join(","))
    } else {
      params.delete(key)
    }
    if (key !== "page") params.set("page", "0")
    setSearchParams(params)
  }

  function updateParam(key: string, value: string) {
    const params = new URLSearchParams(searchParams)
    if (value) {
      params.set(key, value)
    } else {
      params.delete(key)
    }
    if (key !== "page") params.set("page", "0")
    setSearchParams(params)
  }

  function clearFilters() {
    setSearchParams({ year: "2026" })
  }

  const hasFilters = section.length || level.length || gender.length || ageCategory.length || eventType.length || q

  return (
    <div className="container py-8">
      <h1 className="text-2xl font-bold mb-6">Tournaments</h1>

      {/* Filter bar */}
      <div className="mb-6 flex flex-wrap gap-3">
        <MultiSelect
          label="Section"
          selected={section}
          onChange={(v) => updateMultiParam("section", v)}
          options={filters?.sections || []}
          placeholder="All Sections"
          minWidth="180px"
        />
        <div>
          <label className="text-xs font-medium text-muted-foreground block mb-1.5">Year</label>
          <select
            value={year}
            onChange={(e) => updateParam("year", e.target.value)}
            className="h-9 rounded-md border bg-background px-3 text-sm min-w-[90px]"
          >
            {[2026, 2025, 2024, 2023].map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </div>
        <MultiSelect
          label="Level"
          selected={level}
          onChange={(v) => updateMultiParam("level", v)}
          options={filters?.levels || []}
          placeholder="All Levels"
          minWidth="160px"
        />
        <MultiSelect
          label="Gender"
          selected={gender}
          onChange={(v) => updateMultiParam("gender", v)}
          options={filters?.genders || []}
          placeholder="All"
          minWidth="100px"
        />
        <MultiSelect
          label="Age Group"
          selected={ageCategory}
          onChange={(v) => updateMultiParam("ageCategory", v)}
          options={AGE_FILTER_OPTIONS}
          placeholder="All"
          minWidth="100px"
        />
        <MultiSelect
          label="Event Type"
          selected={eventType}
          onChange={(v) => updateMultiParam("eventType", v)}
          options={filters?.eventTypes || []}
          placeholder="All"
          minWidth="100px"
        />
      </div>

      {/* Search bar */}
      <div className="mb-4 flex items-center gap-3">
        <input
          type="text"
          placeholder="Search by tournament name..."
          defaultValue={q}
          onKeyDown={(e) => {
            if (e.key === "Enter") updateParam("q", (e.target as HTMLInputElement).value)
          }}
          className="h-9 flex-1 max-w-md rounded-md border bg-background px-3 text-sm"
        />
        {hasFilters ? (
          <button onClick={clearFilters} className="text-xs text-muted-foreground hover:text-foreground transition-colors">
            Clear filters
          </button>
        ) : null}
      </div>

      {/* Results */}
      {loading && <LoadingSection />}
      {error && <ErrorAlert message={error} />}
      {!loading && data && data.content.length === 0 && (
        <EmptyState title="No tournaments found" description="Try adjusting your filters" />
      )}
      {data && data.content.length > 0 && (
        <>
          <p className="text-sm text-muted-foreground mb-4">{data.totalElements} tournament(s)</p>
          <div className="space-y-3">
            {data.content.map((t) => (
              <TournamentCard key={t.id} tournament={t} />
            ))}
          </div>
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            onPageChange={(p) => updateParam("page", String(p))}
          />
        </>
      )}
    </div>
  )
}

function MultiSelect({ label, selected, onChange, options, placeholder, minWidth }: {
  label: string
  selected: string[]
  onChange: (v: string[]) => void
  options: string[]
  placeholder: string
  minWidth: string
}) {
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener("mousedown", handleClick)
    return () => document.removeEventListener("mousedown", handleClick)
  }, [])

  function toggle(val: string) {
    if (selected.includes(val)) {
      onChange(selected.filter((s) => s !== val))
    } else {
      onChange([...selected, val])
    }
  }

  const displayText = selected.length === 0
    ? placeholder
    : selected.length === 1
      ? selected[0]
      : `${selected.length} selected`

  return (
    <div ref={ref} className="relative">
      <label className="text-xs font-medium text-muted-foreground block mb-1.5">{label}</label>
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="h-9 rounded-md border bg-background px-3 text-sm flex items-center gap-1.5 hover:bg-accent/50 transition-colors"
        style={{ minWidth }}
      >
        <span className={`truncate ${selected.length === 0 ? "text-muted-foreground" : ""}`}>
          {displayText}
        </span>
        {selected.length > 0 ? (
          <X
            className="h-3.5 w-3.5 text-muted-foreground hover:text-foreground flex-shrink-0"
            onClick={(e) => { e.stopPropagation(); onChange([]) }}
          />
        ) : (
          <ChevronDown className="h-3.5 w-3.5 text-muted-foreground flex-shrink-0" />
        )}
      </button>
      {open && options.length > 0 && (
        <div className="absolute z-50 mt-1 w-max min-w-full max-h-60 overflow-auto rounded-md border bg-popover shadow-md">
          {options.map((o) => (
            <label
              key={o}
              className="flex items-center gap-2 px-3 py-1.5 text-sm hover:bg-accent cursor-pointer"
            >
              <input
                type="checkbox"
                checked={selected.includes(o)}
                onChange={() => toggle(o)}
                className="rounded border-muted-foreground"
              />
              <span className="truncate">{o}</span>
            </label>
          ))}
        </div>
      )}
    </div>
  )
}

function TournamentCard({ tournament: t }: { tournament: Tournament }) {
  return (
    <Link to={`/tournaments/${t.id}`} className="block">
      <Card className="group cursor-pointer hover:border-primary/30">
        <CardContent className="p-4">
          <div className="flex items-start justify-between gap-3 mb-2">
            <div className="min-w-0">
              <h3 className="font-semibold text-sm leading-tight group-hover:text-primary transition-colors">
                {t.name}
              </h3>
              {t.code && <p className="text-xs text-muted-foreground mt-0.5">{t.code}</p>}
            </div>
            <div className="flex-shrink-0">
              <LevelBadge level={t.level} />
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <Calendar className="h-3 w-3" />
              {formatDate(t.startDate)}{t.endDate ? ` \u2013 ${formatDate(t.endDate)}` : ""}
            </span>
            {(t.city || t.state) && (
              <span className="flex items-center gap-1">
                <MapPin className="h-3 w-3" />
                {t.city && t.state ? `${t.city}, ${t.state}` : t.state || t.city}
              </span>
            )}
            {t.section && <span>{t.section}</span>}
            {t.status === "cancelled" && (
              <span className="text-destructive font-medium">Cancelled</span>
            )}
          </div>
          {t.organization && (
            <p className="text-xs text-muted-foreground mt-1">Org: {t.organization}</p>
          )}
          {/* Events grouped by gender */}
          {t.events && t.events.length > 0 && <EventsSummary events={t.events} />}
        </CardContent>
      </Card>
    </Link>
  )
}

import type { TournamentEvent } from "@/types"

const GENDER_ORDER = ["Boys", "Girls", "Coed", "Mixed"] as const
const GENDER_MAP: Record<string, string> = {
  Male: "Boys", Female: "Girls", Coed: "Coed", Mixed: "Mixed",
  male: "Boys", female: "Girls", coed: "Coed", mixed: "Mixed",
  Boys: "Boys", Girls: "Girls",
}
const BALL_COLOR: Record<string, string> = {
  Yellow: "#eab308", Green: "#22c55e", Orange: "#f97316", Red: "#ef4444",
  yellow: "#eab308", green: "#22c55e", orange: "#f97316", red: "#ef4444",
}

function formatEventLabel(ev: TournamentEvent): string {
  const age = ev.ageCategory || ""
  const type = ev.eventType?.toLowerCase().startsWith("s") ? "S" : ev.eventType?.toLowerCase().startsWith("d") ? "D" : ""
  return [age, type].filter(Boolean).join(" ")
}

function BallDot({ color }: { color: string | null }) {
  if (!color) return null
  const hex = BALL_COLOR[color] || BALL_COLOR[color.toLowerCase()] || null
  if (!hex) return null
  return <span className="inline-block h-2 w-2 rounded-full shrink-0" style={{ backgroundColor: hex }} />
}

function EventsSummary({ events }: { events: TournamentEvent[] }) {
  const grouped = new Map<string, TournamentEvent[]>()
  for (const ev of events) {
    const label = GENDER_MAP[ev.gender || ""] || ev.gender || "Other"
    if (!grouped.has(label)) grouped.set(label, [])
    grouped.get(label)!.push(ev)
  }

  const sorted = GENDER_ORDER
    .filter((g) => grouped.has(g))
    .map((g) => [g, grouped.get(g)!] as const)
  // append any remaining categories not in GENDER_ORDER
  for (const [label, evts] of grouped) {
    if (!GENDER_ORDER.includes(label as typeof GENDER_ORDER[number])) {
      sorted.push([label, evts])
    }
  }

  if (sorted.length === 0) return null

  return (
    <div className="mt-2 pt-2 border-t space-y-1">
      {sorted.map(([label, evts]) => (
        <div key={label} className="flex items-baseline gap-2 text-[11px]">
          <span className="font-medium text-muted-foreground w-10 shrink-0">{label}</span>
          <div className="flex flex-wrap gap-1.5">
            {evts.map((ev) => (
              <span key={ev.eventId} className="inline-flex items-center gap-1 rounded bg-muted px-1.5 py-0.5 text-muted-foreground">
                {formatEventLabel(ev)}
                <BallDot color={ev.ballColor} />
              </span>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}
