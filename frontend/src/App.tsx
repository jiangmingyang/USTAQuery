import { BrowserRouter, Routes, Route } from "react-router-dom"
import { AppShell } from "@/components/layout/AppShell"
import { HomePage } from "@/pages/HomePage"
import { SearchResultsPage } from "@/pages/SearchResultsPage"
import { PlayerProfilePage } from "@/pages/PlayerProfilePage"
import { TournamentBrowserPage } from "@/pages/TournamentBrowserPage"
import { TournamentDetailPage } from "@/pages/TournamentDetailPage"
import { RankingsPage } from "@/pages/RankingsPage"
import { NotFoundPage } from "@/pages/NotFoundPage"

function App() {
  return (
    <BrowserRouter>
      <AppShell>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/search" element={<SearchResultsPage />} />
          <Route path="/players/:uaid" element={<PlayerProfilePage />} />
          <Route path="/tournaments" element={<TournamentBrowserPage />} />
          <Route path="/tournaments/:id" element={<TournamentDetailPage />} />
          <Route path="/rankings" element={<RankingsPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AppShell>
    </BrowserRouter>
  )
}

export default App