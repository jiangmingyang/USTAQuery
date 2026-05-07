import Foundation

@Observable
final class RankingsViewModel {
    var listKey = "STANDING"
    var gender = "M"
    var ageRestriction = "Y12"
    var page = 0
    var publishDate = ""

    var data: PagedResponse<Ranking>?
    var versions: [String] = []
    var isLoading = false
    var error: String?

    var selectedList: (value: String, label: String, catalogPattern: String) {
        AppConstants.listTypes.first(where: { $0.value == listKey }) ?? AppConstants.listTypes[0]
    }

    var catalogId: String {
        AppConstants.buildCatalogId(pattern: selectedList.catalogPattern, gender: gender, age: ageRestriction)
    }

    var genderLabel: String {
        AppConstants.genders.first(where: { $0.value == gender })?.label ?? gender
    }

    var ageLabel: String {
        AppConstants.ageGroupLabels[ageRestriction] ?? ageRestriction
    }

    // Track last fetched catalogId to detect changes
    private var lastVersionsCatalogId = ""

    func loadIfNeeded() async {
        guard data == nil else { return }
        await fetchAll()
    }

    func reload() async {
        lastVersionsCatalogId = ""
        await fetchAll()
    }

    func updateFilter(list: String? = nil, gender g: String? = nil, age: String? = nil) {
        if let list { listKey = list }
        if let g { gender = g }
        if let age { ageRestriction = age }
        page = 0
        publishDate = ""
        Task { await fetchAll() }
    }

    func updateVersion(_ date: String) {
        publishDate = date
        page = 0
        Task { await fetchLeaderboard() }
    }

    func goToPage(_ p: Int) {
        page = p
        Task { await fetchLeaderboard() }
    }

    private func fetchAll() async {
        let currentCatalogId = catalogId
        if currentCatalogId != lastVersionsCatalogId {
            lastVersionsCatalogId = currentCatalogId
            await fetchVersions(currentCatalogId)
        }
        await fetchLeaderboard()
    }

    private func fetchVersions(_ catId: String) async {
        do {
            versions = try await APIClient.getRankingVersions(catalogId: catId)
        } catch {
            versions = []
        }
    }

    private func fetchLeaderboard() async {
        isLoading = true
        error = nil
        do {
            data = try await APIClient.getLeaderboard(
                catalogId: catalogId,
                page: page,
                size: 50,
                publishDate: publishDate.isEmpty ? nil : publishDate
            )
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }
}
