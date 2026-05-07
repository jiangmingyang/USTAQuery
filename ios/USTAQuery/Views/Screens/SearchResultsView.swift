import SwiftUI

struct SearchResultsView: View {
    let initialQuery: String
    @State private var viewModel = SearchViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // Search bar
            SearchBarView(text: $viewModel.query, placeholder: "Search players...") {
                Task { await viewModel.search() }
            }
            .padding()

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
                    EmptyStateView(title: "No players found", description: "Try a different search term")
                } else {
                    List {
                        ForEach(results.content) { player in
                            NavigationLink(value: PlayerRoute(uaid: player.uaid)) {
                                PlayerCard(player: player)
                            }
                        }
                    }
                    .listStyle(.plain)

                    if results.totalPages > 1 {
                        PaginationControls(
                            currentPage: viewModel.currentPage,
                            totalPages: results.totalPages
                        ) { page in
                            Task { await viewModel.loadPage(page) }
                        }
                    }
                }
            } else {
                EmptyStateView(title: "Search for players", description: "Enter a name or UAID to find players", systemImage: "magnifyingglass")
            }
        }
        .navigationTitle("Search")
        .navigationBarTitleDisplayMode(.inline)
        .navigationDestination(for: PlayerRoute.self) { route in
            PlayerProfileView(uaid: route.uaid)
        }
        .navigationDestination(for: TournamentRoute.self) { route in
            TournamentDetailView(tournamentId: route.id)
        }
        .task {
            if !initialQuery.isEmpty {
                viewModel.query = initialQuery
                await viewModel.search()
            }
        }
    }
}
