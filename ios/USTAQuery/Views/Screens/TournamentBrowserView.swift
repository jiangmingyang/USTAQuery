import SwiftUI

struct TournamentBrowserView: View {
    @State private var viewModel = TournamentBrowserViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // Search bar
            SearchBarView(text: $viewModel.searchText, placeholder: "Search tournaments...") {
                viewModel.currentPage = 0
                Task { await viewModel.search() }
            }
            .padding(.horizontal)
            .padding(.top, 14)

            // Inline filter row
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    // Year
                    Menu {
                        ForEach(["2026", "2025", "2024", "2023"], id: \.self) { year in
                            Button {
                                viewModel.selectedYear = year
                                viewModel.currentPage = 0
                                Task { await viewModel.search() }
                            } label: {
                                HStack {
                                    Text(year)
                                    if year == viewModel.selectedYear {
                                        Image(systemName: "checkmark")
                                    }
                                }
                            }
                        }
                    } label: {
                        filterLabel("Year", value: viewModel.selectedYear)
                    }

                    // Section filter
                    if let filters = viewModel.filters {
                        MultiFilterMenu(label: "Section", options: filters.sections, selected: Binding(
                            get: { viewModel.selectedSections },
                            set: { viewModel.selectedSections = $0 }
                        )) {
                            triggerSearch()
                        }

                        MultiFilterMenu(label: "Level", options: filters.levels, selected: Binding(
                            get: { viewModel.selectedLevels },
                            set: { viewModel.selectedLevels = $0 }
                        )) {
                            triggerSearch()
                        }

                        MultiFilterMenu(label: "Gender", options: filters.genders, selected: Binding(
                            get: { viewModel.selectedGenders },
                            set: { viewModel.selectedGenders = $0 }
                        )) {
                            triggerSearch()
                        }

                        MultiFilterMenu(label: "Age", options: filters.ageCategories, selected: Binding(
                            get: { viewModel.selectedAgeCategories },
                            set: { viewModel.selectedAgeCategories = $0 }
                        )) {
                            triggerSearch()
                        }

                        MultiFilterMenu(label: "Event", options: filters.eventTypes, selected: Binding(
                            get: { viewModel.selectedEventTypes },
                            set: { viewModel.selectedEventTypes = $0 }
                        )) {
                            triggerSearch()
                        }
                    }

                    // Clear filters
                    if viewModel.activeFilterCount > 0 {
                        Button {
                            viewModel.clearFilters()
                            triggerSearch()
                        } label: {
                            HStack(spacing: 2) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.caption2)
                                Text("Clear")
                                    .font(.caption.weight(.medium))
                            }
                            .foregroundStyle(.secondary)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 6)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }
            .background(Color(.systemGroupedBackground))

            // Results
            if viewModel.isLoading {
                LoadingView()
            } else if let error = viewModel.error {
                ErrorAlertView(message: error) {
                    Task { await viewModel.search() }
                }
                .padding()
                Spacer()
            } else if let results = viewModel.results {
                if results.content.isEmpty {
                    EmptyStateView(title: "No tournaments found", description: "Try adjusting your filters")
                } else {
                    List {
                        ForEach(results.content) { tournament in
                            NavigationLink(value: TournamentRoute(id: tournament.id)) {
                                TournamentCard(tournament: tournament)
                            }
                        }
                    }
                    .listStyle(.plain)

                    if results.totalPages > 1 {
                        PaginationControls(currentPage: viewModel.currentPage, totalPages: results.totalPages) { page in
                            Task { await viewModel.loadPage(page) }
                        }
                    }
                }
            }
        }
        .padding(.top, -22)
        .navigationTitle("Tournaments")
        .navigationDestination(for: TournamentRoute.self) { route in
            TournamentDetailView(tournamentId: route.id)
        }
        .navigationDestination(for: PlayerRoute.self) { route in
            PlayerProfileView(uaid: route.uaid)
        }
        .task {
            await viewModel.loadFilters()
            await viewModel.search()
        }
    }

    private func triggerSearch() {
        viewModel.currentPage = 0
        Task { await viewModel.search() }
    }

    private func filterLabel(_ label: String, value: String) -> some View {
        HStack(spacing: 4) {
            Text(value)
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

// MARK: - Multi-select Filter Menu

private struct MultiFilterMenu: View {
    let label: String
    let options: [String]
    @Binding var selected: Set<String>
    var onChange: () -> Void

    var body: some View {
        Menu {
            ForEach(options, id: \.self) { option in
                Button {
                    if selected.contains(option) {
                        selected.remove(option)
                    } else {
                        selected.insert(option)
                    }
                    onChange()
                } label: {
                    HStack {
                        Text(option)
                        if selected.contains(option) {
                            Image(systemName: "checkmark")
                        }
                    }
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(displayLabel)
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

    private var displayLabel: String {
        if selected.isEmpty { return label }
        if selected.count == 1 { return selected.first ?? label }
        return "\(label) (\(selected.count))"
    }
}
