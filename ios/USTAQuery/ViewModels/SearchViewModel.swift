import SwiftUI

@Observable
final class SearchViewModel {
    var query = ""
    var results: PagedResponse<PlayerSummary>?
    var isLoading = false
    var error: String?
    var currentPage = 0

    func search() async {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        isLoading = true
        error = nil
        do {
            results = try await APIClient.searchPlayers(query: query, page: currentPage)
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }

    func loadPage(_ page: Int) async {
        currentPage = page
        await search()
    }
}
