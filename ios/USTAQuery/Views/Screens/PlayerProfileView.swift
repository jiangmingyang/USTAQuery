import SwiftUI

struct PlayerProfileView: View {
    let uaid: String
    @State private var viewModel: PlayerProfileViewModel

    init(uaid: String) {
        self.uaid = uaid
        _viewModel = State(initialValue: PlayerProfileViewModel(uaid: uaid))
    }

    var body: some View {
        Group {
            if viewModel.isLoading {
                LoadingView()
            } else if let error = viewModel.error {
                ErrorAlertView(message: error) {
                    Task { await viewModel.loadProfile() }
                }
                .padding()
            } else if let player = viewModel.player {
                ScrollView {
                    VStack(spacing: 16) {
                        PlayerInfoHeader(player: player, stats: viewModel.stats)

                        // Tab selector
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(PlayerTab.allCases, id: \.self) { tab in
                                    Button {
                                        viewModel.selectedTab = tab
                                    } label: {
                                        Text(tab.rawValue)
                                            .font(.subheadline.weight(viewModel.selectedTab == tab ? .semibold : .regular))
                                            .padding(.horizontal, 14)
                                            .padding(.vertical, 7)
                                            .background(viewModel.selectedTab == tab ? AppTheme.tennisGreen : Color(.systemGray6))
                                            .foregroundStyle(viewModel.selectedTab == tab ? .white : .primary)
                                            .clipShape(Capsule())
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }

                        // Tab content
                        tabContent
                            .padding(.horizontal)
                    }
                    .padding(.bottom, 20)
                }
            }
        }
        .navigationTitle(viewModel.player.map { "\($0.firstName) \($0.lastName)" } ?? "Player")
        .navigationBarTitleDisplayMode(.inline)
        .task { await viewModel.loadProfile() }
        .refreshable { await viewModel.loadProfile() }
        .onChange(of: viewModel.selectedTab) { _, tab in
            Task { await viewModel.loadTabIfNeeded(tab) }
        }
    }

    @ViewBuilder
    private var tabContent: some View {
        switch viewModel.selectedTab {
        case .info:
            if let player = viewModel.player {
                PlayerInfoTab(player: player)
            }
        case .tournaments:
            PlayerTournamentsTab(entries: viewModel.tournamentEntries, isLoading: viewModel.tournamentsLoading)
        case .registrations:
            PlayerRegistrationsTab(
                registrations: viewModel.registrations,
                isLoading: viewModel.registrationsLoading,
                currentPage: viewModel.registrationsPage
            ) { page in
                Task { await viewModel.loadRegistrations(page: page) }
            }
        case .matches:
            PlayerMatchesTab(
                uaid: uaid,
                matches: viewModel.matches,
                isLoading: viewModel.matchesLoading,
                currentPage: viewModel.matchesPage
            ) { page in
                Task { await viewModel.loadMatches(page: page) }
            }
        case .rankings:
            PlayerRankingsTab(
                rankings: viewModel.rankings,
                isLoading: viewModel.rankingsLoading,
                selectedAge: $viewModel.selectedAgeRestriction
            ) {
                Task { await viewModel.loadRankings() }
            }
        }
    }
}
