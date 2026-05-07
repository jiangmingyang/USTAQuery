import { useSearchParams, useNavigate } from "react-router-dom"
import { useState, useEffect } from "react"
import { SearchBar } from "@/components/shared/SearchBar"
import { PlayerCard } from "@/components/player/PlayerCard"
import { LoadingSection, EmptyState, ErrorAlert } from "@/components/shared/StatusComponents"
import { Pagination } from "@/components/shared/Pagination"
import { searchPlayers } from "@/api/client"
import type { PagedResponse, PlayerSummary } from "@/types"

export function SearchResultsPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const query = searchParams.get("q") || ""
  const page = parseInt(searchParams.get("page") || "0")

  const [data, setData] = useState<PagedResponse<PlayerSummary> | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!query) return
    setLoading(true)
    setError(null)
    searchPlayers(query, page)
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [query, page])

  function handleSearch(q: string) {
    navigate(`/search?q=${encodeURIComponent(q)}`)
  }

  function handlePageChange(p: number) {
    navigate(`/search?q=${encodeURIComponent(query)}&page=${p}`)
  }

  return (
    <div className="container py-8">
      <SearchBar onSearch={handleSearch} defaultValue={query} className="mb-6 max-w-xl" />

      {query && <h2 className="text-lg font-semibold mb-4">Results for "{query}"</h2>}

      {loading && <LoadingSection />}
      {error && <ErrorAlert message={error} />}
      {data && data.content.length === 0 && <EmptyState title="No players found" description="Try a different name or UAID" />}
      {data && data.content.length > 0 && (
        <>
          <p className="text-sm text-muted-foreground mb-4">{data.totalElements} player(s) found</p>
          <div className="space-y-2">
            {data.content.map((p) => (
              <PlayerCard key={p.uaid} player={p} />
            ))}
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onPageChange={handlePageChange} />
        </>
      )}
    </div>
  )
}