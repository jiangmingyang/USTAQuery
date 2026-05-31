import { useSearchParams, useNavigate, Link } from "react-router-dom"
import { useState, useEffect } from "react"
import { Trophy } from "lucide-react"
import { SearchBar } from "@/components/shared/SearchBar"
import { PlayerCard } from "@/components/player/PlayerCard"
import { Card, CardContent } from "@/components/ui/card"
import { LoadingSection, EmptyState, ErrorAlert } from "@/components/shared/StatusComponents"
import { unifiedSearch } from "@/api/client"
import type { UnifiedSearchResponse, Tournament } from "@/types"

export function SearchResultsPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const query = searchParams.get("q") || ""

  const [data, setData] = useState<UnifiedSearchResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!query.trim()) return
    setLoading(true)
    setError(null)
    unifiedSearch(query, 0, 10)
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [query])

  function handleSearch(q: string) {
    navigate(`/search?q=${encodeURIComponent(q)}`)
  }

  const playerCount = data?.players?.totalElements ?? 0
  const tournamentCount = data?.tournaments?.totalElements ?? 0
  const hasResults = playerCount > 0 || tournamentCount > 0

  return (
    <div className="container py-8">
      <SearchBar
        onSearch={handleSearch}
        defaultValue={query}
        className="mb-6 max-w-xl"
        placeholder="Search players or tournaments..."
      />

      {query && !loading && !error && data && (
        <p className="text-sm text-muted-foreground mb-6">
          {playerCount} player{playerCount !== 1 ? "s" : ""} · {tournamentCount} tournament{tournamentCount !== 1 ? "s" : ""} found
        </p>
      )}

      {loading && <LoadingSection />}
      {error && <ErrorAlert message={error} />}

      {!loading && !error && data && !hasResults && (
        <EmptyState title="No results found" description="Try a different name, UAID, or tournament code" />
      )}

      {!loading && !error && data && hasResults && (
        <div className="space-y-8">
          {playerCount > 0 && (
            <section>
              <h2 className="text-base font-semibold mb-3">
                Players
                {playerCount > (data.players?.content.length ?? 0) && (
                  <span className="text-sm font-normal text-muted-foreground ml-2">
                    (showing {data.players?.content.length} of {playerCount})
                  </span>
                )}
              </h2>
              <div className="space-y-2">
                {data.players?.content.map((p) => (
                  <PlayerCard key={p.uaid} player={p} />
                ))}
              </div>
              {playerCount > (data.players?.content.length ?? 0) && (
                <p className="text-xs text-muted-foreground mt-2">
                  <Link
                    to={`/search?q=${encodeURIComponent(query)}&type=players`}
                    className="underline hover:text-primary"
                  >
                    View all {playerCount} players
                  </Link>
                </p>
              )}
            </section>
          )}

          {tournamentCount > 0 && (
            <section>
              <h2 className="text-base font-semibold mb-3">
                Tournaments
                {tournamentCount > (data.tournaments?.content.length ?? 0) && (
                  <span className="text-sm font-normal text-muted-foreground ml-2">
                    (showing {data.tournaments?.content.length} of {tournamentCount})
                  </span>
                )}
              </h2>
              <div className="space-y-2">
                {data.tournaments?.content.map((t) => (
                  <TournamentRow key={t.id} tournament={t} />
                ))}
              </div>
              {tournamentCount > (data.tournaments?.content.length ?? 0) && (
                <p className="text-xs text-muted-foreground mt-2">
                  <Link
                    to={`/tournaments?q=${encodeURIComponent(query)}`}
                    className="underline hover:text-primary"
                  >
                    View all {tournamentCount} tournaments
                  </Link>
                </p>
              )}
            </section>
          )}
        </div>
      )}
    </div>
  )
}

function TournamentRow({ tournament }: { tournament: Tournament }) {
  const startYear = tournament.startDate ? tournament.startDate.slice(0, 4) : null
  const location = [tournament.city, tournament.state].filter(Boolean).join(", ")

  return (
    <Link to={`/tournaments/${tournament.id}`}>
      <Card className="group cursor-pointer transition-all duration-200 hover:border-primary/30">
        <CardContent className="p-4 flex items-center gap-4">
          <div className="h-11 w-11 rounded-full bg-accent flex items-center justify-center flex-shrink-0">
            <Trophy className="h-5 w-5 text-accent-foreground group-hover:text-primary transition-colors" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="font-semibold truncate group-hover:text-primary transition-colors">
              {tournament.name}
            </p>
            <p className="text-xs text-muted-foreground">
              {[location, startYear].filter(Boolean).join(" · ")}
              {tournament.code && ` · ${tournament.code}`}
            </p>
          </div>
          <div className="hidden sm:flex items-center gap-3 text-right">
            {tournament.level && (
              <div>
                <p className="text-xs text-muted-foreground">Level</p>
                <p className="font-mono font-semibold text-sm">{tournament.level}</p>
              </div>
            )}
            {tournament.registrationStatus && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-accent text-accent-foreground">
                {tournament.registrationStatus}
              </span>
            )}
          </div>
        </CardContent>
      </Card>
    </Link>
  )
}
