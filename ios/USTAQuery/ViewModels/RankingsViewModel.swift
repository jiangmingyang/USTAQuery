import Foundation

@Observable
final class RankingsViewModel {
    static let shared = RankingsViewModel()

    private enum Keys {
        static let listKey = "ustaquery_rankings_listKey"
        static let gender = "ustaquery_rankings_gender"
        static let age = "ustaquery_rankings_age"
        static let section = "ustaquery_rankings_section"
    }

    var listKey: String {
        didSet { UserDefaults.standard.set(listKey, forKey: Keys.listKey) }
    }
    var gender: String {
        didSet { UserDefaults.standard.set(gender, forKey: Keys.gender) }
    }
    var ageRestriction: String {
        didSet { UserDefaults.standard.set(ageRestriction, forKey: Keys.age) }
    }
    var page = 0
    var publishDate = ""

    var data: PagedResponse<Ranking>?
    var versions: [String] = []
    var sections: [String] = []
    var isLoading = false
    var error: String?

    var sectionFilter: String {
        didSet {
            UserDefaults.standard.set(sectionFilter.isEmpty ? nil : sectionFilter, forKey: Keys.section)
        }
    }

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

    private var lastVersionsCatalogId = ""

    init() {
        let defaults = UserDefaults.standard
        self.listKey = defaults.string(forKey: Keys.listKey) ?? "STANDING"
        self.gender = defaults.string(forKey: Keys.gender) ?? "M"
        self.ageRestriction = defaults.string(forKey: Keys.age) ?? "Y12"
        self.sectionFilter = defaults.string(forKey: Keys.section) ?? ""
    }

    func loadIfNeeded() async {
        guard data == nil else { return }
        await fetchAll()
    }

    func reload() async {
        lastVersionsCatalogId = ""
        await fetchAll()
    }

    func updateFilter(list: String? = nil, gender g: String? = nil, age: String? = nil, section: String? = nil) {
        let catalogChanging = list != nil || g != nil || age != nil
        if let list { listKey = list }
        if let g { gender = g }
        if let age { ageRestriction = age }
        if let section { sectionFilter = section }
        if catalogChanging {
            page = 0
            publishDate = ""
        }
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
            await fetchSections(currentCatalogId)
        }
        await fetchLeaderboard()
    }

    private func fetchVersions(_ catId: String) async {
        do {
            versions = try await APIClient.getRankingVersions(catalogId: catId)
            if !versions.isEmpty && publishDate.isEmpty {
                publishDate = versions[0]
            }
        } catch {
            versions = []
        }
    }

    private func fetchSections(_ catId: String) async {
        do {
            sections = try await APIClient.getRankingSections(catalogId: catId)
        } catch {
            sections = []
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
                publishDate: publishDate.isEmpty ? nil : publishDate,
                section: sectionFilter.isEmpty ? nil : sectionFilter
            )
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }
}
