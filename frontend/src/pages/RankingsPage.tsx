import { useState, useEffect } from "react"
import { useSearchParams } from "react-router-dom"
import { getLeaderboard, getRankingVersions } from "@/api/client"
import type { PagedResponse, Ranking } from "@/types"
import { Pagination } from "@/components/shared/Pagination"
import { LoadingSection, EmptyState, ErrorAlert } from "@/components/shared/StatusComponents"
import { cn } from "@/lib/utils"
import { Link } from "react-router-dom"

const LIST_TYPES = [
  { value: "STANDING", label: "Combined National Standing List", catalogPattern: "JUNIOR_NULL_{G}_STANDING_{A}_UNDER_NULL_NULL_NULL" },
  { value: "SEEDING_SINGLES", label: "Singles Seeding List", catalogPattern: "JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_SINGLES_NULL_NULL" },
  { value: "SEEDING_DOUBLES", label: "Doubles Seeding List", catalogPattern: "JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_DOUBLES_INDIVIDUAL_NULL" },
  { value: "BONUS_POINTS", label: "Bonus Points List", catalogPattern: "JUNIOR_NULL_{G}_BONUS_POINTS_{A}_UNDER_NULL_NULL_NULL" },
  { value: "QUOTA", label: "Quota List", catalogPattern: "JUNIOR_NULL_{G}_QUOTA_{A}_UNDER_NULL_NULL_S05" },
  { value: "YEAR_END_COMBINED", label: "Final Year End Combined Rank List", catalogPattern: "JUNIOR_NULL_{G}_YEAR_END_{A}_UNDER_NULL_NULL_NULL" },
  { value: "YEAR_END_DOUBLES", label: "Final Year End Doubles Rank List", catalogPattern: "JUNIOR_NULL_{G}_YEAR_END_{A}_UNDER_DOUBLES_INDIVIDUAL_NULL" },
] as const

function buildCatalogId(pattern: string, gender: string, age: string): string {
  return pattern.replace("{G}", gender).replace("{A}", age)
}

const GENDERS = [
  { value: "M", label: "Boys" },
  { value: "F", label: "Girls" },
]

const AGE_GROUPS = [
  { value: "Y12", label: "12 & Under" },
  { value: "Y14", label: "14 & Under" },
  { value: "Y16", label: "16 & Under" },
  { value: "Y18", label: "18 & Under" },
]

export function RankingsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [data, setData] = useState<PagedResponse<Ranking> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [versions, setVersions] = useState<string[]>([])

  const listKey = searchParams.get("list") || "STANDING"
  const gender = searchParams.get("gender") || "M"
  const ageRestriction = searchParams.get("ageRestriction") || "Y12"
  const page = parseInt(searchParams.get("page") || "0")
  const publishDate = searchParams.get("publishDate") || ""

  const selectedList = LIST_TYPES.find((t) => t.value === listKey) ?? LIST_TYPES[0]

  function updateParam(key: string, value: string) {
    const params = new URLSearchParams(searchParams)
    params.set(key, value)
    if (key !== "page") params.set("page", "0")
    setSearchParams(params)
  }

  const catalogId = buildCatalogId(selectedList.catalogPattern, gender, ageRestriction)

  // Fetch available versions when catalogId changes
  useEffect(() => {
    getRankingVersions(catalogId)
      .then(setVersions)
      .catch(() => setVersions([]))
  }, [catalogId])

  useEffect(() => {
    setLoading(true)
    setError(null)
    getLeaderboard({ catalogId, page, publishDate: publishDate || undefined })
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [catalogId, page, publishDate])

  const genderLabel = GENDERS.find((g) => g.value === gender)?.label ?? gender
  const ageLabel = AGE_GROUPS.find((a) => a.value === ageRestriction)?.label ?? ageRestriction

  return (
    <div className="container py-8">
      <h1 className="text-2xl font-bold mb-6">Rankings Leaderboard</h1>

      {/* Filter selectors */}
      <div className="mb-6 flex flex-wrap gap-3">
        <div>
          <label className="text-xs font-medium text-muted-foreground block mb-1.5">Ranking List</label>
          <select
            value={listKey}
            onChange={(e) => updateParam("list", e.target.value)}
            className="h-9 rounded-md border bg-background px-3 text-sm min-w-[260px]"
          >
            {LIST_TYPES.map((t) => (
              <option key={t.value} value={t.value}>{t.label}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="text-xs font-medium text-muted-foreground block mb-1.5">Gender</label>
          <select
            value={gender}
            onChange={(e) => updateParam("gender", e.target.value)}
            className="h-9 rounded-md border bg-background px-3 text-sm min-w-[100px]"
          >
            {GENDERS.map((g) => (
              <option key={g.value} value={g.value}>{g.label}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="text-xs font-medium text-muted-foreground block mb-1.5">Age Group</label>
          <select
            value={ageRestriction}
            onChange={(e) => updateParam("ageRestriction", e.target.value)}
            className="h-9 rounded-md border bg-background px-3 text-sm min-w-[120px]"
          >
            {AGE_GROUPS.map((a) => (
              <option key={a.value} value={a.value}>{a.label}</option>
            ))}
          </select>
        </div>
        {versions.length > 0 && (
          <div>
            <label className="text-xs font-medium text-muted-foreground block mb-1.5">Version</label>
            <select
              value={publishDate}
              onChange={(e) => updateParam("publishDate", e.target.value)}
              className="h-9 rounded-md border bg-background px-3 text-sm min-w-[180px]"
            >
              <option value="">Latest</option>
              {versions.map((v) => (
                <option key={v} value={v}>{new Date(v).toLocaleDateString()}</option>
              ))}
            </select>
          </div>
        )}
      </div>

      {/* Summary */}
      {data && data.content.length > 0 && (
        <p className="text-sm text-muted-foreground mb-4">
          {genderLabel} {ageLabel} &mdash; {selectedList.label} &middot; {data.totalElements} players
        </p>
      )}

      {/* Leaderboard Table */}
      {loading && <LoadingSection />}
      {error && <ErrorAlert message={error} />}
      {!loading && data && data.content.length === 0 && <EmptyState title="No rankings data" description="No results for this selection" />}
      {data && data.content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-lg border">
            <table className="w-full">
              <thead>
                <tr className="bg-muted/50">
                  <th className="table-header text-left py-3 px-4">Player</th>
                  <th className="table-header text-center py-3 px-4">District</th>
                  <th className="table-header text-center py-3 px-4">Section</th>
                  <th className="table-header text-center py-3 px-4">National</th>
                  <th className="table-header text-right py-3 px-4">Points</th>
                  <th className="table-header text-center py-3 px-4">W/L</th>
                  <th className="table-header text-center py-3 px-4">Trend</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((r) => (
                  <tr key={r.id} className="border-b border-border/50 hover:bg-muted/30 transition-colors">
                    <td className="table-cell">
                      <Link to={`/players/${r.playerUaid}`} className="font-medium hover:text-primary transition-colors">
                        {r.playerFirstName} {r.playerLastName}
                      </Link>
                      {r.section && <p className="text-xs text-muted-foreground">{r.section}{r.district ? ` \u00b7 ${r.district}` : ""}</p>}
                    </td>
                    <td className="table-cell text-center font-mono text-sm">{r.districtRank ?? "\u2014"}</td>
                    <td className="table-cell text-center font-mono text-sm">{r.sectionRank ?? "\u2014"}</td>
                    <td className="table-cell text-center font-mono font-bold text-sm">
                      <span className={cn(r.nationalRank != null && r.nationalRank <= 3 ? "text-primary" : "")}>
                        {r.nationalRank ?? "\u2014"}
                      </span>
                    </td>
                    <td className="table-cell text-right font-mono text-sm">{r.points ?? "\u2014"}</td>
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
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            onPageChange={(p) => {
              const params = new URLSearchParams(searchParams)
              params.set("page", String(p))
              setSearchParams(params)
            }}
          />
        </>
      )}
    </div>
  )
}
