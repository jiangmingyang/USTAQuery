import SwiftUI

struct RankingsView: View {
    @State private var vm = RankingsViewModel()
    @State private var showFilters = false

    var initialGender: String?
    var initialAge: String?

    var body: some View {
        VStack(spacing: 0) {
            filterBar
            leaderboardContent
        }
        .navigationTitle("Rankings")
        .navigationBarTitleDisplayMode(.large)
        .navigationDestination(for: PlayerRoute.self) { route in
            PlayerProfileView(uaid: route.uaid)
        }
        .sheet(isPresented: $showFilters) {
            filterSheet
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
        VStack(spacing: 12) {
            // Summary + filter button
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(vm.selectedList.label)
                        .font(.subheadline.weight(.medium))
                        .lineLimit(1)
                    Text("\(vm.genderLabel) \(vm.ageLabel)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                Button {
                    showFilters = true
                } label: {
                    Label("Filters", systemImage: "line.3.horizontal.decrease.circle")
                        .font(.subheadline)
                }
            }

            // Version picker (inline when versions available)
            if !vm.versions.isEmpty {
                HStack(spacing: 8) {
                    Text("Version")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Picker("Version", selection: Binding(
                        get: { vm.publishDate },
                        set: { vm.updateVersion($0) }
                    )) {
                        Text("Latest").tag("")
                        ForEach(vm.versions, id: \.self) { v in
                            Text(formatVersionDate(v)).tag(v)
                        }
                    }
                    .pickerStyle(.menu)
                    .tint(AppTheme.tennisGreen)
                    Spacer()
                }
            }

            if let data = vm.data, !data.content.isEmpty {
                Text("\(data.totalElements) players")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
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
                Text("W/L")
                    .frame(width: 44, alignment: .center)
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

            // W/L
            if let w = r.wins, let l = r.losses {
                Text("\(w)-\(l)")
                    .font(.caption2.monospaced())
                    .frame(width: 44, alignment: .center)
            } else {
                Text("\u{2014}")
                    .font(.caption2)
                    .frame(width: 44, alignment: .center)
            }

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

    // MARK: - Filter Sheet

    private var filterSheet: some View {
        NavigationStack {
            List {
                // Ranking List Type
                Section("Ranking List") {
                    ForEach(AppConstants.listTypes, id: \.value) { lt in
                        Button {
                            vm.updateFilter(list: lt.value)
                        } label: {
                            HStack {
                                Text(lt.label)
                                    .font(.subheadline)
                                    .foregroundStyle(.primary)
                                Spacer()
                                if vm.listKey == lt.value {
                                    Image(systemName: "checkmark")
                                        .foregroundStyle(AppTheme.tennisGreen)
                                }
                            }
                        }
                    }
                }

                // Gender
                Section("Gender") {
                    ForEach(AppConstants.genders, id: \.value) { g in
                        Button {
                            vm.updateFilter(gender: g.value)
                        } label: {
                            HStack {
                                Text(g.label)
                                    .font(.subheadline)
                                    .foregroundStyle(.primary)
                                Spacer()
                                if vm.gender == g.value {
                                    Image(systemName: "checkmark")
                                        .foregroundStyle(AppTheme.tennisGreen)
                                }
                            }
                        }
                    }
                }

                // Age Group
                Section("Age Group") {
                    ForEach(AppConstants.ageRestrictions, id: \.self) { age in
                        Button {
                            vm.updateFilter(age: age)
                        } label: {
                            HStack {
                                Text(AppConstants.ageGroupLabels[age] ?? age)
                                    .font(.subheadline)
                                    .foregroundStyle(.primary)
                                Spacer()
                                if vm.ageRestriction == age {
                                    Image(systemName: "checkmark")
                                        .foregroundStyle(AppTheme.tennisGreen)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Filter Rankings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") { showFilters = false }
                }
            }
        }
        .presentationDetents([.medium, .large])
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
