import SwiftUI

enum PlayerTab: String, CaseIterable {
    case info = "Info"
    case tournaments = "Tournaments"
    case registrations = "Registrations"
    case matches = "Matches"
    case rankings = "Rankings"
}

@Observable
final class PlayerProfileViewModel {
    let uaid: String
    var player: PlayerDetail?
    var stats: PlayerStats?
    var isLoading = false
    var error: String?
    var selectedTab: PlayerTab = .info

    // Tournaments tab
    var tournamentEntries: [PlayerTournamentEntry] = []
    var tournamentsLoading = false

    // Registrations tab
    var registrations: PagedResponse<Registration>?
    var registrationsPage = 0
    var registrationsLoading = false

    // Matches tab
    var matches: PagedResponse<Match>?
    var matchesPage = 0
    var matchesLoading = false

    // Rankings tab
    var rankings: [Ranking] = []
    var rankingsLoading = false
    var selectedAgeRestriction = "All"
    var selectedListType = ""

    // Track which tabs have been loaded
    private var loadedTabs: Set<PlayerTab> = []

    init(uaid: String) {
        self.uaid = uaid
    }

    func loadProfile() async {
        isLoading = true
        error = nil
        do {
            async let playerReq = APIClient.getPlayer(uaid: uaid)
            async let statsReq = APIClient.getPlayerStats(uaid: uaid)
            let (p, s) = try await (playerReq, statsReq)
            player = p
            stats = s
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }

    func loadTabIfNeeded(_ tab: PlayerTab) async {
        guard !loadedTabs.contains(tab) else { return }
        loadedTabs.insert(tab)
        switch tab {
        case .info: break
        case .tournaments: await loadTournaments()
        case .registrations: await loadRegistrations()
        case .matches: await loadMatches()
        case .rankings: await loadRankings()
        }
    }

    func loadTournaments() async {
        tournamentsLoading = true
        do {
            tournamentEntries = try await APIClient.getPlayerTournamentEntries(uaid: uaid)
        } catch {
            tournamentEntries = []
        }
        tournamentsLoading = false
    }

    func loadRegistrations(page: Int = 0) async {
        registrationsLoading = true
        registrationsPage = page
        do {
            registrations = try await APIClient.getPlayerRegistrations(uaid: uaid, page: page)
        } catch {
            registrations = nil
        }
        registrationsLoading = false
    }

    func loadMatches(page: Int = 0) async {
        matchesLoading = true
        matchesPage = page
        do {
            matches = try await APIClient.getPlayerMatches(uaid: uaid, page: page)
        } catch {
            matches = nil
        }
        matchesLoading = false
    }

    func loadRankings() async {
        rankingsLoading = true
        do {
            let age = selectedAgeRestriction == "All" ? nil : selectedAgeRestriction
            let lt = selectedListType.isEmpty ? nil : selectedListType
            rankings = try await APIClient.getPlayerRankings(uaid: uaid, listType: lt, ageRestriction: age)
        } catch {
            rankings = []
        }
        rankingsLoading = false
    }
}
