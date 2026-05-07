import SwiftUI

@Observable
final class TournamentBrowserViewModel {
    var results: PagedResponse<Tournament>?
    var filters: TournamentFilterOptions?
    var isLoading = false
    var error: String?
    var currentPage = 0

    // Filter state
    var searchText = ""
    var selectedSections: Set<String> = []
    var selectedLevels: Set<String> = []
    var selectedGenders: Set<String> = []
    var selectedAgeCategories: Set<String> = []
    var selectedEventTypes: Set<String> = []
    var selectedYear = "2026"

    private let mainAgeGroups = ["U8", "U10", "U12", "U14", "U16", "U18"]

    func loadFilters() async {
        do {
            filters = try await APIClient.getTournamentFilters()
        } catch {}
    }

    func search() async {
        isLoading = true
        error = nil

        // Resolve "Other" age category
        var resolvedAge = Array(selectedAgeCategories)
        if resolvedAge.contains("Other"), let filters {
            resolvedAge.removeAll { $0 == "Other" }
            let otherValues = filters.ageCategories.filter { !mainAgeGroups.contains($0) }
            resolvedAge.append(contentsOf: otherValues)
        }

        do {
            results = try await APIClient.searchTournaments(
                q: searchText.isEmpty ? nil : searchText,
                section: selectedSections.isEmpty ? nil : Array(selectedSections),
                level: selectedLevels.isEmpty ? nil : Array(selectedLevels),
                year: Int(selectedYear),
                gender: selectedGenders.isEmpty ? nil : Array(selectedGenders),
                ageCategory: resolvedAge.isEmpty ? nil : resolvedAge,
                eventType: selectedEventTypes.isEmpty ? nil : Array(selectedEventTypes),
                page: currentPage
            )
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }

    func loadPage(_ page: Int) async {
        currentPage = page
        await search()
    }

    func clearFilters() {
        selectedSections.removeAll()
        selectedLevels.removeAll()
        selectedGenders.removeAll()
        selectedAgeCategories.removeAll()
        selectedEventTypes.removeAll()
        searchText = ""
    }

    var activeFilterCount: Int {
        var count = 0
        if !selectedSections.isEmpty { count += 1 }
        if !selectedLevels.isEmpty { count += 1 }
        if !selectedGenders.isEmpty { count += 1 }
        if !selectedAgeCategories.isEmpty { count += 1 }
        if !selectedEventTypes.isEmpty { count += 1 }
        return count
    }
}
