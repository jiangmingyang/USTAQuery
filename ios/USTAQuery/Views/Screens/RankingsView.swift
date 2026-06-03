import SwiftUI

struct RankingsView: View {
    @State var vm: RankingsViewModel = .shared

    var initialGender: String?
    var initialAge: String?

    var body: some View {
        VStack(spacing: 0) {
            filterBar
            leaderboardContent
        }
        .padding(.top, -22)
        .navigationTitle("Rankings")
        .navigationBarTitleDisplayMode(.large)
        .navigationDestination(for: PlayerRoute.self) { route in
            PlayerProfileView(uaid: route.uaid)
        }
        .task {
            if let g = initialGender { vm.gender = g }
            if let a = initialAge { vm.ageRestriction = a }
            await vm.loadIfNeeded()
        }
        .refreshable { await vm.reload() }
    }

    // MARK: - Filter Bar

    private var filterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                FilterMenu(label: "Section", options: [("", "All Sections")] + vm.sections.map { ($0, $0) }, selectedValue: vm.sectionFilter) { val in
                    vm.updateFilter(section: val)
                }
                FilterMenu(label: "List", options: AppConstants.listTypes.map { ($0.value, $0.label) }, selectedValue: vm.listKey) { val in
                    vm.updateFilter(list: val)
                }
                FilterMenu(label: "Gender", options: AppConstants.genders.map { ($0.value, $0.label) }, selectedValue: vm.gender) { val in
                    vm.updateFilter(gender: val)
                }
                FilterMenu(label: "Age", options: AppConstants.ageRestrictions.map { ($0, AppConstants.ageGroupLabels[$0] ?? $0) }, selectedValue: vm.ageRestriction) { val in
                    vm.updateFilter(age: val)
                }
                FilterMenu(label: "Version", options: [("", "Latest")] + vm.versions.map { ($0, formatVersionDate($0)) }, selectedValue: vm.publishDate) { val in
                    vm.updateVersion(val)
                }
            }
            .padding(.horizontal)
            .padding(.top, 14)
            .padding(.bottom, 8)
        }
        .background(Color(.systemGroupedBackground))
    }

    // MARK: - Leaderboard Content

    @ViewBuilder
    private var leaderboardContent: some View {
        if vm.isLoading {
            LoadingView()
        } else if let error = vm.error {
            ErrorAlertView(message: error) {
                Task { await vm.loadIfNeeded() }
            }
        } else if let data = vm.data {
            if data.content.isEmpty {
                EmptyStateView(title: "No rankings data", description: "No results for this selection", systemImage: "chart.bar.xaxis")
            } else {
                rankingsTable(data)
            }
        }
    }

    private func rankingsTable(_ data: PagedResponse<Ranking>) -> some View {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 0) {
                Text("")
                    .frame(width: 32, alignment: .center)
                Text("Player")
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text("Dist")
                    .frame(width: 40, alignment: .center)
                Text("Sect")
                    .frame(width: 40, alignment: .center)
                Text("Natl")
                    .frame(width: 40, alignment: .center)
                Text("Pts")
                    .frame(width: 50, alignment: .trailing)
                Text("")
                    .frame(width: 24)
            }
            .font(.caption.weight(.semibold))
            .foregroundStyle(.secondary)
            .padding(.horizontal)
            .padding(.vertical, 8)
            .background(Color(.systemGroupedBackground))

            // Rows
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(data.content) { ranking in
                        NavigationLink(value: PlayerRoute(uaid: ranking.playerUaid)) {
                            rankingRow(ranking)
                        }
                        .buttonStyle(.plain)
                        Divider().padding(.leading)
                    }
                }

                // Pagination
                if data.totalPages > 1 {
                    PaginationControls(
                        currentPage: data.page,
                        totalPages: data.totalPages,
                        onPageChange: { vm.goToPage($0) }
                    )
                    .padding()
                }
            }
        }
    }

    private func rankingRow(_ r: Ranking) -> some View {
        HStack(spacing: 0) {
            // Green circle with national rank
            ZStack {
                Circle()
                    .fill(AppTheme.tennisGreen.opacity(0.15))
                    .frame(width: 28, height: 28)
                Text(r.nationalRank.map(String.init) ?? "\u{2014}")
                    .font(.caption2.weight(.bold))
                    .foregroundStyle(AppTheme.tennisGreen)
            }
            .frame(width: 32, alignment: .center)

            // Player info
            VStack(alignment: .leading, spacing: 2) {
                Text("\(r.playerFirstName) \(r.playerLastName)")
                    .font(.subheadline.weight(.medium))
                    .lineLimit(1)
                if let section = r.section {
                    Text(section + (r.district.map { " \u{00b7} \($0)" } ?? ""))
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // District rank
            Text(r.districtRank.map(String.init) ?? "\u{2014}")
                .font(.caption.monospaced())
                .frame(width: 40, alignment: .center)

            // Section rank
            Text(r.sectionRank.map(String.init) ?? "\u{2014}")
                .font(.caption.monospaced())
                .frame(width: 40, alignment: .center)

            // National rank
            Text(r.nationalRank.map(String.init) ?? "\u{2014}")
                .font(.caption.weight(.bold).monospaced())
                .foregroundStyle(r.nationalRank != nil && r.nationalRank! <= 3 ? AppTheme.tennisGreen : .primary)
                .frame(width: 40, alignment: .center)

            // Points
            Text(r.points.map(String.init) ?? "\u{2014}")
                .font(.caption.monospaced())
                .frame(width: 50, alignment: .trailing)

            // Trend
            trendIcon(r.trendDirection)
                .frame(width: 24, alignment: .center)
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
    }

    @ViewBuilder
    private func trendIcon(_ direction: String?) -> some View {
        switch direction {
        case "down":
            Image(systemName: "arrowtriangle.up.fill")
                .font(.caption2)
                .foregroundStyle(AppTheme.winGreen)
        case "up":
            Image(systemName: "arrowtriangle.down.fill")
                .font(.caption2)
                .foregroundStyle(AppTheme.lossRed)
        case "no change":
            Text("\u{2014}")
                .font(.caption2)
                .foregroundStyle(.secondary)
        default:
            EmptyView()
        }
    }

    // MARK: - Helpers

    private func formatVersionDate(_ dateString: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        guard let date = formatter.date(from: dateString) else { return dateString }
        formatter.dateFormat = "MMM d, yyyy"
        return formatter.string(from: date)
    }
}

// MARK: - Filter Menu Dropdown

private struct FilterMenu: View {
    let label: String
    let options: [(value: String, label: String)]
    let selectedValue: String
    let onSelect: (String) -> Void

    var body: some View {
        Menu {
            ForEach(options, id: \.value) { opt in
                Button {
                    onSelect(opt.value)
                } label: {
                    HStack {
                        Text(opt.label)
                        if opt.value == selectedValue {
                            Image(systemName: "checkmark")
                        }
                    }
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(selectedLabel)
                    .font(.caption.weight(.medium))
                    .lineLimit(1)
                Image(systemName: "chevron.down")
                    .font(.caption2)
            }
            .foregroundStyle(.primary)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(Color(.systemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 6))
            .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color(.separator), lineWidth: 0.5))
        }
    }

    private var selectedLabel: String {
        options.first(where: { $0.value == selectedValue })?.label ?? label
    }
}
