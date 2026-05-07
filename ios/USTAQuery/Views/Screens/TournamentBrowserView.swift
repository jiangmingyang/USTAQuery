import SwiftUI

struct TournamentBrowserView: View {
    @State private var viewModel = TournamentBrowserViewModel()
    @State private var showFilters = false

    var body: some View {
        VStack(spacing: 0) {
            // Search + filter bar
            VStack(spacing: 8) {
                SearchBarView(text: $viewModel.searchText, placeholder: "Search tournaments...") {
                    viewModel.currentPage = 0
                    Task { await viewModel.search() }
                }

                HStack(spacing: 8) {
                    // Year picker
                    Picker("Year", selection: $viewModel.selectedYear) {
                        ForEach(["2023", "2024", "2025", "2026"], id: \.self) { year in
                            Text(year).tag(year)
                        }
                    }
                    .pickerStyle(.menu)
                    .onChange(of: viewModel.selectedYear) { _, _ in
                        viewModel.currentPage = 0
                        Task { await viewModel.search() }
                    }

                    Spacer()

                    // Filter button
                    Button {
                        showFilters = true
                    } label: {
                        HStack(spacing: 4) {
                            Image(systemName: "line.3.horizontal.decrease.circle")
                            Text("Filters")
                                .font(.subheadline)
                            if viewModel.activeFilterCount > 0 {
                                Text("\(viewModel.activeFilterCount)")
                                    .font(.caption2.bold())
                                    .padding(4)
                                    .background(AppTheme.tennisGreen)
                                    .foregroundStyle(.white)
                                    .clipShape(Circle())
                            }
                        }
                    }
                }
            }
            .padding()

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
        .navigationTitle("Tournaments")
        .navigationDestination(for: TournamentRoute.self) { route in
            TournamentDetailView(tournamentId: route.id)
        }
        .navigationDestination(for: PlayerRoute.self) { route in
            PlayerProfileView(uaid: route.uaid)
        }
        .sheet(isPresented: $showFilters) {
            FilterSheet(viewModel: viewModel) {
                showFilters = false
                viewModel.currentPage = 0
                Task { await viewModel.search() }
            }
        }
        .task {
            await viewModel.loadFilters()
            await viewModel.search()
        }
    }
}

// MARK: - Filter Sheet

private struct FilterSheet: View {
    let viewModel: TournamentBrowserViewModel
    var onApply: () -> Void

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                if let filters = viewModel.filters {
                    MultiSelectSection(title: "Section", options: filters.sections, selected: Binding(
                        get: { viewModel.selectedSections },
                        set: { viewModel.selectedSections = $0 }
                    ))
                    MultiSelectSection(title: "Level", options: filters.levels, selected: Binding(
                        get: { viewModel.selectedLevels },
                        set: { viewModel.selectedLevels = $0 }
                    ))
                    MultiSelectSection(title: "Gender", options: filters.genders, selected: Binding(
                        get: { viewModel.selectedGenders },
                        set: { viewModel.selectedGenders = $0 }
                    ))
                    MultiSelectSection(title: "Age Group", options: ["U8", "U10", "U12", "U14", "U16", "U18", "Other"], selected: Binding(
                        get: { viewModel.selectedAgeCategories },
                        set: { viewModel.selectedAgeCategories = $0 }
                    ))
                    MultiSelectSection(title: "Event Type", options: filters.eventTypes, selected: Binding(
                        get: { viewModel.selectedEventTypes },
                        set: { viewModel.selectedEventTypes = $0 }
                    ))
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Clear") {
                        viewModel.clearFilters()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Apply", action: onApply)
                        .fontWeight(.semibold)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

private struct MultiSelectSection: View {
    let title: String
    let options: [String]
    @Binding var selected: Set<String>

    var body: some View {
        Section(title) {
            ForEach(options, id: \.self) { option in
                Button {
                    if selected.contains(option) {
                        selected.remove(option)
                    } else {
                        selected.insert(option)
                    }
                } label: {
                    HStack {
                        Text(option)
                            .foregroundStyle(.primary)
                        Spacer()
                        if selected.contains(option) {
                            Image(systemName: "checkmark")
                                .foregroundStyle(AppTheme.tennisGreen)
                        }
                    }
                }
            }
        }
    }
}
